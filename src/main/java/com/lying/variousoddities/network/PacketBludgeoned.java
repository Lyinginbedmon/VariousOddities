package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

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
	
	public static PacketBludgeoned decode(FriendlyByteBuf par1Buffer)
	{
		return new PacketBludgeoned(par1Buffer.readUUID(), par1Buffer.readBoolean());
	}
	
	public static void encode(PacketBludgeoned msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeUUID(msg.entityID);
		par1Buffer.writeBoolean(msg.knockedOut);
	}
	
	public static void handle(PacketBludgeoned msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(!context.getDirection().getReceptionSide().isServer())
		{
			Player player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			for(LivingEntity entity : player.getLevel().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16D)))
				if(entity.getUUID().equals(msg.entityID))
				{
					entity.playSound(msg.knockedOut ? SoundEvents.PLAYER_ATTACK_CRIT : SoundEvents.PLAYER_ATTACK_STRONG, 1F, 0.5F + entity.getRandom().nextFloat());
					break;
				}
		}
		
		context.setPacketHandled(true);
	}
}
