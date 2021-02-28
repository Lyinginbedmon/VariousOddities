package com.lying.variousoddities.entity.ai;

import java.util.EnumSet;
import java.util.Random;

import com.lying.variousoddities.api.world.settlement.SettlementRoomBehaviour;
import com.lying.variousoddities.entity.ISettlementEntity;
import com.lying.variousoddities.world.settlement.BoxRoom;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntityAIOperateRoom extends Goal
{
	protected final World theWorld;
	protected final MobEntity theMob;
	protected final PathNavigator theNavigator;
	
	protected BoxRoom targetRoom = null;
	protected SettlementRoomBehaviour targetBehaviour = null;
	protected Random targetRand = null;
	
	protected boolean discontinue = false;
	
	public EntityAIOperateRoom(MobEntity creatureIn)
	{
		theWorld = creatureIn.getEntityWorld();
		theMob = creatureIn;
		theNavigator = creatureIn.getNavigator();
		this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
	}
	
	public boolean shouldExecute()
	{
		return theMob.getAttackTarget() == null && isBusy();
	}
	
	public boolean shouldContinueExecuting()
	{
		return shouldExecute() && !isInsideRoom() && !discontinue;
	}
	
	public void resetTask()
	{
		if(isInsideRoom() && !theWorld.isRemote)
			targetBehaviour.function(targetRoom, (ServerWorld)theWorld, targetRand);
		
		theNavigator.clearPath();
		targetRoom = null;
		targetBehaviour = null;
		targetRand = null;
		discontinue = false;
	}
	
	public void startExecuting()
	{
		theNavigator.clearPath();
		BlockPos core = targetRoom.getCore();
		
		Path path = theNavigator.getPathToPos(core, (int)theMob.getAttribute(Attributes.FOLLOW_RANGE).getValue());
		if(path != null)
			theNavigator.setPath(path, 1.0D);
	}
	
	public void tick()
	{
		if(!isInsideRoom())
		{
			if(theNavigator.noPath())
			{
				startExecuting();
				
				if(theNavigator.noPath() && !isInsideRoom())
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
	
	public void requestVisitTo(BoxRoom roomIn, SettlementRoomBehaviour behaviourIn, Random randIn)
	{
		if(isBusy()) return;
		targetRoom = roomIn;
		targetBehaviour = behaviourIn;
		targetRand = randIn;
	}
	
	public static EntityAIOperateRoom getOperateTask(MobEntity creature)
	{
		return creature instanceof ISettlementEntity ? ((ISettlementEntity)creature).getOperateRoomTask() : null;
	}
}
