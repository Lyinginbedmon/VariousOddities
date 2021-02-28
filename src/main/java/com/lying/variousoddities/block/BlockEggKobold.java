package com.lying.variousoddities.block;

import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.tileentity.TileEntityEggKobold;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockEggKobold extends BlockEgg
{
	public BlockEggKobold(Properties properties)
	{
		super(BlockEggBase.SHAPE_SMALL, properties);
	}
	
	public TileEntity createNewTileEntity(IBlockReader worldIn)
	{
		return new TileEntityEggKobold();
	}
	
    public void onHatch(BlockPos pos, World worldIn)
    {
    	worldIn.setBlockState(pos, VOBlocks.LAYER_SCALE.getDefaultState());
    }
}
