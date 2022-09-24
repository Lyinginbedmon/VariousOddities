package com.lying.variousoddities.block;

import java.util.List;

import javax.annotation.Nullable;

import com.lying.variousoddities.api.world.settlement.EnumRoomFunction;
import com.lying.variousoddities.client.gui.GuiDraftingTable;
import com.lying.variousoddities.init.VOItems;
import com.lying.variousoddities.init.VOBlockEntities;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.tileentity.TileEntityDraftingTable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings("deprecation")
public class BlockDraftingTable extends VOBlockRotated implements BlockEntitySupplier<TileEntityDraftingTable>
{
	protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
	
	public BlockDraftingTable(BlockBehaviour.Properties properties)
	{
		super(properties.noOcclusion().isViewBlocking(VOBlock::isntSolid));
	}
	
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return SHAPE;
	}
	
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, Level worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		if(facing == Direction.DOWN && !canSurvive(stateIn, worldIn, currentPos))
		{
			dropAsItem((Level)worldIn, currentPos);
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}
	
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos)
	{
		return worldIn.getBlockState(pos).isFaceSturdy(worldIn, pos.below(), Direction.UP);
	}
	
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit)
	{
		BlockEntity tileentity = worldIn.getBlockEntity(pos);
		if(tileentity.getType() == VOBlockEntities.TABLE_DRAFTING)
		{
			TileEntityDraftingTable table = (TileEntityDraftingTable)tileentity;
			if(worldIn.isClientSide)
				GuiDraftingTable.open(table);
			else
				table.setChanged();
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.CONSUME;
	}
	
	public RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.MODEL;
	}
	
	public TileEntityDraftingTable create(BlockPos pos, BlockState state)
	{
		return new TileEntityDraftingTable(pos, state);
	}
	
	public void dropAsItem(Level worldIn, BlockPos pos)
	{
		BlockEntity tile = worldIn.getBlockEntity(pos);
		if(tile instanceof TileEntityDraftingTable && !worldIn.isClientSide)
		{
			ItemStack stack = getItem(worldIn, pos, worldIn.getBlockState(pos));
			ItemEntity entity = new ItemEntity(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, stack);
			entity.setDefaultPickUpDelay();
			worldIn.addFreshEntity(entity);
		}
	}
	
	public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player)
	{
		BlockEntity tile = worldIn.getBlockEntity(pos);
		if(tile.getType() == VOBlockEntities.TABLE_DRAFTING)
		{
			if(player != null && player.isCreative())
				return;
			
			dropAsItem(worldIn, pos);
		}
		super.playerWillDestroy(worldIn, pos, state, player);
	}
	
	public ItemStack getItem(Level worldIn, BlockPos pos, BlockState state)
	{
		ItemStack stack = VOItems.DRAFTING_TABLE.getDefaultInstance();
		TileEntityDraftingTable tile = (TileEntityDraftingTable)worldIn.getBlockEntity(pos);
		if(tile.bitMask() > 0)
			tile.saveToItem(stack);
		return stack;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn)
	{
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if(stack.hasTag() && !stack.getTag().isEmpty())
		{
			CompoundTag blockData = stack.getTagElement("BlockEntityTag");
			
			int lock = blockData.contains("Locked", 3) ? blockData.getInt("Locked") : 0;
			
			if(blockData.contains("CustomName", 8) && !TileEntityDraftingTable.canAlter(8, lock))
 				tooltip.add(setFormatting(Component.translatable("gui."+Reference.ModInfo.MOD_ID+".drafting_table.tooltip_1", blockData.getString("CustomName").replace(" ", "_")), ChatFormatting.GRAY));
			
			if(blockData.contains("Function", 8) && !TileEntityDraftingTable.canAlter(4, lock))
 				tooltip.add(setFormatting(Component.translatable("gui."+Reference.ModInfo.MOD_ID+".drafting_table.tooltip_2", EnumRoomFunction.fromString(blockData.getString("Function")).getName()), ChatFormatting.GRAY));
			
			BlockPos min = blockData.contains("Start", 10) ? NbtUtils.readBlockPos(blockData.getCompound("Start")) : null;
			BlockPos max = blockData.contains("End", 10) ? NbtUtils.readBlockPos(blockData.getCompound("End")) : null;
			
			if(min != null && max != null)
			{
	 			BlockPos size = max.subtract(min);
	 			tooltip.add(setFormatting(Component.translatable("gui."+Reference.ModInfo.MOD_ID+".drafting_table.tooltip_5", size.getX(), size.getY(), size.getZ()), ChatFormatting.GRAY));
			}
			
			if(min != null && !TileEntityDraftingTable.canAlter(1, lock))
	 			tooltip.add(Component.literal("  ").append(setFormatting(Component.translatable("gui."+Reference.ModInfo.MOD_ID+".drafting_table.tooltip_3", min.getX(), min.getY(), min.getZ()), ChatFormatting.DARK_GRAY, true)));
			
			if(max != null && !TileEntityDraftingTable.canAlter(2, lock))
	 			tooltip.add(Component.literal("  ").append(setFormatting(Component.translatable("gui."+Reference.ModInfo.MOD_ID+".drafting_table.tooltip_4", max.getX(), max.getY(), max.getZ()), ChatFormatting.DARK_GRAY, true)));
			
			if(lock == 15)
				tooltip.add(setFormatting(Component.translatable("gui."+Reference.ModInfo.MOD_ID+".drafting_table.tooltip_6"), ChatFormatting.GOLD));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	private static Component setFormatting(MutableComponent textComponent, ChatFormatting colour, boolean italic)
	{
		return textComponent.withStyle((style) -> { return style.applyFormat(colour).withItalic(italic); });
	}
	
	@OnlyIn(Dist.CLIENT)
	private static Component setFormatting(MutableComponent textComponent, ChatFormatting colour)
	{
		return setFormatting(textComponent, colour, false);
	}
	
	@OnlyIn(Dist.CLIENT)
	private static Component setFormatting(MutableComponent textComponent)
	{
		return setFormatting(textComponent, ChatFormatting.DARK_GRAY);
	}
}
