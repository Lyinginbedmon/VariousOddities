package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSyncLivingData
{
	private UUID entityID;
	private CompoundNBT dataNBT;
	
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
			ServerPlayerEntity player = context.getSender();
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
				PacketHandler.sendTo(player, new PacketSyncLivingData(msg.entityID, LivingData.forEntity(target)));
		}
		else
		{
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
			{
				LivingData data = LivingData.forEntity(target);
				if(data != null)
					data.deserializeNBT(msg.dataNBT);
			}
		}
		context.setPacketHandled(true);
	}
}
