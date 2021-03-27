package com.lying.variousoddities.network;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler
{
	private static final String PROTOCOL = "1";
	public static final SimpleChannel HANDLER = 
			NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(Reference.ModInfo.MOD_ID, "chan"))
			.clientAcceptedVersions(PROTOCOL::equals)
			.serverAcceptedVersions(PROTOCOL::equals)
			.networkProtocolVersion(() -> PROTOCOL)
			.simpleChannel();
	
	private PacketHandler(){ }
	
	public static void init()
	{
		int id = 0;
		HANDLER.registerMessage(id++, PacketSettlementData.class, PacketSettlementData::encode, PacketSettlementData::decode, PacketSettlementData::handle);
		HANDLER.registerMessage(id++, PacketTileUpdate.class, PacketTileUpdate::encode, PacketTileUpdate::decode, PacketTileUpdate::handle);
		HANDLER.registerMessage(id++, PacketTypesData.class, PacketTypesData::encode, PacketTypesData::decode, PacketTypesData::handle);
	}
	
	/**
	 * Send message to all within 64 blocks that have this chunk loaded
	 */
	public static void sendToNearby(World world, BlockPos pos, Object toSend)
	{
		if(world instanceof ServerWorld)
		{
			ServerWorld ws = (ServerWorld) world;
			ws.getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(pos), false).filter(p -> p.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) < 64 * 64).forEach(p -> HANDLER.send(PacketDistributor.PLAYER.with(() -> p), toSend));
		}
	}
	
	public static void sendToNearby(World world, Entity e, Object toSend)
	{
		sendToNearby(world, e.getPosition(), toSend);
	}
	
	public static void sendToAll(ServerWorld world, Object toSend)
	{
		for(ServerPlayerEntity player : world.getPlayers())
			HANDLER.sendTo(toSend, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
	}
	
	public static void sendTo(ServerPlayerEntity playerMP, Object toSend)
	{
		HANDLER.sendTo(toSend, playerMP.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
	}
	
	public static void sendNonLocal(ServerPlayerEntity playerMP, Object toSend)
	{
		if(playerMP.server.isDedicatedServer() || !playerMP.getGameProfile().getName().equals(playerMP.server.getServerOwner()))
			sendTo(playerMP, toSend);
	}
	
	public static void sendToServer(Object msg)
	{
		HANDLER.sendToServer(msg);
	}
}
