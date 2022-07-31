package com.lying.variousoddities.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BlockEgg<T extends BlockEntity> extends BlockEggBase implements BlockEntitySupplier<T>
{
	public BlockEgg(VoxelShape shapeIn, Properties properties)
	{
		super(shapeIn, properties);
	}
	
	public abstract void onHatch(BlockPos pos, Level world);
}
