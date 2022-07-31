package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.world.settlement.Settlement;
import com.lying.variousoddities.world.savedata.SettlementManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketSettlementData
{
	private CompoundTag data = new CompoundTag();
	
	public PacketSettlementData(){ }
	public PacketSettlementData(CompoundTag dataIn)
	{
		data = dataIn;
	}
	
	public boolean isRemoval(){ return data.isEmpty(); }
	
	public int index()
	{
		int index = data.getInt("Index");
		data.remove("Index");
		return index;
	}
	
	public static PacketSettlementData decode(FriendlyByteBuf par1Buffer)
	{
		PacketSettlementData packet = new PacketSettlementData();
		packet.data = par1Buffer.readNbt();
		return packet;
	}
	
	public static void encode(PacketSettlementData msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeNbt(msg.data);
	}
	
	public static void handle(PacketSettlementData msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		SettlementManager manager;
		if(context.getDirection().getReceptionSide().isServer())
		{
			manager = SettlementManager.get(context.getSender().getLevel());
			manager.notifyObserver(context.getSender());
		}
		else
		{
			manager = VariousOddities.proxy.getSettlementManager(null);
			int index = msg.index();
			if(msg.isRemoval())
				manager.remove(index);
			else
			{
				Settlement settlement = SettlementManager.NBTToSettlement(msg.data);
				if(settlement != null)
					manager.add(index, settlement);
			}
		}
		context.setPacketHandled(true);
	}
}
