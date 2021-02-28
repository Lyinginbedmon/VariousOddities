package com.lying.variousoddities.network;

import java.io.IOException;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class PacketTileUpdate
{
	CompoundNBT tileData;
	BlockPos tilePos;
	
	public PacketTileUpdate(){ }
	public PacketTileUpdate(TileEntity tile)
	{
		this(tile.write(new CompoundNBT()), tile.getPos());
	}
	public PacketTileUpdate(CompoundNBT data, BlockPos pos)
	{
		tileData = data;
		tilePos = pos;
	}
	
	protected void decode(PacketBuffer par1Buffer) throws IOException
	{
		tilePos = new BlockPos(par1Buffer.readInt(), par1Buffer.readInt(), par1Buffer.readInt());
		tileData = par1Buffer.readCompoundTag();
	}
	
	protected void encode(PacketBuffer par1Buffer) throws IOException
	{
		par1Buffer.writeInt(tilePos.getX());
		par1Buffer.writeInt(tilePos.getY());
		par1Buffer.writeInt(tilePos.getZ());
		par1Buffer.writeCompoundTag(tileData);
	}
	
//	public void process(EntityPlayer par1Player, Side par2Side)
//	{
//		TileEntity tile = par1Player.getEntityWorld().getTileEntity(tilePos);
//		if(tile != null)
//		{
//			tile.readFromNBT(tileData);
//			tile.validate();
//			par1Player.getEntityWorld().setTileEntity(tilePos, tile);
//		}
//		
//		if(par2Side == Side.SERVER) PacketHandler.sendToAll(this);
//	}
}
