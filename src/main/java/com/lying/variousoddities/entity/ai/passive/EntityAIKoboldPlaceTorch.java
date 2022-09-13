package com.lying.variousoddities.entity.ai.passive;

import com.lying.variousoddities.entity.passive.EntityKobold;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class EntityAIKoboldPlaceTorch extends Goal
{
	private final EntityKobold theKobold;
	private final Level theWorld;
	
	public EntityAIKoboldPlaceTorch(EntityKobold koboldIn)
	{
		theKobold = koboldIn;
		theWorld = koboldIn.getLevel();
	}
	
	public boolean canUse()
	{
		if(theKobold.isHatcheryGuardian())
		{
			if(!theWorld.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))
				return false;
			
			if(!(!theKobold.getMainHandItem().isEmpty() && theKobold.getOffhandItem().getItem() == Blocks.TORCH.asItem()))
				return false;
			
			return theKobold.getRandom().nextInt(Reference.Values.TICKS_PER_MINUTE * 5) == 0 && theKobold.getTarget() == null;
		}
		
		return false;
	}
	
	public boolean canContinueToUse(){ return false; }
	
	public void start()
	{
		BlockPos targetBlock = theKobold.blockPosition();
		if(theWorld.getLightEmission(targetBlock) <= 8)
			if(theWorld.getBlockState(targetBlock).getMaterial().isReplaceable() && !theWorld.canSeeSky(targetBlock))
				if(Block.canSupportCenter(theWorld, targetBlock.below(), Direction.UP))
					theWorld.setBlockAndUpdate(targetBlock, Blocks.TORCH.defaultBlockState());
	}
}
