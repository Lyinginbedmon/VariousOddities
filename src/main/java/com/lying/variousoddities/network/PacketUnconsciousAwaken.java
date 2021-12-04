package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.PlayerData.BodyCondition;
import com.lying.variousoddities.capabilities.PlayerData.SoulCondition;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketUnconsciousAwaken
{
	public PacketUnconsciousAwaken(){ }
	
	public static PacketUnconsciousAwaken decode(PacketBuffer par1Buffer)
	{
		return new PacketUnconsciousAwaken();
	}
	
	public static void encode(PacketUnconsciousAwaken msg, PacketBuffer par1Buffer)
	{
		
	}
	
	public static void handle(PacketUnconsciousAwaken msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			PlayerEntity sender = context.getSender();
			if(sender != null)
			{
				handleForPlayer(sender);
				PacketHandler.sendTo((ServerPlayerEntity)sender, msg);
			}
		}
		else
			handleForPlayer(((CommonProxy)VariousOddities.proxy).getPlayerEntity(context));
		
		context.setPacketHandled(true);
	}
	
	private static void handleForPlayer(PlayerEntity player)
	{
		if(player == null || !PlayerData.isPlayerBodyAsleep(player))
			return;
		PlayerData data = PlayerData.forPlayer(player);
		if(data == null || data.getSoulCondition() != SoulCondition.ALIVE)
			return;
		
		data.setBodyCondition(BodyCondition.ALIVE);
	}
}
