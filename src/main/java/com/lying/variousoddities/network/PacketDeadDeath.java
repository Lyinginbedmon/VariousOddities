package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.PlayerData.SoulCondition;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketDeadDeath
{
	public PacketDeadDeath(){ }
	
	public static PacketDeadDeath decode(PacketBuffer par1Buffer)
	{
		return new PacketDeadDeath();
	}
	
	public static void encode(PacketDeadDeath msg, PacketBuffer par1Buffer)
	{
		
	}
	
	public static void handle(PacketDeadDeath msg, Supplier<NetworkEvent.Context> cxt)
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
		if(player == null || !PlayerData.isPlayerBodyDead(player))
			return;
		
		PlayerData.forPlayer(player).setSoulCondition(SoulCondition.ROAMING);
		
		if(!player.getEntityWorld().isRemote)
		{
			Entity body = PlayerData.forPlayer(player).getBody(player.getEntityWorld());
			if(body != null && body.getType() == VOEntities.BODY)
				body.onKillCommand();
			else if(body == null)
				VariousOddities.log.warn("Could not find corpse of player "+player.getDisplayName().getString()+" to despawn");
		}
		
		player.onKillCommand();
	}
}
