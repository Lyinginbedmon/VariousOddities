package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.proxy.CommonProxy;
import com.lying.variousoddities.world.savedata.ScentsManager;
import com.lying.variousoddities.world.savedata.ScentsManager.ScentMarker;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class PacketAddScent
{
	private CompoundTag scentData;
	
	public PacketAddScent(){ }
	public PacketAddScent(ScentMarker marker)
	{
		scentData = marker.writeToNBT(new CompoundTag());
	}
	
	public static PacketAddScent decode(FriendlyByteBuf par1Buffer)
	{
		PacketAddScent packet = new PacketAddScent();
		packet.scentData = par1Buffer.readNbt();
		return packet;
	}
	
	public static void encode(PacketAddScent msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeNbt(msg.scentData);
	}
	
	public static void handle(PacketAddScent msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		context.setPacketHandled(true);
		if(!context.getDirection().getReceptionSide().isServer())
		{
			Player player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(player == null)
				return;
			
			ScentsManager manager = VariousOddities.proxy.getScentsManager(player.getLevel());
			
			ScentMarker marker = new ScentMarker(player.getLevel(), msg.scentData);
			manager.addScentMarker(marker);
		}
	}
}
