package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketAbilityCooldown
{
	public PacketAbilityCooldown(){ }
	
	public static PacketAbilityCooldown decode(PacketBuffer par1Buffer)
	{
		return new PacketAbilityCooldown();
	}
	
	public static void encode(PacketAbilityCooldown msg, PacketBuffer par1Buffer)
	{
		
	}
	
	public static void handle(PacketAbilityCooldown msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		
		if(context.getDirection().getReceptionSide().isClient())
		{
			PlayerEntity player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(player != null)
			{
				World world = player.getEntityWorld();
				if(world != null && ConfigVO.CLIENT.announceCools.get())
					world.playSound(player, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.NEUTRAL, 0.25f, 0.1f + player.getRNG().nextFloat() * 0.9F);
			}
		}
		
		context.setPacketHandled(true);
	}
}
