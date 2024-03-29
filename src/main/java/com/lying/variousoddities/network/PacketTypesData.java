package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketTypesData
{
	private CompoundTag data = new CompoundTag();
	
	public PacketTypesData(){ }
	public PacketTypesData(CompoundTag dataIn)
	{
		data = dataIn;
	}
	
	public static PacketTypesData decode(FriendlyByteBuf par1Buffer)
	{
		PacketTypesData packet = new PacketTypesData();
		packet.data = par1Buffer.readNbt();
		return packet;
	}
	
	public static void encode(PacketTypesData msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeNbt(msg.data);
	}
	
	public static void handle(PacketTypesData msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		TypesManager manager = VariousOddities.proxy.getTypesManager();
		manager.clearCaches();
		manager.read(msg.data);
		context.setPacketHandled(true);
	}
}
