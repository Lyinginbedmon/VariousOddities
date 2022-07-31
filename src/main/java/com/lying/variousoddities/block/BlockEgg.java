package com.lying.variousoddities.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BlockEgg<T extends BlockEntity> extends BlockEggBase implements BlockEntitySupplier<T>
{
	public BlockEgg(VoxelShape shapeIn, Properties properties)
	{
		super(shapeIn, properties);
	}
	
	public abstract void onHatch(BlockPos pos, Level world);
	
	@SuppressWarnings("hiding")
	@Nullable
	public abstract <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level worldIn, BlockState state, BlockEntityType<T> typeIn);
}
