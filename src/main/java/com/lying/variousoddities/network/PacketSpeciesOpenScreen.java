package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketSpeciesOpenScreen
{
	private int targetPower;
	private boolean randomise;
	
	public PacketSpeciesOpenScreen(int power, boolean random)
	{
		this.targetPower = power;
		this.randomise = random;
	}
	
	public static PacketSpeciesOpenScreen decode(FriendlyByteBuf par1Buffer)
	{
		return new PacketSpeciesOpenScreen(par1Buffer.readInt(), par1Buffer.readBoolean());
	}
	
	public static void encode(PacketSpeciesOpenScreen msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeInt(msg.targetPower);
		par1Buffer.writeBoolean(msg.randomise);
	}
	
	public static void handle(PacketSpeciesOpenScreen msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		context.setPacketHandled(true);
		if(context.getDirection().getReceptionSide().isClient())
		{
			CommonProxy proxy = (CommonProxy)VariousOddities.proxy;
			proxy.openSpeciesSelectScreen(proxy.getPlayerEntity(context), msg.targetPower, msg.randomise);
		}
	}
}
