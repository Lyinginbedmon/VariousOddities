package com.lying.variousoddities.network;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
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
			ClientWorld world = Minecraft.getInstance().world;
			ClientPlayerEntity player = Minecraft.getInstance().player;
			if(player == null || world == null)
				;
			else
				world.playSound(player, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.NEUTRAL, 0.25f, 0.1f + player.getRNG().nextFloat() * 0.9F);
		}
		
		context.setPacketHandled(true);
	}
}
