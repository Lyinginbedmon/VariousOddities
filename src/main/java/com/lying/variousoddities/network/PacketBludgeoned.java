package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketBludgeoned
{
	private UUID entityID;
	private boolean knockedOut;
	
	public PacketBludgeoned(){ }
	public PacketBludgeoned(UUID uuidIn, boolean knockedIn)
	{
		entityID = uuidIn;
		knockedOut = knockedIn;
	}
	
	public static PacketBludgeoned decode(PacketBuffer par1Buffer)
	{
		return new PacketBludgeoned(par1Buffer.readUniqueId(), par1Buffer.readBoolean());
	}
	
	public static void encode(PacketBludgeoned msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeUniqueId(msg.entityID);
		par1Buffer.writeBoolean(msg.knockedOut);
	}
	
	public static void handle(PacketBludgeoned msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(!context.getDirection().getReceptionSide().isServer())
		{
			PlayerEntity player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			for(LivingEntity entity : player.getEntityWorld().getEntitiesWithinAABB(LivingEntity.class, player.getBoundingBox().grow(16D)))
				if(entity.getUniqueID().equals(msg.entityID))
				{
					entity.playSound(msg.knockedOut ? SoundEvents.ENTITY_PLAYER_ATTACK_CRIT : SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, 1F, 0.5F + entity.getRNG().nextFloat());
					break;
				}
		}
		
		context.setPacketHandled(true);
	}
}
