package com.lying.variousoddities.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public abstract class BlockEgg extends BlockEggBase implements ITileEntityProvider
{
	public BlockEgg(VoxelShape shapeIn, Properties properties)
	{
		super(shapeIn, properties);
	}
	
	public abstract void onHatch(BlockPos pos, World world);
}
