package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.init.VOPotions;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketParalysisResignation
{
	public PacketParalysisResignation(){ }
	
	public static PacketParalysisResignation decode(PacketBuffer par1Buffer)
	{
		return new PacketParalysisResignation();
	}
	
	public static void encode(PacketParalysisResignation msg, PacketBuffer par1Buffer)
	{
		
	}
	
	public static void handle(PacketParalysisResignation msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			PlayerEntity sender = context.getSender();
			if(sender != null && VOPotions.isParalysed(sender))
				sender.attackEntityFrom(VODamageSource.PARALYSIS, Float.MAX_VALUE);
		}
		context.setPacketHandled(true);
	}
}
