package com.lying.variousoddities.entity.ai.passive;

import com.lying.variousoddities.entity.AbstractCrab;

import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityAICrabRave extends Goal
{
	private final World theWorld;
	private final AbstractCrab theCrab;
	
	public EntityAICrabRave(AbstractCrab crabIn)
	{
		theWorld = crabIn.getEntityWorld();
		theCrab = crabIn;
	}
	
	public boolean shouldExecute()
	{
		if(theCrab.getAttackTarget() != null)
			return false;
		
		theCrab.setPartying(nearestJukebox());
		return nearestJukebox() != null;
	}
	
	private BlockPos nearestJukebox()
	{
		BlockPos nearest = null;
		double dist = Double.MAX_VALUE;
		BlockPos origin = theCrab.getPosition();
		int r = 6;
		for(int x=-r; x<r; x++)
			for(int y=-r; y<r; y++)
				for(int z=-r; z<r; z++)
				{
					BlockPos pos = origin.add(x, y, z);
					if(theWorld.getBlockState(pos).getBlock() == Blocks.JUKEBOX && theWorld.getBlockState(pos).get(JukeboxBlock.HAS_RECORD))
					{
						if(origin.distanceSq(pos) < dist)
						{
							nearest = pos;
							dist = origin.distanceSq(pos);
						}
					}
				}
		
		return nearest;
	}
}
