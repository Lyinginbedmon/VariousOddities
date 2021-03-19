package com.lying.variousoddities.block;

import java.util.List;

import javax.annotation.Nullable;

import com.lying.variousoddities.api.world.settlement.EnumRoomFunction;
import com.lying.variousoddities.client.gui.GuiDraftingTable;
import com.lying.variousoddities.init.VOItems;
import com.lying.variousoddities.init.VOTileEntities;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.tileentity.TileEntityDraftingTable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
		if(facing == Direction.DOWN && !isValidPosition(stateIn, worldIn, currentPos))
		{
			dropAsItem((World)worldIn, currentPos);
			return Blocks.AIR.getDefaultState();
		}
		return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}
	
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
	{
		return hasEnoughSolidSide(worldIn, pos.down(), Direction.UP);
	}
	
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if(tileentity.getType() == VOTileEntities.TABLE_DRAFTING)
		{
			TileEntityDraftingTable table = (TileEntityDraftingTable)tileentity;
			if(worldIn.isRemote)
				GuiDraftingTable.open(table);
			else
				table.markDirty();
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.CONSUME;
	}
	
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}
	
	public TileEntity createNewTileEntity(IBlockReader reader)
	{
		return new TileEntityDraftingTable();
	}
	
	public void dropAsItem(World worldIn, BlockPos pos)
	{
		TileEntity tile = worldIn.getTileEntity(pos);
		if(tile instanceof TileEntityDraftingTable && !worldIn.isRemote)
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
		if(tile.getType() == VOTileEntities.TABLE_DRAFTING)
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
			
			int lock = blockData.contains("Locked", 3) ? blockData.getInt("Locked") : 0;
			
			if(blockData.contains("CustomName", 8) && !TileEntityDraftingTable.canAlter(8, lock))
 				tooltip.add(setFormatting(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".drafting_table.tooltip_1", blockData.getString("CustomName").replace(" ", "_")), TextFormatting.GRAY));
			
			if(blockData.contains("Function", 8) && !TileEntityDraftingTable.canAlter(4, lock))
 				tooltip.add(setFormatting(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".drafting_table.tooltip_2", EnumRoomFunction.fromString(blockData.getString("Function")).getName()), TextFormatting.GRAY));
			
			BlockPos min = blockData.contains("Start", 10) ? NBTUtil.readBlockPos(blockData.getCompound("Start")) : null;
			BlockPos max = blockData.contains("End", 10) ? NBTUtil.readBlockPos(blockData.getCompound("End")) : null;
			
			if(min != null && max != null)
			{
	 			BlockPos size = max.subtract(min);
	 			tooltip.add(setFormatting(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".drafting_table.tooltip_5", size.getX(), size.getY(), size.getZ()), TextFormatting.GRAY));
			}
			
			if(min != null && !TileEntityDraftingTable.canAlter(1, lock))
	 			tooltip.add(new StringTextComponent("  ").append(setFormatting(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".drafting_table.tooltip_3", min.getX(), min.getY(), min.getZ()), TextFormatting.DARK_GRAY, true)));
			
			if(max != null && !TileEntityDraftingTable.canAlter(2, lock))
	 			tooltip.add(new StringTextComponent("  ").append(setFormatting(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".drafting_table.tooltip_4", max.getX(), max.getY(), max.getZ()), TextFormatting.DARK_GRAY, true)));
			
			if(lock == 15)
				tooltip.add(setFormatting(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".drafting_table.tooltip_6"), TextFormatting.GOLD));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	private static ITextComponent setFormatting(IFormattableTextComponent textComponent, TextFormatting colour, boolean italic)
	{
		return textComponent.modifyStyle((style) -> { return style.applyFormatting(colour).setItalic(italic); });
	}
	
	@OnlyIn(Dist.CLIENT)
	private static ITextComponent setFormatting(IFormattableTextComponent textComponent, TextFormatting colour)
	{
		return setFormatting(textComponent, colour, false);
	}
	
	@OnlyIn(Dist.CLIENT)
	private static ITextComponent setFormatting(IFormattableTextComponent textComponent)
	{
		return setFormatting(textComponent, TextFormatting.DARK_GRAY);
	}
}
