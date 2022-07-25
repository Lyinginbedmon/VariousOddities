package com.lying.variousoddities.network;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class PacketTileUpdate
{
	private BlockPos pos = BlockPos.ZERO;
	private CompoundTag data = new CompoundTag();
	
	public PacketTileUpdate(){ }
	public PacketTileUpdate(BlockEntity tile)
	{
		pos = tile.getBlockPos();
		data = tile.saveWithFullMetadata();
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
		
		Level world = null;
		try{ world = context.getSender().getLevel(); }
		catch(Exception e){ }
		if(world != null)
		{
			BlockEntity tile = world.getBlockEntity(msg.pos);
			if(tile != null)
				tile.load(msg.data);
		}
		
		context.setPacketHandled(true);
	}
}
