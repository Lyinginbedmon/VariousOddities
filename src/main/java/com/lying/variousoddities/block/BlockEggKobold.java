package com.lying.variousoddities.block;

import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.init.VOTileEntities;
import com.lying.variousoddities.tileentity.TileEntityEgg;
import com.lying.variousoddities.tileentity.TileEntityEggKobold;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEggKobold extends BlockEgg<TileEntityEggKobold>
{
	public BlockEggKobold(Properties properties)
	{
		super(BlockEggBase.SHAPE_SMALL, properties);
	}
    
	public TileEntityEggKobold create(BlockPos pos, BlockState state){ return new TileEntityEggKobold(pos, state); }
	
    public void onHatch(BlockPos pos, Level worldIn)
    {
    	worldIn.setBlockAndUpdate(pos, VOBlocks.LAYER_SCALE.defaultBlockState());
    }
    
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level worldIn, BlockState state, BlockEntityType<T> typeIn)
	{
		return VOBlock.createTickerHelper(typeIn, VOTileEntities.EGG_KOBOLD, worldIn.isClientSide() ? TileEntityEgg::clientTick : TileEntityEgg::serverTick);
	}
}
