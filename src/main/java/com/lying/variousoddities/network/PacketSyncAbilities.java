package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.AbilityData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
	
	public static PacketSyncAbilities decode(FriendlyByteBuf par1Buffer)
	{
		PacketSyncAbilities packet = new PacketSyncAbilities(par1Buffer.readUUID());
		packet.abilitiesData = par1Buffer.readNbt();
		return packet;
	}
	
	public static void encode(PacketSyncAbilities msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeUUID(msg.uuid);
		par1Buffer.writeNbt(msg.abilitiesData);
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
					for(LivingEntity living : world.getEntitiesOfClass(LivingEntity.class, Minecraft.getInstance().player.getBoundingBox().inflate(64D)))
						if(living.getUUID().equals(msg.uuid))
						{
							entity = living;
							break;
						}
					
					if(entity != null)
					{
						AbilityData data = AbilityData.getCapability(entity);
						data.deserializeNBT(msg.abilitiesData);
					}
				}
			}
		}
		else
		{
			ServerPlayer sender = context.getSender();
			if(sender != null)
			{
				AbilityData abilities = AbilityData.getCapability(sender);
				abilities.updateAbilityCache();
				CompoundTag data = abilities.serializeNBT();
				PacketHandler.sendToNearby(sender.getLevel(), sender, new PacketSyncAbilities(sender.getUUID(), data));
			}
		}
		context.setPacketHandled(true);
	}
}
