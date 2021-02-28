package com.lying.variousoddities.tileentity;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public abstract class VOTileEntity extends TileEntity
{
	public VOTileEntity(TileEntityType<?> type)
	{
		super(type);
	}
	
	@Nonnull
	public CompoundNBT write(CompoundNBT tag)
	{
		CompoundNBT compound = super.write(tag);
		writePacketNBT(compound);
		return compound;
	}
	
	@Nonnull
	public final CompoundNBT getUpdateTag()
	{
		return write(new CompoundNBT());
	}
	
	public void read(BlockState state, CompoundNBT compound)
	{
		super.read(state, compound);
		readPacketNBT(compound);
	}
	
	public void writePacketNBT(CompoundNBT compound){ }
	
	public void readPacketNBT(CompoundNBT compound){ }
	
	public final SUpdateTileEntityPacket getUpdatePacket()
	{
		CompoundNBT tag = new CompoundNBT();
		writePacketNBT(tag);
		return new SUpdateTileEntityPacket(pos, -999, tag);
	}
	
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet)
	{
		super.onDataPacket(net, packet);
		readPacketNBT(packet.getNbtCompound());
	}
}
