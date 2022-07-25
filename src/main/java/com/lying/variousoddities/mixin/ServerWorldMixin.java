package com.lying.variousoddities.mixin;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lying.variousoddities.entity.AbstractCrab;

import net.minecraft.core.BlockPos;
import net.minecraft.profiler.IProfiler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.storage.ISpawnWorldInfo;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin extends Level
{
	@Shadow
	private final Map<UUID, Entity> entitiesByUuid = Maps.newHashMap();
	
	protected ServerWorldMixin(ISpawnWorldInfo worldInfo, RegistryKey<Level> dimension, DimensionType dimensionType,
			Supplier<IProfiler> profiler, boolean isRemote, boolean isDebug, long seed)
	{
		super(worldInfo, dimension, dimensionType, profiler, isRemote, isDebug, seed);
	}
	
	@Inject(method = "playEvent", at = @At("HEAD"))
	private void crabDance(Player player, int type, BlockPos pos, int vars, CallbackInfo callbackInfo)
	{
		if(type == 1010)
			AbstractCrab.startParty((ServerLevel)(Object)this, pos, vars != 0);
	}
	
	public List<LivingEntity> getLoadedCreatures(Predicate<LivingEntity> predicate)
	{
		List<LivingEntity> creatures = Lists.newArrayList();
		entitiesByUuid.values().forEach((entity) -> 
			{
				if(entity instanceof LivingEntity && entity.isAlive() && predicate.apply((LivingEntity)entity))
					creatures.add((LivingEntity)entity); 
			});
		return creatures;
	}
}
