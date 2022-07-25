package com.lying.variousoddities.species.types;

import java.util.List;
import java.util.Map;

import com.lying.variousoddities.api.event.CreatureTypeEvent.TypeApplyEvent;
import com.lying.variousoddities.api.event.CreatureTypeEvent.TypeRemoveEvent;
import com.lying.variousoddities.api.event.DamageResistanceEvent;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSyncTypesCustom;
import com.lying.variousoddities.species.abilities.AbilityImmunityCrits;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.types.EnumCreatureType.ActionSet;
import com.lying.variousoddities.species.types.TypeHandler.DamageResist;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
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
	public static void onPlayerLogInEvent(EntityJoinLevelEvent event)
	{
		if(event.getEntity().getType() == EntityType.PLAYER)
		{
			Level world = event.getLevel();
			if(world != null && !world.isClientSide)
			{
				ServerPlayer player = (ServerPlayer)event.getEntity();
				TypesManager manager = TypesManager.get(world);
				if(manager != null)
					manager.notifyPlayer(player);
				
				PacketHandler.sendToNearby(world, player, new PacketSyncTypesCustom(player, LivingData.forEntity(player).getCustomTypes()));
			}
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
		if(event.getEntity() != null && event.getPos() != null)
		{
			Player player = event.getEntity();
			if(!ActionSet.fromTypes(player, EnumCreatureType.getCreatureTypes(player)).sleeps())
			{
				event.setResult(BedSleepingProblem.NOT_POSSIBLE_NOW);
				if(!player.getLevel().isClientSide)
				{
					ServerPlayer playerServer = (ServerPlayer)player;
					playerServer.setRespawnPosition(playerServer.getLevel().dimension(), event.getPos(), 0.0F, true, true);
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
			if(AbilityRegistry.hasAbility(event.getEntity(), AbilityImmunityCrits.REGISTRY_NAME))
			{
				event.setDamageModifier(1.0F);
				event.setResult(Result.DENY);
			}
		}
	}
	
	/** Applies immunity to certain types of damage, to prevent damage that might be applied */
	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event)
	{
		if(!shouldFire()) return;
		if(event.getEntity() != null && event.getEntity().isAlive())
		{
			List<EnumCreatureType> types = EnumCreatureType.getCreatureTypes(event.getEntity());
			ActionSet actions = ActionSet.fromTypes(event.getEntity(), types);
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
				DamageResistanceEvent resistanceEvent = new DamageResistanceEvent(source, event.getEntity());
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
		if(event.getEntity() != null && event.getEntity().isAlive())
		{
			List<EnumCreatureType> types = EnumCreatureType.getCreatureTypes(event.getEntity());
			ActionSet actions = ActionSet.fromTypes(event.getEntity(), types);
			
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
				DamageResistanceEvent resistanceEvent = new DamageResistanceEvent(source, event.getEntity());
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
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void onLivingDamageEvent(LivingDamageEvent event)
	{
		if(!shouldFire()) return;
		
		if(!EnumCreatureType.getTypes(event.getEntity()).isUndead()) return;
		
		if(event.getSource().getEntity() != null && event.getSource().getEntity() instanceof LivingEntity)
		{
			LivingEntity livingSource = (LivingEntity)event.getSource().getEntity();
			if(!livingSource.getMainHandItem().isEmpty() && livingSource.getMainHandItem().isEnchanted())
			{
				MobType actualType = event.getEntity().getMobType();
				List<MobType> configTypes = EnumCreatureType.getTypes(event.getEntity()).getAttributes();
				
				Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(livingSource.getMainHandItem());
				
				float configMod = 0.0F;
				float appliedMod = 0.0F;
				for(Enchantment enchant : enchantments.keySet())
				{
					int level = enchantments.get(enchant);
					// Calculate the modifier that has already been applied when this event is called
					appliedMod += enchant.getDamageBonus(level, actualType);
					
					// Only apply the highest modifier, to avoid applying multiple applicable modifiers at once (with Sharpness, for instance)
					float maxMod = 0.0F;
					for(MobType attribute : configTypes)
					{
						float mod = enchant.getDamageBonus(level, attribute);
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
	
	@SubscribeEvent
	public static void onBreakSpeedEvent(BreakSpeed event)
	{
		if(!shouldFire()) return;
		Player player = event.getEntity();
		if(player.getLevel().isClientSide) return;
		
		if(TypesManager.isMobOfType(player, EnumCreatureType.AQUATIC) || TypesManager.isMobOfType(player, EnumCreatureType.WATER))
			if(player.getEyeInFluidType() == Fluids.WATER.getFluidType() && !EnchantmentHelper.hasAquaAffinity(player))
				event.setNewSpeed(event.getNewSpeed() * 5F);
		
//		if(TypesManager.isMobOfType(player, EnumCreatureType.EARTH) && (event.getState().getMaterial() == Material.ROCK || event.getState().getMaterial() == Material.EARTH))
//			event.setNewSpeed(event.getNewSpeed() * 1.3F);
	}
}
