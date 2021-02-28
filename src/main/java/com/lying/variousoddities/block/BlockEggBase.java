package com.lying.variousoddities.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class BlockEggBase extends VOFallingBlock
{
	public static final VoxelShape SHAPE_SMALL = Block.makeCuboidShape(3.04D, 0.0D, 3.04D, 12.96D, 12.8D, 12.96D);
	
	private final VoxelShape shape;
	
	public BlockEggBase(VoxelShape shapeIn, Properties properties)
	{
		super(properties.notSolid().setOpaque(VOBlock::isntSolid));
		this.shape = shapeIn;
	}
	
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return this.shape;
	}
	
	/**
	 * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
	 * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
	 * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
	 */
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
	
	public boolean isTransparent(BlockState blockState){ return true; }
	
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos)
	{
		return true;
	}
}
