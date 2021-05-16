package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.Abilities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSyncAbilities
{
	private final UUID uuid;
	private CompoundNBT abilitiesData = new CompoundNBT();
	
	public PacketSyncAbilities(UUID entityUUID)
	{
		this.uuid = entityUUID;
	}
	public PacketSyncAbilities(UUID entityUUID, CompoundNBT abilitiesIn)
	{
		this(entityUUID);
		abilitiesData = abilitiesIn;
	}
	
	public static PacketSyncAbilities decode(PacketBuffer par1Buffer)
	{
		PacketSyncAbilities packet = new PacketSyncAbilities(par1Buffer.readUniqueId());
		packet.abilitiesData = par1Buffer.readCompoundTag();
		return packet;
	}
	
	public static void encode(PacketSyncAbilities msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeUniqueId(msg.uuid);
		par1Buffer.writeCompoundTag(msg.abilitiesData);
	}
	
	public static void handle(PacketSyncAbilities msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isClient())
		{
			PlayerEntity player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(player != null)
			{
				World world = player.getEntityWorld();
				if(world != null)
				{
					LivingEntity entity = null;
					for(LivingEntity living : world.getEntitiesWithinAABB(LivingEntity.class, Minecraft.getInstance().player.getBoundingBox().grow(64D)))
						if(living.getUniqueID().equals(msg.uuid))
						{
							entity = living;
							break;
						}
					
					if(entity != null)
					{
						LivingData data = LivingData.forEntity(entity);
						data.getAbilities().deserializeNBT(msg.abilitiesData);
					}
				}
			}
		}
		else
		{
			ServerPlayerEntity sender = context.getSender();
			if(sender != null)
			{
				Abilities abilities = LivingData.forEntity(sender).getAbilities();
				CompoundNBT data = abilities.serializeNBT();
				PacketHandler.sendToNearby(sender.getEntityWorld(), sender, new PacketSyncAbilities(sender.getUniqueID(), data));
			}
		}
		context.setPacketHandled(true);
	}
}
