package com.lying.variousoddities.block;

import java.util.Random;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class BlockPhylacteryBase extends VOBlock
{
	public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 14.0D, 13.0D);
	
	public BlockPhylacteryBase(AbstractBlock.Properties properties)
	{
		super(properties.notSolid().setOpaque(VOBlock::isntSolid).hardnessAndResistance(25.0F, 1200.0F));
		this.setDefaultState(getDefaultState().with(POWER, 0));
	}
	
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}
	
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
	{
		return hasEnoughSolidSide(worldIn, pos.down(), Direction.UP);
	}
	
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
	
	public boolean canProvidePower(BlockState state) { return true; }
	
	public boolean hasComparatorInputOverride(BlockState state) { return true; }
	
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
	{
		return blockState.get(POWER);
	}
	
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
	{
		return getStrongPower(blockState, blockAccess, pos, side);
	}
	
	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos)
	{
		return getStrongPower(blockState, worldIn, pos, Direction.UP);
	}
	
	public void powerBlock(BlockState state, World world, BlockPos pos, int power)
	{
		world.setBlockState(pos, state.with(POWER, power), 3);
		world.notifyNeighborsOfStateChange(pos, this);
		if(power != 0)
			world.getPendingBlockTicks().scheduleTick(pos, this, 20);
	}
	
	@SuppressWarnings("deprecation")
	public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player)
	{
		powerBlock(state, worldIn, pos, 15);
		super.onBlockClicked(state, worldIn, pos, player);
	}
	
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handin, BlockRayTraceResult hit)
	{
		if(state.get(POWER) > 5)
			return ActionResultType.CONSUME;
		
		powerBlock(state, worldIn, pos, 5);
		return ActionResultType.func_233537_a_(worldIn.isRemote);
	}
	
	@SuppressWarnings("deprecation")
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if(!isMoving && !state.isIn(newState.getBlock()))
		{
			if(state.get(POWER) > 0)
				worldIn.notifyNeighborsOfStateChange(pos, this);
			
			super.onReplaced(state, worldIn, pos, newState, isMoving);
		}
	}
	
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand)
	{
		if(state.get(POWER) > 0)
			powerBlock(state, worldIn, pos, state.get(POWER) - 1);
	}
	
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(POWER);
	}
}
