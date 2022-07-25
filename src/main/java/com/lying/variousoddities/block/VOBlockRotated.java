package com.lying.variousoddities.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class VOBlockRotated extends HorizontalDirectionalBlock
{
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	
	protected VOBlockRotated(Properties builder)
	{
		super(builder);
		this.registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
	}
    
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
    	builder.add(FACING);
    }
    
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
    	return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }
}
