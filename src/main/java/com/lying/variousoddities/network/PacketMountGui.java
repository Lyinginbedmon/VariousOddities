package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.entity.IMountInventory;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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
			ServerPlayerEntity player = context.getSender();
			Entity mount = player.getRidingEntity();
			if(mount == null || !(mount instanceof IMountInventory))
				return;
			
			IMountInventory inv = (IMountInventory)mount;
			inv.openContainer(player);
			
//			PacketHandler.sendTo(player, msg);
		}
		else
		{
			PlayerEntity player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			Entity mount = player.getRidingEntity();
			if(mount == null || !(mount instanceof IMountInventory))
				return;
			
			IMountInventory inv = (IMountInventory)mount;
			inv.openContainer(player);
		}
	}
}
