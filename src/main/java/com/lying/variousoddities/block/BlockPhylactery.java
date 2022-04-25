package com.lying.variousoddities.block;

import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.tileentity.TileEntityPhylactery;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class BlockPhylactery extends VOBlock implements ITileEntityProvider
{
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 14.0D, 13.0D);
	
	public BlockPhylactery(AbstractBlock.Properties properties)
	{
		super(properties.notSolid().setOpaque(VOBlock::isntSolid));
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
	
	public TileEntity createNewTileEntity(IBlockReader reader)
	{
		return new TileEntityPhylactery();
	}
	
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)
	{
		TileEntity tile = worldIn.getTileEntity(pos);
		if(tile instanceof TileEntityPhylactery)
		{
			TileEntityPhylactery phylacteryTile = (TileEntityPhylactery)tile;
			if(!worldIn.isRemote && player.isCreative())
			{
				ItemStack itemstack = new ItemStack(VOBlocks.PHYLACTERY);
				CompoundNBT tileData = phylacteryTile.write(new CompoundNBT());
				tileData.remove("TimeSincePlaced");
				if(!tileData.isEmpty())
					itemstack.setTagInfo("BlockEntityTag", tileData);
				
				ItemEntity itementity = new ItemEntity(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemstack);
				itementity.setDefaultPickupDelay();
				worldIn.addEntity(itementity);
			}
		}
		
		super.onBlockHarvested(worldIn, pos, state, player);
	}
}
