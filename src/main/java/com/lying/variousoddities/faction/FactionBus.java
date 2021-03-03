package com.lying.variousoddities.faction;

import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.lying.variousoddities.config.ConfigVO;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FactionBus
{
	public static boolean shouldFire(){ return ConfigVO.MOBS.factionSettings.repChanges(); }
	
	/** Returns true if the entity is associated with a faction */
	private static final Predicate<Entity> ALL_FACTIONS = new Predicate<Entity>()
			{
				public boolean apply(Entity input)
				{
					if(!input.isAlive()) return false;
					return input instanceof LivingEntity && FactionReputation.getFaction((LivingEntity)input) != null;
				}
			};
	private static final Predicate<Entity> MOB_FACTIONS = new Predicate<Entity>()
			{
				public boolean apply(Entity input)
				{
					return input instanceof LivingEntity && FactionBus.ALL_FACTIONS.apply(input);
				}
			};
	
//	@SubscribeEvent(priority = EventPriority.LOWEST)
//	public static void onLivingHurtEvent(LivingHurtEvent event)
//	{
//		if(!shouldFire()) return;
//		
//		Entity trueSource = event.getSource().getTrueSource();
//		if(trueSource != null && trueSource instanceof PlayerEntity && !EntityPredicates.CAN_AI_TARGET.test(trueSource))
//			if(event.getEntityLiving().getRNG().nextInt(3) == 0)
//				FactionReputation.changePlayerReputation((PlayerEntity)trueSource, event.getEntityLiving(), -1);
//	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onLivingDamageEvent(LivingDamageEvent event)
	{
		if(!shouldFire()) return;
		
		Entity trueSource = event.getSource().getTrueSource();
		if(trueSource != null && trueSource instanceof PlayerEntity && !EntityPredicates.CAN_AI_TARGET.test(trueSource) && event.getAmount() > 0F)
			ReputationChange.HURT.applyTo((PlayerEntity)trueSource, event.getEntityLiving(), event.getEntityLiving().getRNG());
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onLivingDeathEvent(LivingDeathEvent event)
	{
		if(!shouldFire()) return;
		
		Entity trueSource = event.getSource().getTrueSource();
		if(trueSource != null && trueSource instanceof PlayerEntity)
		{
			LivingEntity victim = event.getEntityLiving();
			
			// Decrement reputation with the victim's faction if there is at least one witness
			String victimFaction = FactionReputation.getFaction(event.getEntityLiving());
			if(victimFaction != null)
			{
				for(Entity living : victim.getEntityWorld().getEntitiesInAABBexcluding(victim, victim.getBoundingBox().grow(32D), ALL_FACTIONS))
				{
					LivingEntity livingBase = (LivingEntity)living;
					if(victimFaction.equals(FactionReputation.getFaction(livingBase)) && livingBase.canEntityBeSeen(victim))
					{
						ReputationChange.KILL.applyTo((PlayerEntity)trueSource, victim, victim.getRNG());
						break;
					}
				}
			}
			
			// Increment reputation of victim's attack target OR one mob fleeing from the victim, for saving their life
			if(victim instanceof MobEntity && ((MobEntity)victim).getAttackTarget() != null)
			{
				LivingEntity attackTarget = ((MobEntity)victim).getAttackTarget();
				String attackFaction = FactionReputation.getFaction(attackTarget);
				if(attackFaction != null)
				{
					ReputationChange.PROTECT.applyTo((PlayerEntity)trueSource, attackTarget, victim.getRNG());
					return;
				}
			}
			
			for(Entity living : victim.getEntityWorld().getEntitiesInAABBexcluding(victim, victim.getBoundingBox().grow(32D), MOB_FACTIONS))
			{
				LivingEntity livingBase = (LivingEntity)living;
				if(FactionReputation.getFaction(livingBase) != null && livingBase.getRevengeTarget() == victim && livingBase.canEntityBeSeen(victim))
				{
					ReputationChange.KILL.applyTo((PlayerEntity)trueSource, victim, victim.getRNG());
					break;
				}
			}
		}
	}
	
//	@SubscribeEvent
//	public static void onPlayerTradeEvent(PlayerTradeEvent event)
//	{
//		if(!shouldFire()) return;
//		
//		LivingEntity trader = event.getTrader();
//		if(trader != null && trader.getRNG().nextInt(3) == 0)
//			FactionReputation.changePlayerReputation(event.getPlayerEntity(), trader, ReputationChange.TRADE, trader.getRNG());
//	}
	
	@SubscribeEvent
	public static void onLivingHealEvent(LivingHealEvent event)
	{
		if(!shouldFire()) return;
		
		LivingEntity mob = event.getEntityLiving();
		if(mob != null && mob instanceof MobEntity)
		{
			MobEntity living = (MobEntity)mob;
			String faction = FactionReputation.getFaction(living);
			if(living.getHealth() < living.getMaxHealth() && faction != null)
				for(PlayerEntity player : living.getEntityWorld().getEntitiesWithinAABB(PlayerEntity.class, living.getBoundingBox().grow(8D), EntityPredicates.CAN_AI_TARGET))
					if(living.getEntitySenses().canSee(player) && player != living.getRevengeTarget())
						ReputationChange.HEAL.applyTo(player, faction, mob.getRNG(), mob);
		}
	}
	
	public static enum ReputationChange
	{
		HURT(-1),
		KILL(-1, -3),
		PROTECT(1, 3),
		TRADE(1),
		HEAL(1),
		COMMAND(0);
		
		private final int min, max;
		
		private ReputationChange(int val1, int val2)
		{
			min = Math.min(val1, val2);
			max = Math.max(val1, val2);
		}
		
		private ReputationChange(int val)
		{
			this(val, val);
		}
		
		public int volume(Random rand)
		{
			if(max != min)
				return min + rand.nextInt(max - min);
			else
				return min;
		}
		
		public void applyTo(PlayerEntity player, LivingEntity mob, Random rand)
		{
			applyTo(player, FactionReputation.getFaction(mob), rand, mob);
		}
		
		public void applyTo(PlayerEntity player, String faction, Random rand, @Nullable LivingEntity mob)
		{
			FactionReputation.changePlayerReputation(player, faction, this, volume(rand), mob);
		}
	}
}
