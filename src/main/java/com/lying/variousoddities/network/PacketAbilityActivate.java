package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.proxy.CommonProxy;
import com.lying.variousoddities.types.abilities.Ability;
import com.lying.variousoddities.types.abilities.ActivatedAbility;
import com.lying.variousoddities.types.abilities.AbilityRegistry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketAbilityActivate
{
	private ResourceLocation abilityName = null;
	
	public PacketAbilityActivate(){ }
	public PacketAbilityActivate(ResourceLocation mapName)
	{
		abilityName = mapName;
	}
	
	public static PacketAbilityActivate decode(PacketBuffer par1Buffer)
	{
		PacketAbilityActivate packet = new PacketAbilityActivate();
		packet.abilityName = par1Buffer.readResourceLocation();
		return packet;
	}
	
	public static void encode(PacketAbilityActivate msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeResourceLocation(msg.abilityName);
	}
	
	public static void handle(PacketAbilityActivate msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			PlayerEntity sender = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(sender != null)
			{
				Ability ability = AbilityRegistry.getAbilityByName(sender, msg.abilityName);
				if(ability != null && !ability.passive())
				{
					ActivatedAbility activatedAbility = (ActivatedAbility)ability;
					if(activatedAbility.canTrigger(sender))
						activatedAbility.trigger(sender, Dist.DEDICATED_SERVER);
				}
				else
					VariousOddities.log.warn(sender.getName().getUnformattedComponentText()+" does not have the ability they tried to activate ("+msg.abilityName.toString()+")");
			}
		}
		
		context.setPacketHandled(true);
	}
}
