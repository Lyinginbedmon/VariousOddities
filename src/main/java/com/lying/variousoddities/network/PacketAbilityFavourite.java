package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.AbilityData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class PacketAbilityFavourite
{
	private ResourceLocation mapName;
	private boolean favourite = true;
	
	public PacketAbilityFavourite(ResourceLocation nameIn)
	{
		mapName = nameIn;
	}
	
	public PacketAbilityFavourite(ResourceLocation nameIn, boolean shouldFavourite)
	{
		this(nameIn);
		this.favourite = shouldFavourite;
	}
	
	public static PacketAbilityFavourite decode(FriendlyByteBuf par1Buffer)
	{
		PacketAbilityFavourite packet = new PacketAbilityFavourite(par1Buffer.readResourceLocation());
		packet.favourite = par1Buffer.readBoolean();
		return packet;
	}
	
	public static void encode(PacketAbilityFavourite msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeResourceLocation(msg.mapName);
		par1Buffer.writeBoolean(msg.favourite);
	}
	
	public static void handle(PacketAbilityFavourite msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			Player sender = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(sender != null)
			{
				AbilityData abilities = AbilityData.getCapability(sender);
				if(abilities != null)
				{
					if(msg.favourite)
						abilities.favourite(msg.mapName);
					else
						abilities.unfavourite(msg.mapName);
				}
			}
		}
		
		context.setPacketHandled(true);
	}
}
