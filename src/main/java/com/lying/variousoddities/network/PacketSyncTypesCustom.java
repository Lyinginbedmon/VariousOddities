package com.lying.variousoddities.network;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.proxy.CommonProxy;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncTypesCustom
{
	private UUID entityID;
	private List<EnumCreatureType> types = Lists.newArrayList();
	
	public PacketSyncTypesCustom(){ }
	public PacketSyncTypesCustom(LivingEntity entity, List<EnumCreatureType> typesIn)
	{
		this.entityID = entity.getUUID();
		this.types = typesIn;
	}
	
	public static PacketSyncTypesCustom decode(FriendlyByteBuf par1Buffer)
	{
		PacketSyncTypesCustom packet = new PacketSyncTypesCustom();
		packet.entityID = par1Buffer.readUUID();
		
		int count = par1Buffer.readInt();
		for(int i=0; i<count; i++)
			packet.types.add(par1Buffer.readEnum(EnumCreatureType.class));
		
		return packet;
	}
	
	public static void encode(PacketSyncTypesCustom msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeUUID(msg.entityID);
		
		par1Buffer.writeInt(msg.types.size());
		msg.types.forEach((type) -> { par1Buffer.writeEnum(type); });
	}
	
	public static void handle(PacketSyncTypesCustom msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(!context.getDirection().getReceptionSide().isServer())
		{
			Player player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(player != null)
			{
				Level world = player.getLevel();
				
				LivingEntity entity = null;
				for(LivingEntity living : world.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(64D, 64D, 64D)))
					if(living.getUUID().equals(msg.entityID))
					{
						entity = living;
						break;
					};
				
				if(entity != null)
				{
					LivingData data = LivingData.getCapability(entity);
					data.setCustomTypes(msg.types);
				}
			}
		}
		context.setPacketHandled(true);
	}
}
