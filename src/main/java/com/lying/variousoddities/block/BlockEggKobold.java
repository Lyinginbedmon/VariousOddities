package com.lying.variousoddities.block;

import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.tileentity.TileEntityEggKobold;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEggKobold extends BlockEgg<TileEntityEggKobold>
{
	public BlockEggKobold(Properties properties)
	{
		super(BlockEggBase.SHAPE_SMALL, properties);
	}
    
	public TileEntityEggKobold create(BlockPos p_155268_, BlockState p_155269_){ return new TileEntityEggKobold(); }
	
    public void onHatch(BlockPos pos, Level worldIn)
    {
    	worldIn.setBlockAndUpdate(pos, VOBlocks.LAYER_SCALE.defaultBlockState());
    }
}
