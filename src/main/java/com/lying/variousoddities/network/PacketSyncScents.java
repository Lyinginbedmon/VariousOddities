package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.proxy.CommonProxy;
import com.lying.variousoddities.world.savedata.ScentsManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncScents
{
	private CompoundTag scentData;
	
	public PacketSyncScents(){ }
	public PacketSyncScents(ScentsManager manager)
	{
		scentData = manager.write(new CompoundTag());
	}
	
	public static PacketSyncScents decode(FriendlyByteBuf par1Buffer)
	{
		PacketSyncScents packet = new PacketSyncScents();
		packet.scentData = par1Buffer.readNbt();
		return packet;
	}
	
	public static void encode(PacketSyncScents msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeNbt(msg.scentData);
	}
	
	public static void handle(PacketSyncScents msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(!context.getDirection().getReceptionSide().isServer())
		{
			Player player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			ScentsManager manager = VariousOddities.proxy.getScentsManager(player.getLevel());
			manager.read(msg.scentData);
		}
		
		context.setPacketHandled(true);
	}
}
