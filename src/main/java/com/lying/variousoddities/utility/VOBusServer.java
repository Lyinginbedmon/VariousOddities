package com.lying.variousoddities.utility;

import java.util.List;
import java.util.Random;

import com.lying.variousoddities.api.event.CreatureTypeEvent.TypeGetEntityTypesEvent;
import com.lying.variousoddities.api.event.FireworkExplosionEvent;
import com.lying.variousoddities.api.event.LivingWakeUpEvent;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.AbstractGoblinWolf;
import com.lying.variousoddities.entity.ai.EntityAISleep;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.entity.hostile.EntityRatGiant;
import com.lying.variousoddities.entity.passive.EntityGhastling;
import com.lying.variousoddities.entity.passive.EntityRat;
import com.lying.variousoddities.entity.passive.EntityWorg;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSyncAir;
import com.lying.variousoddities.potion.PotionSleep;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VOBusServer
{
	@SubscribeEvent
	public static void onAttachCapabilityEvent(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof LivingEntity)
		{
			LivingData dataLiving = new LivingData();
			event.addCapability(LivingData.IDENTIFIER, dataLiving);
			event.addListener(dataLiving.handler()::invalidate);
			
			if(event.getObject().getType() == EntityType.PLAYER)
			{
				PlayerData dataPlayer = new PlayerData();
				event.addCapability(PlayerData.IDENTIFIER, dataPlayer);
				event.addListener(dataPlayer.handler()::invalidate);
			}
		}
	}
	
	@SubscribeEvent
	public static void onChangeDimensionEvent(EntityTravelToDimensionEvent event)
	{
		if(!event.getEntity().getEntityWorld().isRemote && event.getEntity().getType() == EntityType.PLAYER)
		{
			PlayerEntity player = (PlayerEntity)event.getEntity();
			LivingData data = LivingData.forEntity(player);
			if(data == null)
				return;
			PacketHandler.sendTo((ServerPlayerEntity)player, new PacketSyncAir(data.getAir()));
			data.getAbilities().markDirty();
		}
	}
	
	@SubscribeEvent
	public static void onPlayerLogInEvent(PlayerLoggedInEvent event)
	{
		LivingData data = LivingData.forEntity(event.getPlayer());
		if(data != null)
		{
			PacketHandler.sendTo((ServerPlayerEntity)event.getPlayer(), new PacketSyncAir(data.getAir()));
			data.getAbilities().markDirty();
		}
	}
	
	@SubscribeEvent
	public static void onPlayerCloneEvent(PlayerEvent.Clone event)
	{
		LivingData oldData = LivingData.forEntity(event.getOriginal());
		LivingData newData = LivingData.forEntity(event.getPlayer());
		if(oldData != null && newData != null)
		{
			newData.getAbilities().copy(oldData.getAbilities());
			newData.getAbilities().markDirty();
		}
	}
	
	@SubscribeEvent
	public static void onPlayerRespawnEvent(PlayerRespawnEvent event)
	{
		LivingData data = LivingData.forEntity(event.getPlayer());
		if(data != null)
			data.getAbilities().markDirty();
	}
	
	@SubscribeEvent
	public static void addEntityBehaviours(EntityJoinWorldEvent event)
	{
		Entity theEntity = event.getEntity();
		if(theEntity.getType() == EntityType.CAT || theEntity.getType() == EntityType.OCELOT)
		{
			MobEntity feline = (MobEntity)theEntity;
			
			if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(VOEntities.RAT))
				feline.targetSelector.addGoal(1, new NearestAttackableTargetGoal<EntityRat>(feline, EntityRat.class, true));
			
			if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(VOEntities.RAT_GIANT))
				feline.targetSelector.addGoal(1, new NearestAttackableTargetGoal<EntityRatGiant>(feline, EntityRatGiant.class, true));
		}
		
		if(theEntity instanceof MobEntity)
		{
			MobEntity living = (MobEntity)theEntity;
			living.goalSelector.addGoal(1, new EntityAISleep(living));
		}
	}
	
	/**
	 * Occasionally spawns ghastlings when a ghast is killed by a fireball.
	 * @param event
	 */
	@SubscribeEvent
	public static void onGhastSenderEvent(LivingDeathEvent event)
	{
		LivingEntity ghast = event.getEntityLiving();
		if(ghast.getType() == EntityType.GHAST)
		{
			DamageSource source = event.getSource();
			if(source.getImmediateSource() instanceof FireballEntity && source.getTrueSource() instanceof PlayerEntity)
			{
				Random rand = ghast.getRNG();
				World world = ghast.getEntityWorld();
				if(rand.nextInt(15) == 0)
					for(int i=0; i<rand.nextInt(3); i++)
					{
						EntityGhastling ghastling = VOEntities.GHASTLING.create(world);
						ghastling.setLocationAndAngles(ghast.getPosX(), ghast.getPosY(), ghast.getPosZ(), rand.nextFloat() * 360F, 0F);
						world.addEntity(ghastling);
					}
			}
		}
	}
	
	/**
	 * Reduces the refractory period of nearby goblins when<br>
	 * a. any goblin is slain or <br>
	 * b. a goblin slays any mob (with big bonus for slaying a player)
	 * @param event
	 */
	@SubscribeEvent
	public static void onDeathNearGoblinEvent(LivingDeathEvent event)
	{
		LivingEntity victim = event.getEntityLiving();
		DamageSource cause = event.getSource();
		if(victim instanceof EntityGoblin)
			reduceRefractory(victim, 1000);
		else if(cause instanceof EntityDamageSource && ((EntityDamageSource)cause).getTrueSource() instanceof EntityGoblin)
			reduceRefractory(victim, victim instanceof PlayerEntity ? 4000 : 500);
	}
	
	@SubscribeEvent
	public static void onGoblinWolfKillEvent(LivingDeathEvent event)
	{
		LivingEntity victim = event.getEntityLiving();
		DamageSource cause = event.getSource();
		if(cause instanceof EntityDamageSource && ((EntityDamageSource)cause).getTrueSource() instanceof AbstractGoblinWolf)
			((AbstractGoblinWolf)cause.getTrueSource()).heal(2F + victim.getRNG().nextFloat() * 3F);
	}
	
	private static void reduceRefractory(LivingEntity goblinIn, int amount)
	{
		List<EntityGoblin> nearbyGoblins = goblinIn.getEntityWorld().getEntitiesWithinAABB(EntityGoblin.class, goblinIn.getBoundingBox().grow(10));
		for(EntityGoblin goblin : nearbyGoblins)
			if(goblin.isAlive() && goblin.getGrowingAge() > 0)
				goblin.setGrowingAge(Math.max(0, goblin.getGrowingAge() - amount));
	}
	
	@SubscribeEvent
	public static void onLightingSpawnEvent(EntityJoinWorldEvent event)
	{
		if(event.getEntity().getType() == EntityType.LIGHTNING_BOLT)
		{
			BlockPos pos = event.getEntity().getPosition();
			AxisAlignedBB bounds = new AxisAlignedBB(0, 0, 0, 1, 256, 1).offset(pos.getX(), 0, pos.getZ()).grow(128, 0, 128);
			for(EntityWorg worg : event.getEntity().getEntityWorld().getEntitiesWithinAABB(EntityWorg.class, bounds))
				worg.spook();
		}
	}
	
	@SubscribeEvent
	public static void onFireworkBlastEvent(FireworkExplosionEvent event)
	{
		ListNBT explosions = event.fireworkData() == null ? null : event.fireworkData().getList("Explosions", 10);
		if(explosions != null && !explosions.isEmpty())
		{
			AxisAlignedBB bounds = new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(event.position()).grow(16 * explosions.size());
			for(EntityWorg worg : event.world().getEntitiesWithinAABB(EntityWorg.class, bounds))
				worg.spook();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void applyNativeExtraplanar(TypeGetEntityTypesEvent event)
	{
		LivingEntity entity = event.getEntity();
		LivingData data = LivingData.forEntity(entity);
		if(data == null)
			return;
		
		if(data.getHomeDimension() != null)
		{
			List<EnumCreatureType> types = event.getTypes();
			ResourceLocation currentDim = entity.getEntityWorld().getDimensionKey().getLocation();
			if(currentDim.equals(data.getHomeDimension()))
			{
				if(!types.contains(EnumCreatureType.EXTRAPLANAR) && EnumCreatureType.NATIVE.canApplyTo(types))
					event.getTypes().add(EnumCreatureType.NATIVE);
			}
			else
			{
				if(!types.contains(EnumCreatureType.NATIVE) && EnumCreatureType.EXTRAPLANAR.canApplyTo(types))
					event.getTypes().add(EnumCreatureType.EXTRAPLANAR);
			}
		}
	}
	
	/**
	 * If a sleeping mob is harmed, wake them up.
	 * @param event
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onSleepingHurtEvent(LivingHurtEvent event)
	{
		if(event.getAmount() > 0F && !event.isCanceled())
		{
			LivingEntity hurtEntity = event.getEntityLiving();
			wakeupEntitiesAround(hurtEntity);
			
			EffectInstance sleepEffect = hurtEntity.getActivePotionEffect(VOPotions.SLEEP);
			int tier = (sleepEffect == null || sleepEffect.getDuration() <= 0) ? -1 : sleepEffect.getAmplifier();
			
			if(PotionSleep.isSleeping(hurtEntity) && tier < 1)
				if(!MinecraftForge.EVENT_BUS.post(new LivingWakeUpEvent(hurtEntity, true)))
				{
					if(hurtEntity instanceof PlayerEntity && ((PlayerEntity)hurtEntity).isSleeping())
						((PlayerEntity)hurtEntity).wakeUp();
					else if(hurtEntity instanceof LivingEntity)
						PotionSleep.setSleeping((LivingEntity)hurtEntity, false);
					hurtEntity.removePotionEffect(VOPotions.SLEEP);
				}
		}
	}
	
	public static void wakeupEntitiesAround(Entity source, double rangeXZ, double rangeY)
	{
		for(LivingEntity entity : source.getEntityWorld().getEntitiesWithinAABB(LivingEntity.class, source.getBoundingBox().grow(rangeXZ, rangeY, rangeXZ)))
		{
			if(entity == source || !PotionSleep.isSleeping(entity) || PotionSleep.hasSleepEffect(entity))
				continue;
			
			if(entity instanceof LivingEntity)
			{
				double wakeupChance = (new Random(entity.getUniqueID().getLeastSignificantBits())).nextDouble();
				if(entity.getRNG().nextDouble() < wakeupChance && !MinecraftForge.EVENT_BUS.post(new LivingWakeUpEvent(entity, true)))
					PotionSleep.setSleeping((LivingEntity)entity, false);
			}
			else if(entity instanceof PlayerEntity)
			{
				PlayerEntity player = (PlayerEntity)entity;
				if(!MinecraftForge.EVENT_BUS.post(new LivingWakeUpEvent(entity, true)))
					player.wakeUp();
			}
		}
	}
	
	public static void wakeupEntitiesAround(Entity source)
	{
		wakeupEntitiesAround(source, 6D, 2D);
	}
}
