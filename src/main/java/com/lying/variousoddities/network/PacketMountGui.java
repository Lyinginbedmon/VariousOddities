package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.entity.IMountInventory;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.network.PacketBuffer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class PacketMountGui
{
	public PacketMountGui(){ }
	
	public static PacketMountGui decode(PacketBuffer par1Buffer)
	{
		return new PacketMountGui();
	}
	
	public static void encode(PacketMountGui msg, PacketBuffer par1Buffer)
	{
		
	}
	
	public static void handle(PacketMountGui msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		context.setPacketHandled(true);
		if(context.getDirection().getReceptionSide().isServer())
		{
			ServerPlayer player = context.getSender();
			Entity mount = player.getVehicle();
			if(mount == null || !(mount instanceof IMountInventory))
				return;
			
			IMountInventory inv = (IMountInventory)mount;
			inv.openContainer(player);
			
//			PacketHandler.sendTo(player, msg);
		}
		else
		{
			Player player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			Entity mount = player.getVehicle();
			if(mount == null || !(mount instanceof IMountInventory))
				return;
			
			IMountInventory inv = (IMountInventory)mount;
			inv.openContainer(player);
		}
	}
}
