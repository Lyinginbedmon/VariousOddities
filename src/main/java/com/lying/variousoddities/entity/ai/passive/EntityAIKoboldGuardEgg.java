package com.lying.variousoddities.entity.ai.passive;

import java.util.EnumSet;
import java.util.List;

import com.lying.variousoddities.api.world.settlement.EnumRoomFunction;
import com.lying.variousoddities.api.world.settlement.Settlement;
import com.lying.variousoddities.api.world.settlement.SettlementRoomBehaviour;
import com.lying.variousoddities.entity.passive.EntityKobold;
import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.world.savedata.SettlementManager;
import com.lying.variousoddities.world.settlement.BoxRoom;
import com.lying.variousoddities.world.settlement.SettlementKobold;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityAIKoboldGuardEgg extends Goal
{
	private final EntityKobold theKobold;
	private final Level theWorld;
	private BlockPos nearestEgg = null;
	
	public EntityAIKoboldGuardEgg(EntityKobold koboldIn)
	{
		theKobold = koboldIn;
		theWorld = koboldIn.getLevel();
        setFlags(EnumSet.of(Goal.Flag.MOVE));
	}
	
	public boolean canUse()
	{
		if(!theKobold.isHatcheryGuardian() || theKobold.getTarget() != null)
			return false;
		
		SettlementManager manager = SettlementManager.get(theWorld);
		BoxRoom closestRoom = null;
		double closestDist = Double.MAX_VALUE;
		for(Settlement nest : manager.getSettlementsOfType(SettlementKobold.TYPE_NAME))
			for(BoxRoom room : nest.getRoomsOfType(EnumRoomFunction.NEST))
			{
				BlockPos core = room.getCore();
				double dist = theKobold.distanceToSqr(new Vec3(core.getX() + 0.5D, core.getY() + 0.5D, core.getZ() + 0.5D));
				if(dist < closestDist)
				{
					closestRoom = room;
					closestDist = dist;
				}
			}
		if(closestRoom == null)
			return false;
		
		if(!closestRoom.contains(theKobold.blockPosition()))
			nearestEgg = closestRoom.getCore();
		else
		{
			nearestEgg = null;
			List<BlockPos> eggs = SettlementRoomBehaviour.findAllBlock(closestRoom, theWorld, VOBlocks.EGG_KOBOLD);
			if(eggs.isEmpty())
				return false;
			
			nearestEgg = eggs.get(theKobold.getRandom().nextInt(eggs.size()));
		}
		double eggDist = theKobold.distanceToSqr(new Vec3(nearestEgg.getX() + 0.5D, nearestEgg.getY() + 0.5D, nearestEgg.getZ() + 0.5D));
		
		double distMax = 30D;
		double distMin = 2D;
		return (eggDist < (distMax * distMax) && eggDist > (distMin * distMin)) && theKobold.getRandom().nextInt(Reference.Values.TICKS_PER_SECOND * 15) == 0;
	}
	
	public void start()
	{
		theKobold.getNavigation().stop();
		theKobold.getNavigation().moveTo(nearestEgg.getX(), nearestEgg.getY(), nearestEgg.getZ(), 1.0D);
	}
}
