package com.lying.variousoddities.network;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketTileUpdate
{
	private BlockPos pos = BlockPos.ZERO;
	private CompoundNBT data = new CompoundNBT();
	
	public PacketTileUpdate(){ }
	public PacketTileUpdate(TileEntity tile)
	{
		pos = tile.getPos();
		data = tile.write(new CompoundNBT());
	}
	
	public static PacketTileUpdate decode(PacketBuffer par1Buffer)
	{
		PacketTileUpdate packet = new PacketTileUpdate();
		packet.pos = par1Buffer.readBlockPos();
		packet.data = par1Buffer.readCompoundTag();
		return packet;
	}
	
	public static void encode(PacketTileUpdate msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeBlockPos(msg.pos);
		par1Buffer.writeCompoundTag(msg.data);
	}
	
	public static void handle(PacketTileUpdate msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		
		World world = null;
		try{ world = context.getSender().getEntityWorld(); }
		catch(Exception e){ }
		if(world != null)
		{
			TileEntity tile = world.getTileEntity(msg.pos);
			if(tile != null)
				tile.read(world.getBlockState(msg.pos), msg.data);
		}
		
		context.setPacketHandled(true);
	}
}
