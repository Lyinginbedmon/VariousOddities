package com.lying.variousoddities.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockEggBase extends VOFallingBlock
{
	public static final VoxelShape SHAPE_SMALL = Block.box(3.04D, 0.0D, 3.04D, 12.96D, 12.8D, 12.96D);
	
	private final VoxelShape shape;
	
	public BlockEggBase(VoxelShape shapeIn, Properties properties)
	{
		super(properties.noOcclusion());
		this.shape = shapeIn;
	}
	
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return this.shape;
	}
	
	public RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.MODEL;
	}
	
	public boolean isTransparent(BlockState blockState){ return true; }
	
	public boolean propagatesSkylightDown(BlockState state, Level reader, BlockPos pos)
	{
		return true;
	}
}
