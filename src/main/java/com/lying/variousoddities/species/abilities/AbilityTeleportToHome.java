package com.lying.variousoddities.species.abilities;

import java.util.Optional;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

public class AbilityTeleportToHome extends ActivatedAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "teleport_to_home");
	
	public AbilityTeleportToHome()
	{
		super(REGISTRY_NAME, Reference.Values.TICKS_PER_MINUTE);
	}
	
	protected Nature getDefaultNature(){ return Nature.SPELL_LIKE; }
	
	public Type getType(){ return Ability.Type.UTILITY; }
	
	public boolean canTrigger(LivingEntity entity)
	{
		if(entity.getType() == EntityType.PLAYER)
		{
			World world = entity.getEntityWorld();
			if(!world.isRemote)
			{
				RegistryKey<World> spawnDim = ((ServerPlayerEntity)entity).func_241141_L_();
				if(world.getDimensionKey() != spawnDim)
					return false;
			}
		}
		else if(entity instanceof MobEntity)
		{
			MobEntity mob = (MobEntity)entity;
			if(mob.getHomePosition().equals(BlockPos.ZERO))
				return false;
			
			LivingData data = LivingData.forEntity(mob);
			if(!data.getHomeDimension().equals(mob.getEntityWorld().getDimensionKey().getLocation()))
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
			ServerWorld world = (ServerWorld)entity.getEntityWorld();
			if(entity.getType() == EntityType.PLAYER)
			{
				ServerPlayerEntity player = (ServerPlayerEntity)entity;
				RegistryKey<World> spawnDim = player.func_241141_L_();
				ServerWorld destWorld = world;
				if(destWorld.getDimensionKey() != spawnDim)
					destWorld = destWorld.getServer().getWorld(spawnDim);
				
				if(destWorld != null)
				{
					BlockPos spawnPoint = player.func_241140_K_();
					if(spawnPoint != null)
					{
						Optional<Vector3d> optional = PlayerEntity.func_242374_a(destWorld, spawnPoint, player.func_242109_L(), false, true);
						if(optional.isPresent())
						{
							Vector3d pos = optional.get();
				            doTeleport(player, world, destWorld, pos.getX(), pos.getY(), pos.getZ());
							return;
				        }
					}
					spawnPoint = destWorld.getSpawnPoint();
					
					if(spawnPoint != null)
						doTeleport(player, world, destWorld, spawnPoint.getX() + 0.5D, spawnPoint.getY(), spawnPoint.getZ() + 0.5D);
				}
			}
		}
	}
	
	private void doTeleport(LivingEntity entity, World world, World destWorld, double x, double y, double z)
	{
		destWorld.playSound(null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1F, 0.2F + entity.getRNG().nextFloat() * 0.8F);
		
		EnderTeleportEvent event = new EnderTeleportEvent(entity, x, y, z, 0F);
		if(MinecraftForge.EVENT_BUS.post(event))
		{
			putOnCooldown(entity);
			return;
		}
		
		entity.stopRiding();
		if(entity.isSleeping())
			entity.wakeUp();
		
		if(world != destWorld)
		{
			((ServerChunkProvider)destWorld.getChunkProvider()).registerTicket(TicketType.POST_TELEPORT, new ChunkPos(new BlockPos(x, y, z)), 1, entity.getEntityId());
			
			if(entity.getType() == EntityType.PLAYER)
				((ServerPlayerEntity)entity).teleport((ServerWorld)destWorld, x, y, z, entity.rotationYaw, entity.rotationPitch);
			else
				;
		}
		else
			entity.setPositionAndUpdate(x, y, z);
		
		if(entity.fallDistance > 0.0F)
			entity.fallDistance = 0.0F;
		
		destWorld.playSound(null, x, y, z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1F, 0.2F + entity.getRNG().nextFloat() * 0.8F);
		putOnCooldown(entity);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityTeleportToHome();
		}
	}
}
