package com.lying.variousoddities.network.gui;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.network.PacketSettlementData;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketOpenGui
{
	private UUID playerUUID;
	private int guiID;
	
	private int[] values = new int[]{0,0,0};
	
	public PacketOpenGui(){ }
	public PacketOpenGui(PlayerEntity player, int gui)
	{
		playerUUID = player.getUniqueID();
		guiID = gui;
	}
	public PacketOpenGui(PlayerEntity player, int gui, int x, int y, int z)
	{
		this(player, gui);
		values[0] = x;
		values[1] = y;
		values[2] = z;
	}
	
	protected void decode(PacketBuffer par1Buffer) throws IOException
	{
		playerUUID = par1Buffer.readUniqueId();
		guiID = par1Buffer.readInt();
		values = par1Buffer.readVarIntArray(3);
	}
	
	protected void encode(PacketBuffer par1Buffer) throws IOException
	{
		par1Buffer.writeUniqueId(playerUUID);
		par1Buffer.writeInt(guiID);
		par1Buffer.writeVarIntArray(values);
	}
	
	public static void handle(PacketSettlementData msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			
		}
		else
		{
			
		}
	}
	
//	public void process(PlayerEntity par1Player, Dist par2Side)
//	{
//		PlayerEntity targetPlayer = par1Player;
//		World world = par1Player.getEntityWorld();
//		if(par2Side != Dist.CLIENT)
//			targetPlayer = world.getPlayerEntityByUUID(playerUUID);
//		
//		if(targetPlayer != null)
//			targetPlayer.openGui(VariousOddities.instance, guiID, world, values[0], values[1], values[2]);
//	}
}
