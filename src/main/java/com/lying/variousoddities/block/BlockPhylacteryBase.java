package com.lying.variousoddities.block;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockPhylacteryBase extends VOBlock
{
	public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);
	protected static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 14.0D, 13.0D);
	
	public BlockPhylacteryBase(BlockBehaviour.Properties properties)
	{
		super(properties.notSolid().setOpaque(VOBlock::isntSolid).hardnessAndResistance(25.0F, 1200.0F));
		this.registerDefaultState(defaultBlockState().setValue(POWER, 0));
	}
	
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return SHAPE;
	}
	
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos)
	{
		return canSupportCenter(worldIn, pos.below(), Direction.UP);
	}
	
	public RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.MODEL;
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
	public void attack(BlockState state, Level worldIn, BlockPos pos, Player player)
	{
		powerBlock(state, worldIn, pos, 15);
		super.attack(state, worldIn, pos, player);
	}
	
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handin, BlockHitResult hit)
	{
		if(state.getValue(POWER) > 5)
			return InteractionResult.CONSUME;
		
		powerBlock(state, worldIn, pos, 5);
		return InteractionResult.sidedSuccess(worldIn.isClientSide);
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
	
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(POWER);
	}
}
