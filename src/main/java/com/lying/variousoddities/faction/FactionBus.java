package com.lying.variousoddities.faction;

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
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FactionBus
{
	public static boolean shouldFire(){ return ConfigVO.MOBS.factionSettings.repChanges(); }
	
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
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onLivingHurtEvent(LivingHurtEvent event)
	{
		if(!shouldFire()) return;
		
		Entity trueSource = event.getSource().getTrueSource();
		if(trueSource != null && trueSource instanceof PlayerEntity && !EntityPredicates.CAN_AI_TARGET.test(trueSource))
			if(event.getEntityLiving().getRNG().nextInt(3) == 0)
				FactionReputation.changePlayerReputation((PlayerEntity)trueSource, event.getEntityLiving(), -1);
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onLivingDamageEvent(LivingDamageEvent event)
	{
		if(!shouldFire()) return;
		
		Entity trueSource = event.getSource().getTrueSource();
		if(trueSource != null && trueSource instanceof PlayerEntity && !EntityPredicates.CAN_AI_TARGET.test(trueSource) && event.getAmount() > 0F)
			FactionReputation.changePlayerReputation((PlayerEntity)trueSource, event.getEntityLiving(), -1);
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
						FactionReputation.changePlayerReputation((PlayerEntity)trueSource, victim, -(1 + victim.getRNG().nextInt(3)));
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
					FactionReputation.changePlayerReputation((PlayerEntity)trueSource, attackTarget, 1 + victim.getRNG().nextInt(3));
					return;
				}
			}
			
			for(Entity living : victim.getEntityWorld().getEntitiesInAABBexcluding(victim, victim.getBoundingBox().grow(32D), MOB_FACTIONS))
			{
				LivingEntity livingBase = (LivingEntity)living;
				if(FactionReputation.getFaction(livingBase) != null && livingBase.getRevengeTarget() == victim && livingBase.canEntityBeSeen(victim))
				{
					FactionReputation.changePlayerReputation((PlayerEntity)trueSource, victim, -(1 + victim.getRNG().nextInt(3)));
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
//			FactionReputation.changePlayerReputation(event.getPlayerEntity(), trader, 1);
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
						FactionReputation.addPlayerReputation(player, faction, 1, mob);
		}
	}
}
