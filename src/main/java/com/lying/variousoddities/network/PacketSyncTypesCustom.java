package com.lying.variousoddities.network;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.proxy.CommonProxy;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSyncTypesCustom
{
	private UUID entityID;
	private List<EnumCreatureType> types = Lists.newArrayList();
	
	public PacketSyncTypesCustom(){ }
	public PacketSyncTypesCustom(LivingEntity entity, List<EnumCreatureType> typesIn)
	{
		this.entityID = entity.getUniqueID();
		this.types = typesIn;
	}
	
	public static PacketSyncTypesCustom decode(PacketBuffer par1Buffer)
	{
		PacketSyncTypesCustom packet = new PacketSyncTypesCustom();
		packet.entityID = par1Buffer.readUniqueId();
		
		int count = par1Buffer.readInt();
		for(int i=0; i<count; i++)
			packet.types.add(par1Buffer.readEnumValue(EnumCreatureType.class));
		
		return packet;
	}
	
	public static void encode(PacketSyncTypesCustom msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeUniqueId(msg.entityID);
		
		par1Buffer.writeInt(msg.types.size());
		msg.types.forEach((type) -> { par1Buffer.writeEnumValue(type); });
	}
	
	public static void handle(PacketSyncTypesCustom msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(!context.getDirection().getReceptionSide().isServer())
		{
			PlayerEntity player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(player != null)
			{
				World world = player.getEntityWorld();
				
				LivingEntity entity = null;
				for(LivingEntity living : world.getEntitiesWithinAABB(LivingEntity.class, player.getBoundingBox().grow(64D, 64D, 64D)))
					if(living.getUniqueID().equals(msg.entityID))
					{
						entity = living;
						break;
					};
				
				if(entity != null)
				{
					LivingData data = LivingData.forEntity(entity);
					data.setCustomTypes(msg.types);
				}
			}
		}
		context.setPacketHandled(true);
	}
}
