package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncLivingData
{
	private UUID entityID;
	private CompoundTag dataNBT;
	
	public PacketSyncLivingData(UUID idIn)
	{
		this.entityID = idIn;
	}
	public PacketSyncLivingData(UUID idIn, LivingData dataIn)
	{
		this(idIn);
		this.dataNBT = dataIn.serializeNBT();
	}
	
	public boolean isRequest(){ return dataNBT != null; }
	
	public static PacketSyncLivingData decode(PacketBuffer par1Buffer)
	{
		PacketSyncLivingData packet = new PacketSyncLivingData(par1Buffer.readUniqueId());
		if(par1Buffer.readBoolean())
			packet.dataNBT = par1Buffer.readCompoundTag();
		return packet;
	}
	
	public static void encode(PacketSyncLivingData msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeUniqueId(msg.entityID);
		par1Buffer.writeBoolean(msg.isRequest());
		if(msg.isRequest())
			par1Buffer.writeCompoundTag(msg.dataNBT);
	}
	
	public static void handle(PacketSyncLivingData msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			ServerPlayer player = context.getSender();
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
				PacketHandler.sendTo(player, new PacketSyncLivingData(msg.entityID, LivingData.forEntity(target)));
		}
		else
		{
			Player player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
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
			{
				LivingData data = LivingData.forEntity(target);
				if(data != null)
					data.deserializeNBT(msg.dataNBT);
			}
		}
		context.setPacketHandled(true);
	}
}
