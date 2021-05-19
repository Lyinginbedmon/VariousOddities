package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.Abilities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketAirJump
{
	public PacketAirJump(){ }
	
	public static PacketAirJump decode(PacketBuffer par1Buffer)
	{
		return new PacketAirJump();
	}
	
	public static void encode(PacketAirJump msg, PacketBuffer par1Buffer)
	{
		
	}
	
	public static void handle(PacketAirJump msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			PlayerEntity sender = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(sender != null)
			{
				LivingData data = LivingData.forEntity(sender);
				if(data != null)
				{
					Abilities abilities = data.getAbilities();
					if(abilities.canAirJump)
						abilities.doAirJump();
				}
			}
		}
		
		context.setPacketHandled(true);
	}
}
