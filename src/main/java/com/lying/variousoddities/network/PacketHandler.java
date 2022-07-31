package com.lying.variousoddities.network;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

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
		HANDLER.registerMessage(id++, PacketSyncAir.class, PacketSyncAir::encode, PacketSyncAir::decode, PacketSyncAir::handle);
		HANDLER.registerMessage(id++, PacketAbilityActivate.class, PacketAbilityActivate::encode, PacketAbilityActivate::decode, PacketAbilityActivate::handle);
		HANDLER.registerMessage(id++, PacketSyncAbilities.class, PacketSyncAbilities::encode, PacketSyncAbilities::decode, PacketSyncAbilities::handle);
		HANDLER.registerMessage(id++, PacketAbilityCooldown.class, PacketAbilityCooldown::encode, PacketAbilityCooldown::decode, PacketAbilityCooldown::handle);
		HANDLER.registerMessage(id++, PacketAbilityFavourite.class, PacketAbilityFavourite::encode, PacketAbilityFavourite::decode, PacketAbilityFavourite::handle);
		HANDLER.registerMessage(id++, PacketParalysisResignation.class, PacketParalysisResignation::encode, PacketParalysisResignation::decode, PacketParalysisResignation::handle);
		HANDLER.registerMessage(id++, PacketSyncTypesCustom.class, PacketSyncTypesCustom::encode, PacketSyncTypesCustom::decode, PacketSyncTypesCustom::handle);
		HANDLER.registerMessage(id++, PacketSyncLivingData.class, PacketSyncLivingData::encode, PacketSyncLivingData::decode, PacketSyncLivingData::handle);
		HANDLER.registerMessage(id++, PacketBonusJump.class, PacketBonusJump::encode, PacketBonusJump::decode, PacketBonusJump::handle);
		HANDLER.registerMessage(id++, PacketSpeciesOpenScreen.class, PacketSpeciesOpenScreen::encode, PacketSpeciesOpenScreen::decode, PacketSpeciesOpenScreen::handle);
		HANDLER.registerMessage(id++, PacketSpeciesSelected.class, PacketSpeciesSelected::encode, PacketSpeciesSelected::decode, PacketSpeciesSelected::handle);
		HANDLER.registerMessage(id++, PacketSyncSpecies.class, PacketSyncSpecies::encode, PacketSyncSpecies::decode, PacketSyncSpecies::handle);
		HANDLER.registerMessage(id++, PacketVisualPotion.class, PacketVisualPotion::encode, PacketVisualPotion::decode, PacketVisualPotion::handle);
		HANDLER.registerMessage(id++, PacketSyncVisualPotions.class, PacketSyncVisualPotions::encode, PacketSyncVisualPotions::decode, PacketSyncVisualPotions::handle);
		HANDLER.registerMessage(id++, PacketAbilityRemove.class, PacketAbilityRemove::encode, PacketAbilityRemove::decode, PacketAbilityRemove::handle);
		HANDLER.registerMessage(id++, PacketPetrifying.class, PacketPetrifying::encode, PacketPetrifying::decode, PacketPetrifying::handle);
		HANDLER.registerMessage(id++, PacketMountGui.class, PacketMountGui::encode, PacketMountGui::decode, PacketMountGui::handle);
		HANDLER.registerMessage(id++, PacketSit.class, PacketSit::encode, PacketSit::decode, PacketSit::handle);
		HANDLER.registerMessage(id++, PacketSyncScents.class, PacketSyncScents::encode, PacketSyncScents::decode, PacketSyncScents::handle);
		HANDLER.registerMessage(id++, PacketAddScent.class, PacketAddScent::encode, PacketAddScent::decode, PacketAddScent::handle);
		HANDLER.registerMessage(id++, PacketSyncPlayerData.class, PacketSyncPlayerData::encode, PacketSyncPlayerData::decode, PacketSyncPlayerData::handle);
		HANDLER.registerMessage(id++, PacketDeadDeath.class, PacketDeadDeath::encode, PacketDeadDeath::decode, PacketDeadDeath::handle);
		HANDLER.registerMessage(id++, PacketUnconsciousAwaken.class, PacketUnconsciousAwaken::encode, PacketUnconsciousAwaken::decode, PacketUnconsciousAwaken::handle);
		HANDLER.registerMessage(id++, PacketBludgeoned.class, PacketBludgeoned::encode, PacketBludgeoned::decode, PacketBludgeoned::handle);
		HANDLER.registerMessage(id++, PacketMobLoseTrack.class, PacketMobLoseTrack::encode, PacketMobLoseTrack::decode, PacketMobLoseTrack::handle);
	}
	
	/**
	 * Send message to all within 64 blocks that have this chunk loaded
	 */
	public static void sendToNearby(Level world, BlockPos pos, Object toSend)
	{
		if(world instanceof ServerLevel)
		{
			ServerLevel ws = (ServerLevel) world;
			ws.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false).stream().filter(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 64 * 64).forEach(p -> HANDLER.send(PacketDistributor.PLAYER.with(() -> p), toSend));
		}
	}
	
	public static void sendToNearby(Level world, Entity e, Object toSend)
	{
		sendToNearby(world, e.blockPosition(), toSend);
	}
	
	public static void sendToAll(ServerLevel world, Object toSend)
	{
		for(ServerPlayer player : world.players())
			HANDLER.sendTo(toSend, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}
	
	public static void sendTo(ServerPlayer playerMP, Object toSend)
	{
		HANDLER.sendTo(toSend, playerMP.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}
	
	public static void sendNonLocal(ServerPlayer playerMP, Object toSend)
	{
		if(playerMP.server.isDedicatedServer() || !playerMP.server.isSingleplayerOwner(playerMP.getGameProfile()))
			sendTo(playerMP, toSend);
	}
	
	public static void sendToServer(Object msg)
	{
		HANDLER.sendToServer(msg);
	}
}
