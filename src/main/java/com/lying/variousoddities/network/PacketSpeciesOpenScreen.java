package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSpeciesOpenScreen
{
	public PacketSpeciesOpenScreen(){ }
	
	public static PacketSpeciesOpenScreen decode(PacketBuffer par1Buffer)
	{
		PacketSpeciesOpenScreen packet = new PacketSpeciesOpenScreen();
		return packet;
	}
	
	public static void encode(PacketSpeciesOpenScreen msg, PacketBuffer par1Buffer)
	{
		
	}
	
	public static void handle(PacketSpeciesOpenScreen msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
			context.setPacketHandled(true);
		else
		{
			CommonProxy proxy = (CommonProxy)VariousOddities.proxy;
			proxy.openSpeciesSelectScreen(proxy.getPlayerEntity(context));
			context.setPacketHandled(true);
		}
	}
}
