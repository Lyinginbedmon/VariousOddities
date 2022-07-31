package com.lying.variousoddities.tileentity;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class VOTileEntity extends BlockEntity
{
	public VOTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}
	
	@Nonnull
	public void saveAdditional(CompoundTag tag)
	{
		super.saveAdditional(tag);
		writePacketNBT(tag);
	}
	
	@Nonnull
	public final CompoundTag getUpdateTag()
	{
		CompoundTag compound = new CompoundTag();
		saveAdditional(compound);
		return compound;
	}
	
	public void load(CompoundTag compound)
	{
		super.load(compound);
		readPacketNBT(compound);
	}
	
	public void writePacketNBT(CompoundTag compound){ }
	
	public void readPacketNBT(CompoundTag compound){ }
	
	public final ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
	
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
	{
		super.onDataPacket(net, pkt);
		readPacketNBT(pkt.getTag());
	}
}
