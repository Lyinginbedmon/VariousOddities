package com.lying.variousoddities.species.abilities;

import java.util.Optional;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTeleportEvent;

public class AbilityTeleportToHome extends ActivatedAbility
{
	public AbilityTeleportToHome()
	{
		super(Reference.Values.TICKS_PER_MINUTE);
	}
	
	protected Nature getDefaultNature(){ return Nature.SPELL_LIKE; }
	
	public Type getType(){ return Ability.Type.UTILITY; }
	
	public boolean canTrigger(LivingEntity entity)
	{
		if(entity.getType() == EntityType.PLAYER)
		{
			Level world = entity.getLevel();
			if(!world.isClientSide)
			{
				ResourceKey<Level> spawnDim = ((ServerPlayer)entity).getRespawnDimension();
				if(world.dimension() != spawnDim)
					return false;
			}
		}
		else if(entity instanceof Monster)
		{
			Monster mob = (Monster)entity;
			if(mob.getSleepingPos().equals(BlockPos.ZERO))
				return false;
			
			LivingData data = LivingData.forEntity(mob);
			if(!data.getHomeDimension().equals(mob.getLevel().dimension().location()))
				return false;
		}
		else
			return false;
		
		return super.canTrigger(entity);
	}
	
	public void trigger(LivingEntity entity, Dist side)
	{
		if(side != Dist.CLIENT)
		{
			ServerLevel world = (ServerLevel)entity.getLevel();
			if(entity.getType() == EntityType.PLAYER)
			{
				ServerPlayer player = (ServerPlayer)entity;
				ResourceKey<Level> spawnDim = player.getRespawnDimension();
				ServerLevel destWorld = world;
				if(destWorld.dimension() != spawnDim)
					destWorld = destWorld.getServer().getLevel(spawnDim);
				
				if(destWorld != null)
				{
					BlockPos spawnPoint = player.getRespawnPosition();
					if(spawnPoint != null)
					{
						Optional<Vec3> optional = Player.findRespawnPositionAndUseSpawnBlock(destWorld, spawnPoint, player.getRespawnAngle(), false, true);
						if(optional.isPresent())
						{
							Vec3 pos = optional.get();
				            doTeleport(player, world, destWorld, pos.x, pos.y, pos.z);
							return;
				        }
					}
					spawnPoint = destWorld.getSharedSpawnPos();
					
					if(spawnPoint != null)
						doTeleport(player, world, destWorld, spawnPoint.getX() + 0.5D, spawnPoint.getY(), spawnPoint.getZ() + 0.5D);
				}
			}
		}
	}
	
	private void doTeleport(LivingEntity entity, Level world, Level destWorld, double x, double y, double z)
	{
		destWorld.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1F, 0.2F + entity.getRandom().nextFloat() * 0.8F);
		
		EntityTeleportEvent.EnderEntity event = new EntityTeleportEvent.EnderEntity(entity, x, y, z);
		if(MinecraftForge.EVENT_BUS.post(event))
		{
			putOnCooldown(entity);
			return;
		}
		
		entity.stopRiding();
		if(entity.isSleeping())
			entity.stopSleeping();
		
		if(world != destWorld)
		{
			((ServerLevel)destWorld).getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, new ChunkPos(new BlockPos(x, y, z)), 1, entity.getId());
			
			if(entity.getType() == EntityType.PLAYER)
				((ServerPlayer)entity).teleportTo((ServerLevel)destWorld, x, y, z, entity.getYRot(), entity.getXRot());
			else
				;
		}
		else
			entity.setPos(x, y, z);
		
		if(entity.fallDistance > 0.0F)
			entity.fallDistance = 0.0F;
		
		destWorld.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1F, 0.2F + entity.getRandom().nextFloat() * 0.8F);
		putOnCooldown(entity);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityTeleportToHome();
		}
	}
}
