package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketPossessionControl
{
	private float strafe, forward;
	private boolean sneak, jump;
	
	public PacketPossessionControl(){ }
	public PacketPossessionControl(float strafe, float forward, boolean sneak, boolean jump)
	{
		this.strafe = strafe;
		this.forward = forward;
		this.sneak = sneak;
		this.jump = jump;
	}
	
	public static PacketPossessionControl decode(PacketBuffer par1Buffer)
	{
		PacketPossessionControl packet = new PacketPossessionControl();
		packet.strafe = par1Buffer.readFloat();
		packet.forward = par1Buffer.readFloat();
		packet.sneak = par1Buffer.readBoolean();
		packet.jump = par1Buffer.readBoolean();
		return packet;
	}
	
	public static void encode(PacketPossessionControl msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeFloat(msg.strafe);
		par1Buffer.writeFloat(msg.forward);
		par1Buffer.writeBoolean(msg.sneak);
		par1Buffer.writeBoolean(msg.jump);
	}
	
	public static void handle(PacketPossessionControl msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			PlayerEntity player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(player != null && PlayerData.forPlayer(player) != null)
			{
				LivingEntity ent = PlayerData.forPlayer(player).getPossessed();
				if(ent != null && ent instanceof MobEntity)
				{
					MobEntity mob = (MobEntity)ent;
					mob.getMoveHelper().strafe(msg.forward, msg.strafe);
					mob.setSneaking(msg.sneak);
					if(msg.jump)
						mob.getJumpController().setJumping();
				}
			}
		}
		
		context.setPacketHandled(true);
	}
}
