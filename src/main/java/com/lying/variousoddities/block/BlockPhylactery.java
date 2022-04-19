package com.lying.variousoddities.block;

import java.util.List;

import javax.annotation.Nullable;

import com.lying.variousoddities.init.VOItems;
import com.lying.variousoddities.init.VOTileEntities;
import com.lying.variousoddities.tileentity.TileEntityDraftingTable;
import com.lying.variousoddities.tileentity.TileEntityPhylactery;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
	
	public void dropAsItem(World worldIn, BlockPos pos)
	{
		TileEntity tile = worldIn.getTileEntity(pos);
		if(tile instanceof TileEntityPhylactery && !worldIn.isRemote)
		{
			ItemStack stack = getItem(worldIn, pos, worldIn.getBlockState(pos));
			ItemEntity entity = new ItemEntity(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, stack);
			entity.setDefaultPickupDelay();
			worldIn.addEntity(entity);
		}
	}
	
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)
	{
		TileEntity tile = worldIn.getTileEntity(pos);
		if(tile.getType() == VOTileEntities.PHYLACTERY)
		{
			if(player != null && player.isCreative())
				return;
			
			dropAsItem(worldIn, pos);
		}
		super.onBlockHarvested(worldIn, pos, state, player);
	}
	
	public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state)
	{
		ItemStack stack = VOItems.DRAFTING_TABLE.getDefaultInstance();
		TileEntityDraftingTable tile = (TileEntityDraftingTable)worldIn.getTileEntity(pos);
		if(tile.bitMask() > 0)
		{
			CompoundNBT data = tile.saveToNbt(new CompoundNBT());
			if(!data.isEmpty())
				stack.setTagInfo("BlockEntityTag", data);
		}
		return stack;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if(stack.hasTag() && !stack.getTag().isEmpty())
		{
			CompoundNBT blockData = stack.getChildTag("BlockEntityTag");
		}
	}
}
