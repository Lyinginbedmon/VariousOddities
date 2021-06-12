package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketVisualPotion
{
	private UUID entityID = null;
	private int index = 0;
	private boolean value = false;
	
	public PacketVisualPotion(){ }
	public PacketVisualPotion(UUID entityUUID, int indexIn, boolean valueIn)
	{
		entityID = entityUUID;
		index = indexIn;
		value = valueIn;
	}
	
	public static PacketVisualPotion decode(PacketBuffer par1Buffer)
	{
		PacketVisualPotion packet = new PacketVisualPotion();
		packet.entityID = par1Buffer.readUniqueId();
		packet.index = par1Buffer.readInt();
		packet.value = par1Buffer.readBoolean();
		return packet;
	}
	
	public static void encode(PacketVisualPotion msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeUniqueId(msg.entityID);
		par1Buffer.writeInt(msg.index);
		par1Buffer.writeBoolean(msg.value);
	}
	
	public static void handle(PacketVisualPotion msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			context.setPacketHandled(true);
			return;
		}
		
		PlayerEntity player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
		LivingEntity target = null;
		if(player.getUniqueID().equals(msg.entityID))
			target = player;
		else
		{
			World world = player.getEntityWorld();
			for(LivingEntity ent : world.getEntitiesWithinAABB(LivingEntity.class, player.getBoundingBox().grow(64D)))
				if(ent.getUniqueID().equals(msg.entityID))
				{
					target = ent;
					break;
				}
		}
		
		if(target != null)
			LivingData.forEntity(target).setVisualPotion(msg.index, msg.value);
		
		context.setPacketHandled(true);
	}
}
