package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.potion.PotionPetrifying;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketPetrifying
{
	private UUID entityID = null;
	private int amplifier = 0;
	
	public PacketPetrifying(){ }
	public PacketPetrifying(UUID entityUUID, int amp)
	{
		entityID = entityUUID;
		amplifier = amp;
	}
	
	public static PacketPetrifying decode(PacketBuffer par1Buffer)
	{
		return new PacketPetrifying(par1Buffer.readUniqueId(), par1Buffer.readInt());
	}
	
	public static void encode(PacketPetrifying msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeUniqueId(msg.entityID);
		par1Buffer.writeInt(msg.amplifier);
	}
	
	public static void handle(PacketPetrifying msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(!context.getDirection().getReceptionSide().isServer())
		{
			PlayerEntity player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			LivingEntity target = getEntityFromUUID(player, msg.entityID);
			if(target != null)
				PotionPetrifying.doParticles(target, msg.amplifier);
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
			target = world.getPlayerByUuid(entityID);
			
			if(target == null)
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
