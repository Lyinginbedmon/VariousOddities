package com.lying.variousoddities.entity.ai.passive;

import com.lying.variousoddities.entity.passive.EntityKobold;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class EntityAIKoboldPlaceTorch extends Goal
{
	private final EntityKobold theKobold;
	private final World theWorld;
	
	public EntityAIKoboldPlaceTorch(EntityKobold koboldIn)
	{
		theKobold = koboldIn;
		theWorld = koboldIn.getEntityWorld();
	}
	
	public boolean shouldExecute()
	{
		if(theKobold.isHatcheryGuardian())
		{
			if(!theWorld.getGameRules().getBoolean(GameRules.MOB_GRIEFING))
				return false;
			
			if(!(!theKobold.getHeldItemOffhand().isEmpty() && theKobold.getHeldItemOffhand().getItem() == Items.TORCH))
				return false;
			
			return theKobold.getRNG().nextInt(Reference.Values.TICKS_PER_MINUTE * 5) == 0 && theKobold.getAttackTarget() == null;
		}
		
		return false;
	}
	
	public boolean shouldContinueExecuting(){ return false; }
	
	public void startExecuting()
	{
		BlockPos targetBlock = theKobold.getPosition();
		if(theWorld.getLight(targetBlock) <= 8)
			if(theWorld.getBlockState(targetBlock).getMaterial().isReplaceable() && !theWorld.canBlockSeeSky(targetBlock))
				if(Block.hasEnoughSolidSide(theWorld, targetBlock.down(), Direction.UP))
					theWorld.setBlockState(targetBlock, Blocks.TORCH.getDefaultState());
	}
}
