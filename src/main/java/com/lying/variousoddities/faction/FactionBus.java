package com.lying.variousoddities.faction;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.lying.variousoddities.api.event.PlayerTradeEvent;
import com.lying.variousoddities.config.ConfigVO;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
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
					if(!input.isAlive())
						return false;
					return input instanceof Mob && FactionReputation.getFaction((Mob)input) != null;
				}
			};
	private static final Predicate<Entity> MOB_FACTIONS = new Predicate<Entity>()
			{
				public boolean apply(Entity input)
				{
					return input instanceof Mob && FactionBus.ALL_FACTIONS.apply(input);
				}
			};
	
//	@SubscribeEvent(priority = EventPriority.LOWEST)
//	public static void onLivingHurtEvent(LivingHurtEvent event)
//	{
//		if(!shouldFire()) return;
//		
//		Entity trueSource = event.getSource().getTrueSource();
//		if(trueSource != null && trueSource instanceof Player && !EntityPredicates.CAN_AI_TARGET.test(trueSource))
//			if(event.getEntityLiving().getRandom().nextInt(3) == 0)
//				FactionReputation.changePlayerReputation((Player)trueSource, event.getEntityLiving(), -1);
//	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onLivingDamageEvent(LivingDamageEvent event)
	{
		if(!shouldFire()) return;
		
		Entity trueSource = event.getSource().getEntity();
		if(trueSource != null && trueSource instanceof Player && !EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(trueSource) && event.getAmount() > 0F)
			ReputationChange.HURT.applyTo((Player)trueSource, event.getEntity(), event.getEntity().getRandom());
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onLivingDeathEvent(LivingDeathEvent event)
	{
		if(!shouldFire()) return;
		
		Entity trueSource = event.getSource().getEntity();
		if(trueSource != null && trueSource instanceof Player)
		{
			LivingEntity victim = event.getEntity();
			
			// Decrement reputation with the victim's faction if there is at least one witness
			String victimFaction = FactionReputation.getFaction(event.getEntity());
			if(victimFaction != null)
			{
				for(Entity living : victim.getLevel().getEntities(victim, victim.getBoundingBox().inflate(32D), ALL_FACTIONS))
				{
					Mob livingBase = (Mob)living;
					if(victimFaction.equals(FactionReputation.getFaction(livingBase)) && livingBase.hasLineOfSight(victim))
					{
						ReputationChange.KILL.applyTo((Player)trueSource, victim, victim.getRandom());
						break;
					}
				}
			}
			
			// Increment reputation of victim's attack target OR one mob fleeing from the victim, for saving their life
			if(victim instanceof Mob && ((Mob)victim).getTarget() != null)
			{
				LivingEntity attackTarget = ((Mob)victim).getTarget();
				String attackFaction = FactionReputation.getFaction(attackTarget);
				if(attackFaction != null)
				{
					ReputationChange.PROTECT.applyTo((Player)trueSource, attackTarget, victim.getRandom());
					return;
				}
			}
			
			for(Entity living : victim.getLevel().getEntities(victim, victim.getBoundingBox().inflate(32D), MOB_FACTIONS))
			{
				Mob livingBase = (Mob)living;
				if(FactionReputation.getFaction(livingBase) != null && livingBase.getLastHurtByMob() == victim && livingBase.hasLineOfSight(victim))
				{
					ReputationChange.KILL.applyTo((Player)trueSource, victim, victim.getRandom());
					break;
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerTradeEvent(PlayerTradeEvent event)
	{
		if(!shouldFire()) return;
		
		LivingEntity trader = event.getTrader();
		if(trader != null && trader.getRandom().nextInt(3) == 0)
			ReputationChange.TRADE.applyTo(event.getEntity(), trader, trader.getRandom());
	}
	
	@SubscribeEvent
	public static void onLivingHealEvent(LivingHealEvent event)
	{
		if(!shouldFire()) return;
		
		LivingEntity mob = event.getEntity();
		if(mob != null && mob instanceof Mob)
		{
			Mob living = (Mob)mob;
			String faction = FactionReputation.getFaction(living);
			if(living.getHealth() < living.getMaxHealth() && faction != null)
				for(Player player : living.getLevel().getEntitiesOfClass(Player.class, living.getBoundingBox().inflate(8D), EntitySelector.NO_CREATIVE_OR_SPECTATOR))
					if(living.hasLineOfSight(player) && player != living.getLastHurtByMob())
						ReputationChange.HEAL.applyTo(player, faction, mob.getRandom(), mob);
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
		
		public int volume(RandomSource rand)
		{
			if(max != min)
				return min + rand.nextInt(max - min);
			else
				return min;
		}
		
		public void applyTo(Player player, LivingEntity mob, RandomSource rand)
		{
			applyTo(player, FactionReputation.getFaction(mob), rand, mob);
		}
		
		public void applyTo(Player player, String faction, RandomSource rand, @Nullable LivingEntity mob)
		{
			FactionReputation.changePlayerReputation(player, faction, this, volume(rand), mob);
		}
	}
}
