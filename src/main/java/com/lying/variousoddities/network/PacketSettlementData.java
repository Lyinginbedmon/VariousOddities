package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.world.settlement.Settlement;
import com.lying.variousoddities.world.savedata.SettlementManager;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSettlementData
{
	private CompoundNBT data = new CompoundNBT();
	
	public PacketSettlementData(){ }
	public PacketSettlementData(CompoundNBT dataIn)
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
	
	public static PacketSettlementData decode(PacketBuffer par1Buffer)
	{
		PacketSettlementData packet = new PacketSettlementData();
		packet.data = par1Buffer.readCompoundTag();
		return packet;
	}
	
	public static void encode(PacketSettlementData msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeCompoundTag(msg.data);
	}
	
	public static void handle(PacketSettlementData msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		SettlementManager manager;
		if(context.getDirection().getReceptionSide().isServer())
		{
			manager = SettlementManager.get(context.getSender().getServerWorld());
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
