package com.lying.variousoddities.entity.ai;

import java.util.EnumSet;

import com.lying.variousoddities.api.world.settlement.SettlementRoomBehaviour;
import com.lying.variousoddities.entity.ISettlementEntity;
import com.lying.variousoddities.world.settlement.BoxRoom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class EntityAIOperateRoom extends Goal
{
	protected final Level theWorld;
	protected final Monster theMob;
	protected final PathNavigation theNavigator;
	
	protected BoxRoom targetRoom = null;
	protected SettlementRoomBehaviour targetBehaviour = null;
	protected RandomSource targetRand = null;
	
	protected boolean discontinue = false;
	
	public EntityAIOperateRoom(Monster creatureIn)
	{
		theWorld = creatureIn.getLevel();
		theMob = creatureIn;
		theNavigator = creatureIn.getNavigation();
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}
	
	public boolean canUse()
	{
		return theMob.getTarget() == null && isBusy();
	}
	
	public boolean canContinueToUse()
	{
		return canUse() && !isInsideRoom() && !discontinue;
	}
	
	public void stop()
	{
		if(isInsideRoom() && !theWorld.isClientSide)
			targetBehaviour.function(targetRoom, (ServerLevel)theWorld, targetRand);
		
		theNavigator.stop();
		targetRoom = null;
		targetBehaviour = null;
		targetRand = null;
		discontinue = false;
	}
	
	public void start()
	{
		theNavigator.stop();
		BlockPos core = targetRoom.getCore();
		
		theNavigator.moveTo(core.getX(), core.getY(), core.getZ(), 1.0D);
	}
	
	public void tick()
	{
		if(!isInsideRoom())
		{
			if(theNavigator.isDone())
			{
				start();
				
				if(theNavigator.isDone() && !isInsideRoom())
					discontinue = true;
			}
		}
		else
			discontinue = true;
	}
	
	public boolean isInsideRoom()
	{
		if(targetRoom == null) return true;
		return targetRoom.getBounds().intersects(theMob.getBoundingBox());
	}
	
	public boolean isBusy()
	{
		return targetRoom != null && targetBehaviour != null;
	}
	
	public BlockPos getDestination()
	{
		return targetRoom.getCore();
	}
	
	public void requestVisitTo(BoxRoom roomIn, SettlementRoomBehaviour behaviourIn, RandomSource randIn)
	{
		if(isBusy()) return;
		targetRoom = roomIn;
		targetBehaviour = behaviourIn;
		targetRand = randIn;
	}
	
	public static EntityAIOperateRoom getOperateTask(Mob creature)
	{
		return creature instanceof ISettlementEntity ? ((ISettlementEntity)creature).getOperateRoomTask() : null;
	}
}
