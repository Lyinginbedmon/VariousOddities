package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.capabilities.LivingData;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSyncAir
{
	private int air = 300;
	
	public PacketSyncAir(){ }
	public PacketSyncAir(int airIn)
	{
		air = airIn;
	}
	
	public static PacketSyncAir decode(PacketBuffer par1Buffer)
	{
		PacketSyncAir packet = new PacketSyncAir();
		packet.air = par1Buffer.readInt();
		return packet;
	}
	
	public static void encode(PacketSyncAir msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeInt(msg.air);
	}
	
	public static void handle(PacketSyncAir msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		LivingData data = LivingData.forEntity(context.getSender());
		data.setAir(msg.air);
		context.setPacketHandled(true);
	}
}
