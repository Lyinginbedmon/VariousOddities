package com.lying.variousoddities.tileentity;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class VOTileEntity extends BlockEntity
{
	public VOTileEntity(BlockEntityType<?> type)
	{
		super(type);
	}
	
	@Nonnull
	public CompoundTag write(CompoundTag tag)
	{
		CompoundTag compound = super.write(tag);
		writePacketNBT(compound);
		return compound;
	}
	
	@Nonnull
	public final CompoundTag getUpdateTag()
	{
		return write(new CompoundTag());
	}
	
	public void read(BlockState state, CompoundTag compound)
	{
		super.read(state, compound);
		readPacketNBT(compound);
	}
	
	public void writePacketNBT(CompoundTag compound){ }
	
	public void readPacketNBT(CompoundTag compound){ }
	
	public final SUpdateTileEntityPacket getUpdatePacket()
	{
		CompoundTag tag = new CompoundTag();
		writePacketNBT(tag);
		return new SUpdateTileEntityPacket(pos, -999, tag);
	}
	
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet)
	{
		super.onDataPacket(net, packet);
		readPacketNBT(packet.getNbtCompound());
	}
}
