package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.network.PacketBuffer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

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
			Player player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(player != null)
			{
				Level world = player.getLevel();
				if(world != null && ConfigVO.CLIENT.announceCools.get())
					world.playSound(player, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.25f, 0.1f + player.getRandom().nextFloat() * 0.9F);
			}
		}
		
		context.setPacketHandled(true);
	}
}
