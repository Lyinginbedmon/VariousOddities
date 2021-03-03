package com.lying.variousoddities.utility;

import java.util.List;
import java.util.Random;

import com.lying.variousoddities.api.event.LivingWakeUpEvent;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.ai.EntityAISleep;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.entity.hostile.EntityRatGiant;
import com.lying.variousoddities.entity.passive.EntityRat;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.potion.PotionSleep;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VOBusServer
{
	@SubscribeEvent
	public static void onAttachCapabilityEvent(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject().getType() == EntityType.PLAYER)
			event.addCapability(PlayerData.IDENTIFIER, new PlayerData());
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
	 * Reduces the refractory period of nearby goblins when<br>
	 * a. any goblin is slain or <br>
	 * b. a goblin slays any mob (with big bonus for slaying a player)
	 * @param event
	 */
	@SubscribeEvent
	public static void onLivingDeathEvent(LivingDeathEvent event)
	{
		LivingEntity victim = event.getEntityLiving();
		DamageSource cause = event.getSource();
		if(victim instanceof EntityGoblin)
			reduceRefractory(victim, 1000);
		else if(cause instanceof EntityDamageSource && ((EntityDamageSource)cause).getTrueSource() instanceof EntityGoblin)
			reduceRefractory(victim, victim instanceof PlayerEntity ? 4000 : 500);
	}
	
	private static void reduceRefractory(LivingEntity goblinIn, int amount)
	{
		List<EntityGoblin> nearbyGoblins = goblinIn.getEntityWorld().getEntitiesWithinAABB(EntityGoblin.class, goblinIn.getBoundingBox().grow(10));
		for(EntityGoblin goblin : nearbyGoblins)
			if(goblin.isAlive() && goblin.getGrowingAge() > 0)
				goblin.setGrowingAge(Math.max(0, goblin.getGrowingAge() - amount));
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
