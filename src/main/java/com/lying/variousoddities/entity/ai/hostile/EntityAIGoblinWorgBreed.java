package com.lying.variousoddities.entity.ai.hostile;

import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.passive.EntityWorg;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EntityAIGoblinWorgBreed extends Goal
{
	private final CreatureEntity theGoblin;
	private final World theWorld;
	private final PathNavigator theNavigator;
	
	private final Predicate<EntityWorg> searchPredicate = new Predicate<EntityWorg>()
			{
				public boolean apply(EntityWorg input)
				{
					return input.isAlive() && input.getGrowingAge() == 0 && !input.isInLove() && !input.isTamed() && (input.getAttackTarget() == null || !input.getAttackTarget().isAlive());
				}
			};
	private EntityWorg worgA, worgB;
	
	private int breedingTimer = Reference.Values.TICKS_PER_SECOND * 2;
	private State currentState = null;
	
	public EntityAIGoblinWorgBreed(CreatureEntity creatureIn)
	{
		theGoblin = creatureIn;
		theWorld = creatureIn.getEntityWorld();
		theNavigator = creatureIn.getNavigator();
        setMutexFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
	}
	
	public boolean shouldExecute()
	{
		List<EntityWorg> eligibleWorgs = theWorld.<EntityWorg>getEntitiesWithinAABB(EntityWorg.class, theGoblin.getBoundingBox().grow(8), searchPredicate);
		if(eligibleWorgs.size() > 6) return false;
		
		// Find Worg A
		double minDist = Double.MAX_VALUE;
		for(EntityWorg worg : eligibleWorgs)
		{
			double distance = theGoblin.getDistanceSq(worg);
			Path path = theNavigator.getPathToEntity(worg, 10);
			if(distance < minDist && path != null)
			{
				worgA = worg;
				minDist = distance;
			}
		}
		eligibleWorgs.remove(worgA);
		
		if(worgA != null)
		{
			// Find Worg B
			minDist = Double.MAX_VALUE;
			for(EntityWorg worg : eligibleWorgs)
			{
				double distance = worgA.getDistanceSq(worg);
				Path path = worgA.getNavigator().getPathToEntity(worg, 10);
				if(distance < minDist && path != null)
				{
					worgB = worg;
					minDist = distance;
				}
			}
		}
		
		return worgA != null && worgB != null && theGoblin.getRNG().nextInt(500) == 0;
	}
	
	public boolean shouldContinueExecuting()
	{
		return currentState != null;
	}
	
	public void resetTask()
	{
		if(worgA != null) worgA.func_233687_w_(false);
		if(worgB != null) worgB.func_233687_w_(false);
		currentState = null;
		worgA = worgB = null;
	}
	
	public void startExecuting()
	{
		theGoblin.getLookController().setLookPositionWithEntity(worgA, (float)(theGoblin.getHorizontalFaceSpeed() + 20), (float)theGoblin.getVerticalFaceSpeed());
		currentState = State.MOVING_TO_A;
	}
	
	public void tick()
	{
		if(!worgA.isAlive() || !worgB.isAlive() || worgA == null || worgB == null)
		{
			currentState = null;
			return;
		}
		
		switch(currentState)
		{
			case MOVING_TO_A:
				theGoblin.getLookController().setLookPositionWithEntity(worgA, (float)(theGoblin.getHorizontalFaceSpeed() + 20), (float)theGoblin.getVerticalFaceSpeed());
				if(theGoblin.getDistance(worgA) > 1.5D)
				{
					if(theNavigator.noPath())
					{
						theNavigator.tryMoveToEntityLiving(worgA, 1.0D);
						if(theNavigator.noPath())
							currentState = null;
					}
				}
				else
				{
					worgA.func_233687_w_(true);
					theNavigator.clearPath();
					breedingTimer = Reference.Values.TICKS_PER_SECOND * 2;
					currentState = State.BREEDING_A;
				}
				break;
			case BREEDING_A:
				if(theGoblin.getDistance(worgA) < 1.5D)
				{
					if(--breedingTimer <= 0)
					{
						theGoblin.swingArm(Hand.MAIN_HAND);
						worgA.setInLove(null);
						worgA.func_233687_w_(false);
						worgA.getNavigator().tryMoveToEntityLiving(worgB, 1D);
						currentState = State.MOVING_TO_B;
					}
				}
				else currentState = State.MOVING_TO_A;
				break;
			case MOVING_TO_B:
				theGoblin.getLookController().setLookPositionWithEntity(worgB, (float)(theGoblin.getHorizontalFaceSpeed() + 20), (float)theGoblin.getVerticalFaceSpeed());
				if(theGoblin.getDistance(worgB) > 1.5D)
				{
					if(theNavigator.noPath())
					{
						theNavigator.tryMoveToEntityLiving(worgB, 1.0D);
						if(theNavigator.noPath())
							currentState = null;
					}
				}
				else
				{
					worgB.func_233687_w_(true);
					theNavigator.clearPath();
					breedingTimer = Reference.Values.TICKS_PER_SECOND * 2;
					currentState = State.BREEDING_B;
				}
				break;
			case BREEDING_B:
				if(theGoblin.getDistance(worgB) < 1.5D)
				{
					if(--breedingTimer <= 0)
					{
						theGoblin.swingArm(Hand.MAIN_HAND);
						worgB.setInLove(null);
						worgB.func_233687_w_(false);
						worgB.getNavigator().tryMoveToEntityLiving(worgA, 1D);
						currentState = null;
					}
				}
				else currentState = State.MOVING_TO_B;
				break;
			default:
				currentState = null;
				break;
		}
	}
	
	private enum State
	{
		MOVING_TO_A,
		BREEDING_A,
		MOVING_TO_B,
		BREEDING_B;
	}
}
