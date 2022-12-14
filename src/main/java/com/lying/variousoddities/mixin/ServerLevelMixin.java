package com.lying.variousoddities.mixin;

import java.util.List;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.entity.AbstractCrab;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level
{
	protected ServerLevelMixin(MinecraftServer p_214999_, Executor p_215000_, LevelStorageSource.LevelStorageAccess p_215001_, 
			ServerLevelData p_215002_, ResourceKey<Level> p_215003_, LevelStem p_215004_, ChunkProgressListener p_215005_, boolean p_215006_, 
			long p_215007_, List<CustomSpawner> p_215008_, boolean p_215009_) 
	{
		super(p_215002_, p_215003_, p_215004_.typeHolder(), p_214999_::getProfiler, false, p_215006_, p_215007_, p_214999_.getMaxChainedNeighborUpdates());
	}
	
	@Inject(method = "playEvent", at = @At("HEAD"))
	private void crabDance(Player player, int type, BlockPos pos, int vars, CallbackInfo callbackInfo)
	{
		if(type == 1010)
			AbstractCrab.startParty((ServerLevel)(Object)this, pos, vars != 0);
	}
}
