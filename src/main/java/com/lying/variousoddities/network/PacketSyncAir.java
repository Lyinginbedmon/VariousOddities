package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncAir
{
	private int air = 300;
	
	public PacketSyncAir(){ }
	public PacketSyncAir(int airIn)
	{
		air = airIn;
	}
	
	public static PacketSyncAir decode(FriendlyByteBuf par1Buffer)
	{
		PacketSyncAir packet = new PacketSyncAir();
		packet.air = par1Buffer.readInt();
		return packet;
	}
	
	public static void encode(PacketSyncAir msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeInt(msg.air);
	}
	
	public static void handle(PacketSyncAir msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		context.setPacketHandled(true);
		if(!context.getDirection().getReceptionSide().isServer())
		{
			Player sender = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(sender != null)
				sender.setAirSupply(msg.air);
		}
	}
}
