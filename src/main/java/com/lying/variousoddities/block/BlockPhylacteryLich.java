package com.lying.variousoddities.block;

import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.init.VOBlockEntities;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.tileentity.TileEntityPhylactery;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.world.ForgeChunkManager;

public class BlockPhylacteryLich extends BlockPhylacteryBase implements EntityBlock
{
	public BlockPhylacteryLich(BlockBehaviour.Properties properties)
	{
		super(properties);
	}
	
	public TileEntityPhylactery newBlockEntity(BlockPos pos, BlockState state){ return new TileEntityPhylactery(pos, state); }
    
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level worldIn, BlockState state, BlockEntityType<T> typeIn)
	{
		return VOBlock.createTickerHelper(typeIn, VOBlockEntities.PHYLACTERY, worldIn.isClientSide() ? null : TileEntityPhylactery::serverTick);
	}
	
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		super.setPlacedBy(worldIn, pos, state, placer, stack);
		BlockEntity tile = worldIn.getBlockEntity(pos);
		if(tile instanceof TileEntityPhylactery)
		{
			if(worldIn.isClientSide) return;
			
			ChunkPos chunk = worldIn.getChunk(pos).getPos();
			ServerLevel world = (ServerLevel)worldIn;
			ForgeChunkManager.forceChunk(world, Reference.ModInfo.MOD_ID, pos, chunk.x, chunk.z, true, true);
		}
	}
	
	public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player)
	{
		BlockEntity tile = worldIn.getBlockEntity(pos);
		if(tile instanceof TileEntityPhylactery)
		{
			if(!worldIn.isClientSide)
			{
				ChunkPos chunk = worldIn.getChunk(pos).getPos();
				ServerLevel world = (ServerLevel)worldIn;
				ForgeChunkManager.forceChunk(world, Reference.ModInfo.MOD_ID, pos, chunk.x, chunk.z, false, true);
			}
			
			TileEntityPhylactery phylacteryTile = (TileEntityPhylactery)tile;
			if(!worldIn.isClientSide && player.isCreative())
			{
				ItemStack itemstack = new ItemStack(VOBlocks.PHYLACTERY.get());
				phylacteryTile.saveToItem(itemstack);
				
				// TODO Most probably broken
				CompoundTag tileData = itemstack.getOrCreateTagElement("BlockEntityTag");
				tileData.remove("TimeSincePlaced");
				itemstack.addTagElement("BlockEntityTag", tileData);
				
				ItemEntity itementity = new ItemEntity(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemstack);
				itementity.setDefaultPickUpDelay();
				worldIn.addFreshEntity(itementity);
			}
		}
		
		super.playerWillDestroy(worldIn, pos, state, player);
	}
}
