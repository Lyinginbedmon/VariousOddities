package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.entity.mount.EntityWarg;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSit
{
	private UUID playerID;
	private boolean sitting;
	
	public PacketSit(UUID playerIDIn, boolean sit)
	{
		this.playerID = playerIDIn;
		this.sitting = sit;
	}
	
	public static PacketSit decode(PacketBuffer par1Buffer)
	{
		return new PacketSit(par1Buffer.readUniqueId(), par1Buffer.readBoolean());
	}
	
	public static void encode(PacketSit msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeUniqueId(msg.playerID);
		par1Buffer.writeBoolean(msg.sitting);
	}
	
	public static void handle(PacketSit msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			World world = context.getSender().getEntityWorld();
			PlayerEntity player = world.getPlayerByUuid(msg.playerID);
			if(player != null)
				if(player.getRidingEntity() != null && player.getRidingEntity().getType() == VOEntities.WARG)
				{
					EntityWarg warg = (EntityWarg)player.getRidingEntity();
					warg.func_233687_w_(msg.sitting);
				}
		}
		context.setPacketHandled(true);
	}
}
