package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSyncPlayerData
{
	private UUID playerID;
	private CompoundNBT dataNBT;
	
	public PacketSyncPlayerData(UUID idIn)
	{
		this.playerID = idIn;
	}
	public PacketSyncPlayerData(UUID idIn, PlayerData dataIn)
	{
		this(idIn);
		this.dataNBT = dataIn.serializeNBT();
	}
	
	public boolean isRequest(){ return dataNBT != null; }
	
	public static PacketSyncPlayerData decode(PacketBuffer par1Buffer)
	{
		PacketSyncPlayerData packet = new PacketSyncPlayerData(par1Buffer.readUniqueId());
		if(par1Buffer.readBoolean())
			packet.dataNBT = par1Buffer.readCompoundTag();
		return packet;
	}
	
	public static void encode(PacketSyncPlayerData msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeUniqueId(msg.playerID);
		par1Buffer.writeBoolean(msg.isRequest());
		if(msg.isRequest())
			par1Buffer.writeCompoundTag(msg.dataNBT);
	}
	
	public static void handle(PacketSyncPlayerData msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			ServerPlayerEntity player = context.getSender();
			PlayerEntity target = null;
			if(player.getUniqueID().equals(msg.playerID))
				target = player;
			else
				target = player.getEntityWorld().getPlayerByUuid(msg.playerID);
			
			if(target != null)
				PacketHandler.sendTo(player, new PacketSyncPlayerData(msg.playerID, PlayerData.forPlayer(target)));
		}
		else
		{
			PlayerEntity player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			PlayerEntity target = null;
			if(player.getUniqueID().equals(msg.playerID))
				target = player;
			else
				target = player.getEntityWorld().getPlayerByUuid(msg.playerID);
			
			if(target != null)
			{
				PlayerData data = PlayerData.forPlayer(target);
				if(data != null)
					data.deserializeNBT(msg.dataNBT);
			}
		}
		context.setPacketHandled(true);
	}
}
