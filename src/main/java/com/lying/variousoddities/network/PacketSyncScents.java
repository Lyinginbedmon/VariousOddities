package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.proxy.CommonProxy;
import com.lying.variousoddities.world.savedata.ScentsManager;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSyncScents
{
	private CompoundNBT scentData;
	
	public PacketSyncScents(){ }
	public PacketSyncScents(ScentsManager manager)
	{
		scentData = manager.write(new CompoundNBT());
	}
	
	public static PacketSyncScents decode(PacketBuffer par1Buffer)
	{
		PacketSyncScents packet = new PacketSyncScents();
		packet.scentData = par1Buffer.readCompoundTag();
		return packet;
	}
	
	public static void encode(PacketSyncScents msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeCompoundTag(msg.scentData);
	}
	
	public static void handle(PacketSyncScents msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(!context.getDirection().getReceptionSide().isServer())
		{
			PlayerEntity player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			ScentsManager manager = VariousOddities.proxy.getScentsManager(player.getEntityWorld());
			manager.read(msg.scentData);
		}
		
		context.setPacketHandled(true);
	}
}
