package com.lying.variousoddities.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;

public class VOBlockRotated extends HorizontalBlock
{
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	
	protected VOBlockRotated(Properties builder)
	{
		super(builder);
		this.setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
	}
    
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
    	builder.add(FACING);
    }
    
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
    	return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing());
    }
}
