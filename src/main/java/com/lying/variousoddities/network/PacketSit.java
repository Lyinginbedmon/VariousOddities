package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.entity.mount.EntityWarg;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.network.PacketBuffer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

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
			Level world = context.getSender().getLevel();
			Player player = world.getPlayerByUUID(msg.playerID);
			if(player != null)
				if(player.getVehicle() != null && player.getVehicle().getType() == VOEntities.WARG)
				{
					EntityWarg warg = (EntityWarg)player.getVehicle();
					warg.setOrderedToSit(msg.sitting);
				}
		}
		context.setPacketHandled(true);
	}
}
