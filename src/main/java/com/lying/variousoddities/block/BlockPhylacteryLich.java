package com.lying.variousoddities.block;

import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.tileentity.TileEntityPhylactery;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.ForgeChunkManager;

@SuppressWarnings("deprecation")
public class BlockPhylacteryLich extends BlockPhylacteryBase implements ITileEntityProvider
{
	public BlockPhylacteryLich(AbstractBlock.Properties properties)
	{
		super(properties);
	}
	
	public TileEntity createNewTileEntity(IBlockReader reader)
	{
		return new TileEntityPhylactery();
	}
	
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		TileEntity tile = worldIn.getTileEntity(pos);
		if(tile instanceof TileEntityPhylactery)
		{
			if(worldIn.isRemote) return;
			
			ChunkPos chunk = worldIn.getChunk(pos).getPos();
			ServerWorld world = (ServerWorld)worldIn;
			ForgeChunkManager.forceChunk(world, Reference.ModInfo.MOD_ID, pos, chunk.x, chunk.z, true, true);
		}
	}
	
	public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player)
	{
		TileEntity tile = worldIn.getTileEntity(pos);
		if(tile instanceof TileEntityPhylactery)
		{
			if(!worldIn.isRemote)
			{
				ChunkPos chunk = worldIn.getChunk(pos).getPos();
				ServerWorld world = (ServerWorld)worldIn;
				ForgeChunkManager.forceChunk(world, Reference.ModInfo.MOD_ID, pos, chunk.x, chunk.z, false, true);
			}
			
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
