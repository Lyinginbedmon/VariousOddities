package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.client.gui.GuiHandler;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketMobLoseTrack
{
	private int duration;
	
	public PacketMobLoseTrack(){ this(Reference.Values.TICKS_PER_SECOND); }
	public PacketMobLoseTrack(int ticksIn)
	{
		duration = ticksIn;
	}
	
	public static PacketMobLoseTrack decode(PacketBuffer par1Buffer)
	{
		return new PacketMobLoseTrack(par1Buffer.readInt());
	}
	
	public static void encode(PacketMobLoseTrack msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeInt(msg.duration);
	}
	
	public static void handle(PacketMobLoseTrack msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isClient())
			GuiHandler.trackingEyeTicks = msg.duration;
		context.setPacketHandled(true);
	}
}
