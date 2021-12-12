package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.proxy.CommonProxy;
import com.lying.variousoddities.utility.VOHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketPossessionLook
{
	private double yaw, pitch;
	
	public PacketPossessionLook(){ }
	public PacketPossessionLook(double yawIn, double pitchIn)
	{
		this.yaw = yawIn;
		this.pitch = pitchIn;
	}
	
	public static PacketPossessionLook decode(PacketBuffer par1Buffer)
	{
		PacketPossessionLook packet = new PacketPossessionLook();
		packet.yaw = par1Buffer.readDouble();
		packet.pitch = par1Buffer.readDouble();
		return packet;
	}
	
	public static void encode(PacketPossessionLook msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeDouble(msg.yaw);
		par1Buffer.writeDouble(msg.pitch);
	}
	
	public static void handle(PacketPossessionLook msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			PlayerEntity player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(player != null && PlayerData.forPlayer(player) != null)
			{
				LivingEntity ent = PlayerData.forPlayer(player).getPossessed();
				if(ent != null && ent instanceof MobEntity)
					VOHelper.addRotationToEntityHead(ent, msg.yaw, msg.pitch);
			}
		}
		
		context.setPacketHandled(true);
	}
}
