package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.entity.player.PlayerEntity;
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
		if(context.getDirection().getReceptionSide().isServer())
		{
			context.setPacketHandled(true);
			return;
		}
		
		PlayerEntity sender = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
		if(sender != null)
		{
			LivingData data = LivingData.forEntity(sender);
			if(data != null)
				data.setAir(msg.air);
		}
		context.setPacketHandled(true);
	}
}
