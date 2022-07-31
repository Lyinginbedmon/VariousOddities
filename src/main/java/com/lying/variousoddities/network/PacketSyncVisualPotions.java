package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncVisualPotions
{
	private UUID entityID = null;
	private byte value = 0;
	
	public PacketSyncVisualPotions(){ }
	public PacketSyncVisualPotions(UUID entityUUID)
	{
		entityID = entityUUID;
	}
	
	public static PacketSyncVisualPotions decode(FriendlyByteBuf par1Buffer)
	{
		PacketSyncVisualPotions packet = new PacketSyncVisualPotions();
		packet.entityID = par1Buffer.readUUID();
		packet.value = par1Buffer.readByte();
		return packet;
	}
	
	public static void encode(PacketSyncVisualPotions msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeUUID(msg.entityID);
		par1Buffer.writeByte(msg.value);
	}
	
	public static void handle(PacketSyncVisualPotions msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			ServerPlayer player = context.getSender();
			LivingEntity target = getEntityFromUUID(player, msg.entityID);
			
			if(target != null)
			{
				LivingData data = LivingData.forEntity(target);
				if(data != null)
				{
					msg.value = data.getVisualPotions();
					PacketHandler.sendTo(player, msg);
				}
			}
		}
		else
		{
			Player player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			LivingEntity target = getEntityFromUUID(player, msg.entityID);
			if(target != null)
				LivingData.forEntity(target).setVisualPotions(msg.value);
		}
		
		context.setPacketHandled(true);
	}
	
	private static LivingEntity getEntityFromUUID(Player player, UUID entityID)
	{
		LivingEntity target = null;
		if(player.getUUID().equals(entityID))
			target = player;
		else
		{
			Level world = player.getLevel();
			for(LivingEntity ent : world.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(64D)))
				if(ent.getUUID().equals(entityID))
				{
					target = ent;
					break;
				}
		}
		
		return target;
	}
}
