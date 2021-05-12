package com.lying.variousoddities.species.types;

import java.util.List;
import java.util.Map;

import com.lying.variousoddities.api.event.CreatureTypeEvent.TypeApplyEvent;
import com.lying.variousoddities.api.event.CreatureTypeEvent.TypeRemoveEvent;
import com.lying.variousoddities.api.event.DamageResistanceEvent;
import com.lying.variousoddities.api.event.SpellEvent.SpellAffectEntityEvent;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.species.types.EnumCreatureType.ActionSet;
import com.lying.variousoddities.species.types.TypeHandler.DamageResist;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity.SleepResult;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TypeBus
{
	public static boolean shouldFire(){ return ConfigVO.MOBS.typeSettings.typesMatter(); }
	
	@SubscribeEvent
	public static void onPlayerLogInEvent(EntityJoinWorldEvent event)
	{
		if(event.getEntity().getType() == EntityType.PLAYER)
		{
			World world = event.getWorld();
			if(world != null && !world.isRemote)
				TypesManager.get(world).notifyPlayer((PlayerEntity)event.getEntity());
		}
	}
	
	@SubscribeEvent
	public static void onTypeApplyEvent(TypeApplyEvent event)
	{
		event.getType().getHandler().onApply(event.getEntityLiving());
	}
	
	@SubscribeEvent
	public static void onTypeRemoveEvent(TypeRemoveEvent event)
	{
		event.getType().getHandler().onRemove(event.getEntityLiving());
	}
	
	/** Prevents creatures that do not sleep from sleeping in a bed */
	@SubscribeEvent
	public static void onSleepEvent(PlayerSleepInBedEvent event)
	{
		if(!shouldFire()) return;
		if(event.getPlayer() != null && event.getPos() != null)
		{
			PlayerEntity player = event.getPlayer();
			if(!ActionSet.fromTypes(player, EnumCreatureType.getCreatureTypes(player)).sleeps())
			{
				event.setResult(SleepResult.NOT_POSSIBLE_NOW);
				if(!player.getEntityWorld().isRemote)
				{
					ServerPlayerEntity playerServer = (ServerPlayerEntity)player;
					playerServer.func_242111_a(playerServer.getServerWorld().getDimensionKey(), event.getPos(), 0.0F, true, true);
				}
				return;
			}
		}
	}
	
	/** Applies immunity and resistance to critical hits, usually by types lacking discernable vulnerabilities */
	@SubscribeEvent
	public static void onCriticalHitEvent(CriticalHitEvent event)
	{
		if(!shouldFire()) return;
		if(event.getTarget() != null && event.getTarget() instanceof LivingEntity)
		{
			for(EnumCreatureType mobType : EnumCreatureType.getCreatureTypes(event.getEntityLiving()))
			{
				if(!mobType.getHandler().canCriticalHit())
				{
					event.setDamageModifier(1.0F);
					event.setResult(Result.DENY);
				}
				mobType.getHandler().onCriticalEvent(event);
				if(event.getResult() == Result.DENY)
					return;
			}
		}
	}
	
	/** Applies immunity to certain types of damage, to prevent damage that might be applied */
	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event)
	{
		if(!shouldFire()) return;
		if(event.getEntityLiving() != null && event.getEntityLiving().isAlive())
		{
			List<EnumCreatureType> types = EnumCreatureType.getCreatureTypes(event.getEntityLiving());
			ActionSet actions = ActionSet.fromTypes(event.getEntityLiving(), types);
			DamageSource source = event.getSource();
			
			// Creatures that don't need to breathe cannot drown or suffocate
			if((source == DamageSource.DROWN || source == DamageSource.IN_WALL) && !actions.breathes())
			{
				event.setCanceled(true);
				return;
			}
			// Creatures that don't need to eat cannot starve to death
			else if(source == DamageSource.STARVE && !actions.eats())
			{
				event.setCanceled(true);
				return;
			}
			else if(event.getAmount() > 0F)
			{
				DamageResistanceEvent resistanceEvent = new DamageResistanceEvent(source, event.getEntityLiving());
				MinecraftForge.EVENT_BUS.post(resistanceEvent);
				DamageResist resistance = resistanceEvent.getResistance();
				if(resistance == DamageResist.IMMUNE)
					event.setCanceled(true);
			}
		}
	}
	
	/** Modifies damage received according to configured creature types */
	@SubscribeEvent
	public static void onLivingHurtEvent(LivingHurtEvent event)
	{
		if(!shouldFire()) return;
		if(event.getEntityLiving() != null && event.getEntityLiving().isAlive())
		{
			List<EnumCreatureType> types = EnumCreatureType.getCreatureTypes(event.getEntityLiving());
			ActionSet actions = ActionSet.fromTypes(event.getEntityLiving(), types);
			
			DamageSource source = event.getSource();
			// Creatures that don't need to breathe cannot drown or suffocate
			if(source == DamageSource.DROWN || source == DamageSource.IN_WALL)
			{
				if(!actions.breathes())
				{
					event.setCanceled(true);
					return;
				}
			}
			// Creatures that don't need to eat cannot starve to death
			else if(source == DamageSource.STARVE)
			{
				if(!actions.eats())
				{
					event.setCanceled(true);
					return;
				}
			}
			else
			{
				DamageResistanceEvent resistanceEvent = new DamageResistanceEvent(source, event.getEntityLiving());
				MinecraftForge.EVENT_BUS.post(resistanceEvent);
				DamageResist resistance = resistanceEvent.getResistance();
				switch(resistance)
				{
					case IMMUNE:		event.setCanceled(true); break;
					case VULNERABLE:	event.setAmount(event.getAmount() * 1.5F); break;
					case RESISTANT:		event.setAmount(event.getAmount() * 0.5F); break;
					case NORMAL:		break;
				}
			}
		}
	}
	
	/** Modifies damage received according to configured creature types */
	@SubscribeEvent
	public static void onLivingDamageEvent(LivingDamageEvent event)
	{
		if(!shouldFire()) return;
		
		if(!EnumCreatureType.getTypes(event.getEntityLiving()).isUndead()) return;
		
		if(event.getSource().getTrueSource() != null && event.getSource().getTrueSource() instanceof LivingEntity)
		{
			LivingEntity livingSource = (LivingEntity)event.getSource().getTrueSource();
			if(!livingSource.getHeldItemMainhand().isEmpty() && livingSource.getHeldItemMainhand().isEnchanted())
			{
				CreatureAttribute actualType = event.getEntityLiving().getCreatureAttribute();
				List<CreatureAttribute> configTypes = EnumCreatureType.getTypes(event.getEntityLiving()).getAttributes();
				
				Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(livingSource.getHeldItemMainhand());
				
				float configMod = 0.0F;
				float appliedMod = 0.0F;
				for(Enchantment enchant : enchantments.keySet())
				{
					int level = enchantments.get(enchant);
					// Calculate the modifier that has already been applied when this event is called
					appliedMod += enchant.calcDamageByCreature(level, actualType);
					
					// Only apply the highest modifier, to avoid applying multiple applicable modifiers at once (with Sharpness, for instance)
					float maxMod = 0.0F;
					for(CreatureAttribute attribute : configTypes)
					{
						float mod = enchant.calcDamageByCreature(level, attribute);
						if(mod > maxMod) maxMod = mod;
					}
					
					// Calculate the modifier that should be applied based on the mob's types
					configMod += maxMod;
				}
				
				// Subtract the applied modifier and add the configured modifier
				event.setAmount(event.getAmount() - appliedMod + configMod);
			}
		}
	}
	
	/**
	 * Prevents magic effects from affecting mobs and players immune to them, such as death effects
	 * @param event
	 */
	@SubscribeEvent
	public static void onSpellAffectEntityEvent(SpellAffectEntityEvent event)
	{
		if(!shouldFire()) return;
		if(event.getTarget() != null && event.getTarget() instanceof LivingEntity)
			for(EnumCreatureType mobType : EnumCreatureType.getCreatureTypes((LivingEntity)event.getTarget()))
				if(!mobType.getHandler().canSpellAffect(event.getSpellData().getSpell()))
				{
					event.setCanceled(true);
					return;
				}
	}
	
	@SubscribeEvent
	public static void onBreakSpeedEvent(BreakSpeed event)
	{
		if(!shouldFire()) return;
		PlayerEntity player = event.getPlayer();
		if(player.getEntityWorld().isRemote) return;
		
		if(TypesManager.isMobOfType(player, EnumCreatureType.AQUATIC) || TypesManager.isMobOfType(player, EnumCreatureType.WATER))
			if(player.areEyesInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player))
				event.setNewSpeed(event.getNewSpeed() * 5F);
		
		if(TypesManager.isMobOfType(player, EnumCreatureType.EARTH) && (event.getState().getMaterial() == Material.ROCK || event.getState().getMaterial() == Material.EARTH))
			event.setNewSpeed(event.getNewSpeed() * 1.3F);
	}
}
