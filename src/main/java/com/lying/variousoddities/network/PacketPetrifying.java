package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.potion.PotionPetrifying;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

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
	
	public static PacketPetrifying decode(FriendlyByteBuf par1Buffer)
	{
		return new PacketPetrifying(par1Buffer.readUUID(), par1Buffer.readInt());
	}
	
	public static void encode(PacketPetrifying msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeUUID(msg.entityID);
		par1Buffer.writeInt(msg.amplifier);
	}
	
	public static void handle(PacketPetrifying msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(!context.getDirection().getReceptionSide().isServer())
		{
			Player player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			LivingEntity target = getEntityFromUUID(player, msg.entityID);
			if(target != null)
				PotionPetrifying.doParticles(target, msg.amplifier);
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
			target = world.getPlayerByUUID(entityID);
			
			if(target == null)
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
