package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSyncBludgeoning
{
	private float bludgeoning = 0F;
	
	public PacketSyncBludgeoning(){ }
	public PacketSyncBludgeoning(float bludgeonIn)
	{
		bludgeoning = bludgeonIn;
	}
	
	public static PacketSyncBludgeoning decode(PacketBuffer par1Buffer)
	{
		return new PacketSyncBludgeoning(par1Buffer.readFloat());
	}
	
	public static void encode(PacketSyncBludgeoning msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeFloat(msg.bludgeoning);
	}
	
	public static void handle(PacketSyncBludgeoning msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		context.setPacketHandled(true);
		if(context.getDirection().getReceptionSide().isServer())
			return;
		
		PlayerEntity sender = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
		if(sender != null)
		{
			LivingData data = LivingData.forEntity(sender);
			if(data != null)
				data.setBludgeoning(msg.bludgeoning);
		}
	}
}
