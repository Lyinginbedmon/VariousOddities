package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.proxy.CommonProxy;
import com.lying.variousoddities.world.savedata.ScentsManager;
import com.lying.variousoddities.world.savedata.ScentsManager.ScentMarker;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketAddScent
{
	private CompoundNBT scentData;
	
	public PacketAddScent(){ }
	public PacketAddScent(ScentMarker marker)
	{
		scentData = marker.writeToNBT(new CompoundNBT());
	}
	
	public static PacketAddScent decode(PacketBuffer par1Buffer)
	{
		PacketAddScent packet = new PacketAddScent();
		packet.scentData = par1Buffer.readCompoundTag();
		return packet;
	}
	
	public static void encode(PacketAddScent msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeCompoundTag(msg.scentData);
	}
	
	public static void handle(PacketAddScent msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(!context.getDirection().getReceptionSide().isServer())
		{
			PlayerEntity player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			ScentsManager manager = VariousOddities.proxy.getScentsManager(player.getEntityWorld());
			
			ScentMarker marker = new ScentMarker(player.getEntityWorld(), msg.scentData);
			manager.addScentMarker(marker);
		}
		
		context.setPacketHandled(true);
	}
}
