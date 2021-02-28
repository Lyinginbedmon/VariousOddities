package com.lying.variousoddities.block;

import java.util.List;

import javax.annotation.Nullable;

import com.lying.variousoddities.init.VOItems;
import com.lying.variousoddities.tileentity.TileEntityDraftingTable;
import com.lying.variousoddities.world.settlement.BoxRoom;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class BlockDraftingTable extends VOBlockRotated implements ITileEntityProvider
{
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
	
	public BlockDraftingTable(AbstractBlock.Properties properties)
	{
		super(properties.notSolid().setOpaque(VOBlock::isntSolid));
	}
	
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}
	
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		switch(facing)
		{
			case DOWN:
				if(isValidPosition(stateIn, worldIn, currentPos))
					return stateIn;
				else
				{
					dropAsItem((World)worldIn, currentPos);
					return Blocks.AIR.getDefaultState();
				}
			default:
				return stateIn;
		}
	}
	
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
	{
		return hasEnoughSolidSide(worldIn, pos.down(), Direction.UP);
	}
	
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if(worldIn.isRemote)
			return ActionResultType.SUCCESS;
		else
		{
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if(tileentity instanceof TileEntityDraftingTable)
				player.openContainer((INamedContainerProvider)tileentity);
			return ActionResultType.CONSUME;
		}
	}
	
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
	
	public TileEntity createNewTileEntity(IBlockReader reader)
	{
		return new TileEntityDraftingTable();
	}
	
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)
	{
		if(!player.isCreative())
			dropAsItem(worldIn, pos);
		super.onBlockHarvested(worldIn, pos, state, player);
	}
	
	public void dropAsItem(World worldIn, BlockPos pos)
	{
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if(tileentity instanceof TileEntityDraftingTable)
		{
			TileEntityDraftingTable tableTile = (TileEntityDraftingTable)tileentity;
			if(!worldIn.isRemote)
			{
				ItemStack itemstack = VOItems.DRAFTING_TABLE.getDefaultInstance();
				CompoundNBT compoundnbt = tableTile.saveToNbt(new CompoundNBT());
				if(!compoundnbt.isEmpty())
					itemstack.setTagInfo("BlockEntityTag", compoundnbt);
				
				ItemEntity itementity = new ItemEntity(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemstack);
				itementity.setDefaultPickupDelay();
				worldIn.addEntity(itementity);
			}
		}
	}
	
	public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced)
	{
		if(stack.hasTag() && !stack.getTag().isEmpty())
		{
			CompoundNBT stackData = stack.getTag();
			if(stackData.contains("BlockEntityTag",10))
			{
				CompoundNBT blockData = stackData.getCompound("BlockEntityTag");
				if(blockData.contains("Room"))
				{
		 			BoxRoom room = new BoxRoom(blockData.getCompound("Room"));
		 			if(room.hasCustomName())
		 				tooltip.add("Name: "+room.getName());
		 			if(room.hasFunction())
		 				tooltip.add("Function: "+room.getFunction().name().toLowerCase());
		 			
		 			BlockPos min = room.min();
		 			tooltip.add("Min: ["+min.getX()+", "+min.getY()+", "+min.getZ()+"]");
		 			BlockPos max = room.max();
		 			tooltip.add("Max: ["+max.getX()+", "+max.getY()+", "+max.getZ()+"]");
		 			BlockPos size = max.subtract(min);
		 			tooltip.add("Size: ["+size.getX()+", "+size.getY()+", "+size.getZ()+"]");
				}
			}
		}
	}
}
