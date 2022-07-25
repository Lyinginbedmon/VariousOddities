package com.lying.variousoddities.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public abstract class BlockEgg extends BlockEggBase implements ITileEntityProvider
{
	public BlockEgg(VoxelShape shapeIn, Properties properties)
	{
		super(shapeIn, properties);
	}
	
	public abstract void onHatch(BlockPos pos, Level world);
}
