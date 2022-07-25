package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.event.AbilityEvent.AbilityRemoveEvent;
import com.lying.variousoddities.capabilities.Abilities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.proxy.CommonProxy;
import com.lying.variousoddities.species.abilities.Ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

public class PacketAbilityRemove
{
	private ResourceLocation mapName;
	
	public PacketAbilityRemove(ResourceLocation nameIn)
	{
		mapName = nameIn;
	}
	
	public static PacketAbilityRemove decode(PacketBuffer par1Buffer)
	{
		PacketAbilityRemove packet = new PacketAbilityRemove(par1Buffer.readResourceLocation());
		return packet;
	}
	
	public static void encode(PacketAbilityRemove msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeResourceLocation(msg.mapName);
	}
	
	public static void handle(PacketAbilityRemove msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(!context.getDirection().getReceptionSide().isServer())
		{
			Player sender = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(sender != null)
			{
				LivingData data = LivingData.forEntity(sender);
				if(data != null)
				{
					Abilities abilities = data.getAbilities();
					Ability ability = abilities.getCachedAbilities().get(msg.mapName);
					if(ability != null)
					{
						abilities.uncacheAbility(msg.mapName);
						ability.onAbilityRemoved(sender);
						MinecraftForge.EVENT_BUS.post(new AbilityRemoveEvent(sender, ability, abilities));
					}
				}
			}
		}
		
		context.setPacketHandled(true);
	}
}
