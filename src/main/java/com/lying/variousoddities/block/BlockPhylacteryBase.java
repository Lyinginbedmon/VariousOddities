package com.lying.variousoddities.block;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockPhylacteryBase extends VOBlock
{
	public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 14.0D, 13.0D);
	
	public BlockPhylacteryBase(BlockBehaviour.Properties properties)
	{
		super(properties.notSolid().setOpaque(VOBlock::isntSolid).hardnessAndResistance(25.0F, 1200.0F));
		this.setDefaultState(getDefaultState().with(POWER, 0));
	}
	
	public VoxelShape getShape(BlockState state, Level worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}
	
	public boolean isValidPosition(BlockState state, Level worldIn, BlockPos pos)
	{
		return hasEnoughSolidSide(worldIn, pos.below(), Direction.UP);
	}
	
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
	
	public boolean canProvidePower(BlockState state) { return true; }
	
	public boolean hasComparatorInputOverride(BlockState state) { return true; }
	
	public int getStrongPower(BlockState blockState, Level blockAccess, BlockPos pos, Direction side)
	{
		return blockState.getValue(POWER);
	}
	
	public int getWeakPower(BlockState blockState, Level blockAccess, BlockPos pos, Direction side)
	{
		return getStrongPower(blockState, blockAccess, pos, side);
	}
	
	public int getComparatorInputOverride(BlockState blockState, Level worldIn, BlockPos pos)
	{
		return getStrongPower(blockState, worldIn, pos, Direction.UP);
	}
	
	public void powerBlock(BlockState state, Level world, BlockPos pos, int power)
	{
		world.setBlock(pos, state.setValue(POWER, power), 3);
		world.updateNeighborsAt(pos, this);
		if(power != 0)
			world.scheduleTick(pos, state.getBlock(), 20);
	}
	
	@SuppressWarnings("deprecation")
	public void onBlockClicked(BlockState state, Level worldIn, BlockPos pos, Player player)
	{
		powerBlock(state, worldIn, pos, 15);
		super.onBlockClicked(state, worldIn, pos, player);
	}
	
	public ActionResultType onBlockActivated(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handin, BlockHitResult hit)
	{
		if(state.getValue(POWER) > 5)
			return ActionResultType.CONSUME;
		
		powerBlock(state, worldIn, pos, 5);
		return ActionResultType.func_233537_a_(worldIn.isClientSide);
	}
	
	@SuppressWarnings("deprecation")
	public void onReplaced(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if(!isMoving && !state.is(newState.getBlock()))
		{
			if(state.getValue(POWER) > 0)
				worldIn.updateNeighborsAt(pos, this);
			
			super.onReplaced(state, worldIn, pos, newState, isMoving);
		}
	}
	
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand)
	{
		if(state.getValue(POWER) > 0)
			powerBlock(state, worldIn, pos, state.getValue(POWER) - 1);
	}
	
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(POWER);
	}
}
