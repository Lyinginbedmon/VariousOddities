package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

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
	
	public static PacketVisualPotion decode(FriendlyByteBuf par1Buffer)
	{
		PacketVisualPotion packet = new PacketVisualPotion();
		packet.entityID = par1Buffer.readUUID();
		packet.index = par1Buffer.readInt();
		packet.value = par1Buffer.readBoolean();
		return packet;
	}
	
	public static void encode(PacketVisualPotion msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeUUID(msg.entityID);
		par1Buffer.writeInt(msg.index);
		par1Buffer.writeBoolean(msg.value);
	}
	
	public static void handle(PacketVisualPotion msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		context.setPacketHandled(true);
		if(context.getDirection().getReceptionSide().isServer())
			return;
		
		Player player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
		if(player == null)
			return;
		
		LivingEntity target = null;
		if(player.getUUID().equals(msg.entityID))
			target = player;
		else
		{
			Level world = player.getLevel();
			for(LivingEntity ent : world.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(64D)))
				if(ent.getUUID().equals(msg.entityID))
				{
					target = ent;
					break;
				}
		}
		
		if(target != null)
			LivingData.getCapability(target).setVisualPotion(msg.index, msg.value);
	}
}
