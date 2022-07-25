package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.Abilities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncAbilities
{
	private final UUID uuid;
	private CompoundTag abilitiesData = new CompoundTag();
	
	public PacketSyncAbilities(UUID entityUUID)
	{
		this.uuid = entityUUID;
	}
	public PacketSyncAbilities(UUID entityUUID, CompoundTag abilitiesIn)
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
			Player player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(player != null)
			{
				Level world = player.getLevel();
				if(world != null)
				{
					LivingEntity entity = null;
					for(LivingEntity living : world.getEntitiesWithinAABB(LivingEntity.class, Minecraft.getInstance().player.getBoundingBox().grow(64D)))
						if(living.getUUID().equals(msg.uuid))
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
			ServerPlayer sender = context.getSender();
			if(sender != null)
			{
				Abilities abilities = LivingData.forEntity(sender).getAbilities();
				abilities.forceRecache();
				CompoundTag data = abilities.serializeNBT();
				PacketHandler.sendToNearby(sender.getLevel(), sender, new PacketSyncAbilities(sender.getUUID(), data));
			}
		}
		context.setPacketHandled(true);
	}
}
