package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSyncVisualPotions
{
	private UUID entityID = null;
	private byte value = 0;
	
	public PacketSyncVisualPotions(){ }
	public PacketSyncVisualPotions(UUID entityUUID)
	{
		entityID = entityUUID;
	}
	
	public static PacketSyncVisualPotions decode(PacketBuffer par1Buffer)
	{
		PacketSyncVisualPotions packet = new PacketSyncVisualPotions();
		packet.entityID = par1Buffer.readUniqueId();
		packet.value = par1Buffer.readByte();
		return packet;
	}
	
	public static void encode(PacketSyncVisualPotions msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeUniqueId(msg.entityID);
		par1Buffer.writeByte(msg.value);
	}
	
	public static void handle(PacketSyncVisualPotions msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			ServerPlayerEntity player = context.getSender();
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
			PlayerEntity player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			LivingEntity target = getEntityFromUUID(player, msg.entityID);
			if(target != null)
				LivingData.forEntity(target).setVisualPotions(msg.value);
		}
		
		context.setPacketHandled(true);
	}
	
	private static LivingEntity getEntityFromUUID(PlayerEntity player, UUID entityID)
	{
		LivingEntity target = null;
		if(player.getUniqueID().equals(entityID))
			target = player;
		else
		{
			World world = player.getEntityWorld();
			for(LivingEntity ent : world.getEntitiesWithinAABB(LivingEntity.class, player.getBoundingBox().grow(64D)))
				if(ent.getUniqueID().equals(entityID))
				{
					target = ent;
					break;
				}
		}
		
		return target;
	}
}
