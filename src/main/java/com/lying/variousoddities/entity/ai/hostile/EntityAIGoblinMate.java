package com.lying.variousoddities.entity.ai.hostile;

import java.util.EnumSet;
import java.util.Random;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntityAIGoblinMate extends Goal
{
	private final World theWorld;
	private final EntityGoblin theGoblin;
	private final PathNavigator theNavigator;
	
	private final Predicate<EntityGoblin> searchPredicate = new Predicate<EntityGoblin>()
			{
				public boolean apply(EntityGoblin input)
				{
					return input.isAlive() && !input.isInLove();
				}
			};
	
	private State currentState = null;
	private int matingTimer = 0;
	
	private EntityGoblin targetMate = null;
	
	public EntityAIGoblinMate(EntityGoblin goblinIn)
	{
		theGoblin = goblinIn;
		theNavigator = goblinIn.getNavigator();
		theWorld = goblinIn.getEntityWorld();
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}
	
	public boolean shouldExecute()
	{
		if(theGoblin.getAttackTarget() != null)
		{
			theGoblin.setInLove(false);
			return false;
		}
		
		if(theWorld.getEntitiesWithinAABB(EntityGoblin.class, theGoblin.getBoundingBox().grow(16), searchPredicate).isEmpty())
			return false;
		
		return (theGoblin.isCarrying() || theGoblin.getGrowingAge() == 0 && !theGoblin.isChild()) && theGoblin.getRNG().nextInt(100) == 0;
	}
	
	public boolean shouldContinueExecuting()
	{
		return theGoblin.getAttackTarget() == null && currentState != null && (theGoblin.isInLove() || !currentState.needsLove);
	}
	
	public void resetTask()
	{
		currentState = null;
		matingTimer = 0;
		targetMate = null;
	}
	
	public void startExecuting()
	{
		theGoblin.getNavigator().clearPath();
		if(theGoblin.isCarrying())
			currentState = State.SEARCHING_NEST;
		else
		{
			theGoblin.setInLove(true);
			currentState = State.SEARCHING_MATE;
		}
	}
	
	public void updateTask()
	{
		BlockPos nestSite = theGoblin.getNestSite();
		switch(currentState)
		{
			case SEARCHING_MATE:
				if(targetMate == null || !targetMate.isAlive() || !targetMate.isInLove())
				{
					for(EntityGoblin goblin : theWorld.getEntitiesWithinAABB(EntityGoblin.class, theGoblin.getBoundingBox().grow(16), searchPredicate))
					{
						if(goblin != theGoblin)
						{
							if(theNavigator.getPathToEntity(goblin, (int)theGoblin.getAttributeValue(Attributes.FOLLOW_RANGE)) != null)
							{
								targetMate = goblin;
								break;
							}
						}
					}
				}
				else
				{
					matingTimer = Reference.Values.TICKS_PER_SECOND * 10;
					currentState = State.MATING;
				}
				break;
			case MATING:
				if(!targetMate.isInLove()) currentState = State.SEARCHING_MATE;
				else if(theGoblin.getDistance(targetMate) < 1D)
				{
					theNavigator.clearPath();
					if(--matingTimer <= 0)
					{
						matingTimer = Reference.Values.TICKS_PER_SECOND * 5;
						theGoblin.setInLove(false);
						targetMate.setInLove(false);
						theGoblin.setGrowingAge(72000);
						targetMate.setGrowingAge(72000);
						
						theGoblin.setCarryingFrom(targetMate);
						currentState = State.SEARCHING_NEST;
					}
				}
				else theNavigator.tryMoveToEntityLiving(targetMate, 1.0D);
				break;
			case SEARCHING_NEST:
				if(nestSite == null || !isValidForNest(nestSite) || distanceToPos(nestSite) > 32D)
					theGoblin.setNestSite(getRandomNestSite());
				else
				{
					if(distanceToPos(nestSite) > 1.5D) theNavigator.tryMoveToXYZ(nestSite.getX(), nestSite.getY(), nestSite.getZ(), 1.2D);
					else
					{
						theNavigator.clearPath();
						
						if(--matingTimer <= 0)
						{
							EntityGoblin parent = theGoblin.getOtherParent();
							for(int i=0; i < (1 + theGoblin.getRNG().nextInt(2)); i++)
							{
								EntityGoblin child = (EntityGoblin)theGoblin.func_241840_a((ServerWorld)theWorld, parent);
								child.copyLocationAndAnglesFrom(theGoblin);
								child.setGrowingAge(-4000);
								theWorld.addEntity(child);
							}
							
							theGoblin.setCarryingFrom(null);
							matingTimer = Reference.Values.TICKS_PER_SECOND * 3;
							currentState = State.LEAVING_NEST;
						}
					}
				}
				break;
			case LEAVING_NEST:
				if(distanceToPos(nestSite) < 8D && matingTimer-- > 0)
				{
					if(theNavigator.noPath())
					{
						// Find random position away from nest site and move towards it
			            Vector3d targetFlee = RandomPositionGenerator.findRandomTargetBlockAwayFrom(theGoblin, 16, 7, new Vector3d(nestSite.getX(), nestSite.getY(), nestSite.getZ()));
			            if(targetFlee != null) theNavigator.tryMoveToXYZ(targetFlee.x, targetFlee.y, targetFlee.z, 1.2D);
					}
				}
				else currentState = null;
				break;
		}
	}
	
	public double distanceToPos(BlockPos pos)
	{
		return Math.sqrt(theGoblin.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()));
	}
	
	public BlockPos getRandomNestSite()
	{
		Random rand = theGoblin.getRNG();
		int attempts = 200;
		BlockPos nestSite = null;
		while(!isValidSite(nestSite) && attempts-- > 0)
		{
			double offX = rand.nextInt(16) - 8;
			double offY = rand.nextInt(8) - 4;
			double offZ = rand.nextInt(16) - 8;
			nestSite = theGoblin.getPosition().add(offX, offY, offZ);
		}
		
		return isValidSite(nestSite) ? nestSite : null;
	}
	
	private boolean isValidSite(BlockPos pos)
	{
		return pos != null && isValidForNest(pos) && theNavigator.getPathToPos(pos, (int)theGoblin.getAttributeValue(Attributes.FOLLOW_RANGE)) != null;
	}
	
	public boolean isValidForNest(BlockPos pos)
	{
		return !theWorld.canBlockSeeSky(pos) && theWorld.isAirBlock(pos) && theWorld.getLight(pos) <= 6;
	}
	
	private enum State
	{
		SEARCHING_MATE(true),
		MATING(true),
		SEARCHING_NEST(false),
		LEAVING_NEST(false);
		
		public boolean needsLove;
		private State(boolean loveIn)
		{
			needsLove = loveIn;
		}
	}
}
