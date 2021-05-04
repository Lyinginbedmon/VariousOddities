package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.capabilities.LivingData;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketAbilitiesSync
{
	private final UUID uuid;
	private CompoundNBT abilitiesData = new CompoundNBT();
	
	public PacketAbilitiesSync(UUID entityUUID)
	{
		this.uuid = entityUUID;
	}
	public PacketAbilitiesSync(UUID entityUUID, CompoundNBT abilitiesIn)
	{
		this(entityUUID);
		abilitiesData = abilitiesIn;
	}
	
	public static PacketAbilitiesSync decode(PacketBuffer par1Buffer)
	{
		PacketAbilitiesSync packet = new PacketAbilitiesSync(par1Buffer.readUniqueId());
		packet.abilitiesData = par1Buffer.readCompoundTag();
		return packet;
	}
	
	public static void encode(PacketAbilitiesSync msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeUniqueId(msg.uuid);
		par1Buffer.writeCompoundTag(msg.abilitiesData);
	}
	
	public static void handle(PacketAbilitiesSync msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isClient())
		{
			World world = Minecraft.getInstance().world;
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
		
		context.setPacketHandled(true);
	}
}
