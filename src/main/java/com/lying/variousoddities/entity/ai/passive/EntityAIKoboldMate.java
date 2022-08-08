package com.lying.variousoddities.entity.ai.passive;

import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Predicate;
import com.lying.variousoddities.api.world.settlement.EnumRoomFunction;
import com.lying.variousoddities.api.world.settlement.Settlement;
import com.lying.variousoddities.entity.ai.EntityAIOperateRoom;
import com.lying.variousoddities.entity.passive.EntityKobold;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.utility.VOHelper;
import com.lying.variousoddities.world.savedata.SettlementManager;
import com.lying.variousoddities.world.settlement.BoxRoom;
import com.lying.variousoddities.world.settlement.SettlementKobold;
import com.lying.variousoddities.world.settlement.SettlementRoomBehaviours;
import com.lying.variousoddities.world.settlement.SettlementRoomBehaviours.KoboldRoomBehaviourNest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class EntityAIKoboldMate extends Goal
{
	private final EntityKobold theKobold;
	private final Level theWorld;
	private final PathNavigation theNavigator;
	
	private EntityKobold targetMate = null;
	private BlockPos targetEgg = null;
	
	private State currentState = null;
	private int matingTime = 0;
	
	private static final Predicate<EntityKobold> IN_LOVE_FILTER = new Predicate<EntityKobold>()
		{
			public boolean apply(EntityKobold input)
			{
				return input.isAlive() && input.isInLove();
			}
		};
	
	public EntityAIKoboldMate(EntityKobold koboldIn)
	{
		theKobold = koboldIn;
		theWorld = koboldIn.getLevel();
		theNavigator = koboldIn.getNavigation();
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}
	
	public boolean canUse()
	{
		if(theKobold.getTarget() == null && theKobold.getRandom().nextInt(20) == 0)
		{
			if(theKobold.isCarryingEgg() && theWorld.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))
				return true;
			
			if(theKobold.getAge() != 0)
				return false;
			
			// 1F == full moon
			if(theWorld.getMoonFactor() != 1F)
				return false;
			
			long dayTime = theWorld.getDayTime();
			// >=15000 && <= 21000 == meaningfully after moonrise & meaningfully before moonset
			if(dayTime < 15000 || dayTime > 21000)
				return false;
			
			if(!theWorld.canSeeSky(theKobold.blockPosition()))
				return false;
			
			return true;
		}
		
		return false;
	}
	
	public boolean shouldContinueExecuting()
	{
		return theKobold.getTarget() == null && currentState != null;
	}
	
	public void resetTask()
	{
		this.matingTime = 0;
		this.currentState = null;
		this.targetMate = null;
		this.targetEgg = null;
	}
	
	public void startExecuting()
	{
		if(theKobold.isCarryingEgg())
			setState(State.SEARCHING_EGG);
		else
		{
			if(!theKobold.isInLove())
				theKobold.setInLove(true);
			setState(State.SEARCHING_MATE);
		}
	}
	
	public void tick()
	{
		// Cease mating entirely if we aren't in the mood
		if(!theKobold.isInLove() && this.currentState.needsLove){ setState(null); return; }
		
		switch(currentState)
		{
			case SEARCHING_MATE:
				if(isMateInvalid())
				{
					this.targetMate = null;
					for(EntityKobold kobold : theWorld.getEntitiesOfClass(EntityKobold.class, theKobold.getBoundingBox().inflate(16), IN_LOVE_FILTER))
					{
						if(kobold == theKobold)
							continue;
						
						if(theNavigator.createPath(kobold, 32) != null)
						{
							targetMate = kobold;
							break;
						}
					}
					if(targetMate == null)
					{
						setState(null);
						theKobold.setInLove(false);
					}
				}
				else if(targetMate.distanceTo(theKobold) > 1F)
				{
					theKobold.getLookController().setLookPositionWithEntity(targetMate, 30F, 30F);
					theNavigator.moveTo(targetMate, 1D);
					if(theNavigator.isDone())
						this.targetMate = null;
				}
				else
				{
					this.matingTime = 0;
					setState(State.MATING);
				}
				
				break;
			case MATING:
				if(isMateInvalid())
					this.currentState = State.SEARCHING_MATE;
				else
				{
					theKobold.getLookController().setLookPositionWithEntity(targetMate, 30F, 30F);
					if(targetMate.distanceTo(theKobold) > 1F)
						theKobold.getNavigation().moveTo(targetMate, 1D);
					else if(matingTime < (Reference.Values.TICKS_PER_SECOND * 15))
						matingTime++;
					else
					{
						theKobold.onMatingFinish();
						targetMate.onMatingFinish();
						theKobold.setCarryingEgg(true);
						setState(State.SEARCHING_EGG);
					}
				}
				
				break;
			case SEARCHING_EGG:
				SettlementManager manager = SettlementManager.get(theWorld);
				/*
				 * Search the world for the nearest available nest
				 * If we can't find any nests, try to create one instead
				 * Then set to supervise said nest
				 */
				List<Settlement> nests = manager.getSettlementsOfType(new ResourceLocation(Reference.ModInfo.MOD_ID,"kobold"));
				BoxRoom closestNest = null;
				double closestDist = Double.MAX_VALUE;
				for(Settlement nest : nests)
					for(BoxRoom room : nest.getRoomsOfType(EnumRoomFunction.NEST))
					{
						BlockPos core = room.getCore();
						if(theNavigator.createPath(core, 64) == null)
							continue;
						
						double dist = theKobold.blockPosition().distSqr(core);
						if(dist < closestDist && dist < (32D * 32D))
						{
							closestNest = room;
							closestDist = dist;
						}
					}
				
				if(closestNest == null)
				{
					// Establish new nest
					BlockPos pos = theKobold.blockPosition();
					BlockPos testPos;
					RandomSource rand = theKobold.getRandom();
					int attempts = 100;
					do
					{
						int xOff = rand.nextInt(16) - 8;
						int yOff = rand.nextInt(8) - 4;
						int zOff = rand.nextInt(16) - 8;
						testPos = pos.offset(xOff, yOff, zOff);
					}
					while(--attempts > 0 && !KoboldRoomBehaviourNest.isPositionValidForEgg(testPos, theWorld));
					
					if(attempts > 0)
					{
						BoxRoom nest = new BoxRoom(testPos);
						nest.addAll(VOHelper.getReplaceableVolumeAround(testPos, theWorld, 8, KoboldRoomBehaviourNest::canIncludeBlock));
						nest.setFunction(EnumRoomFunction.NEST);
						
						Settlement koboldSettlement = new SettlementKobold(theWorld);
						koboldSettlement.addRoom(nest);
						
						manager.add(koboldSettlement);
						closestNest = nest;
					}
				}
				
				if(closestNest != null)
				{
					EntityAIOperateRoom roomTask = EntityAIOperateRoom.getOperateTask(theKobold);
					if(closestNest != null && roomTask != null)
						roomTask.requestVisitTo(closestNest, SettlementRoomBehaviours.KOBOLD_NEST, theKobold.getRandom());
				}
				setState(null);
		}
	}
	
	public void setState(State stateIn)
	{
		this.currentState = stateIn;
	}
	
	public boolean isMateInvalid()
	{
		return targetMate == null || !targetMate.isAlive() || !targetMate.isInLove();
	}
	
	/**
	 * Returns true if no nearby nest is available or it has been rendered invalid
	 */
	public boolean isEggInvalid()
	{
		return targetEgg == null || !isPositionValidForEgg(targetEgg);
	}
	
	public boolean isPositionValidForEgg(BlockPos pos)
	{
		if(pos != null)
			if(theWorld.isEmptyBlock(pos) && Block.hasSolidSideOnTop(theWorld, pos.below()))
				return theWorld.isAreaLoaded(pos, 1) && !theWorld.canSeeSky(pos);
		return false;
	}
	
	public boolean hasSolidNeighbour(BlockPos pos)
	{
		for(Direction face : Direction.Plane.HORIZONTAL)
			if(theWorld.getBlockState(pos.relative(face)).isOpaqueCube(theWorld, pos.relative(face)))
				return true;
		return false;
	}
	
	public BlockPos getRandomEggSite()
	{
		BlockPos eggSite = null;
		int attempts = 100;
		while(!(isPositionValidForEgg(eggSite) && hasSolidNeighbour(eggSite)) && --attempts > 0)
		{
			double moveX = theKobold.getRandom().nextInt(32)	- 16;
			double moveY = theKobold.getRandom().nextInt(6)	- 3;
			double moveZ = theKobold.getRandom().nextInt(32)	- 16;
			eggSite = theKobold.blockPosition().offset(moveX, moveY, moveZ);
		}
		if(attempts >= 0 && theKobold.getNavigation().createPath(eggSite, 16) != null) return eggSite;
		return null;
	}
	
	private enum State
	{
		/** Trying to find a mate */
		SEARCHING_MATE(true),
		/** Mating */
		MATING(true),
		/** Looking for somewhere to lay their egg */
		SEARCHING_EGG(false);
		
		public final boolean needsLove;
		
		private State(boolean loveIn)
		{
			needsLove = loveIn;
		}
	}
}
