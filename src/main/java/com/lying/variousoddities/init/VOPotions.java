package com.lying.variousoddities.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.lying.variousoddities.potion.PotionParalysis;
import com.lying.variousoddities.potion.PotionSleep;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class VOPotions
{
	private static final List<Effect> EFFECTS = new ArrayList<Effect>();
	
	public static boolean isRegistered = false;
	
	// TODO Potions need icon textures
	
	public static final Effect SLEEP				= addPotion((new PotionSleep(3973574)).setIconIndex(9, 0));
	public static final Effect PARALYSIS			= addPotion((new PotionParalysis(-1)).setIconIndex(10, 0));
	
	public static final Map<Effect, Predicate<EffectInstance>> PARALYSIS_EFFECTS = new HashMap<>();
	public static final Map<Effect, Predicate<EffectInstance>> SILENCE_EFFECTS = new HashMap<>();
	
	private static Effect addPotion(Effect potionIn){ EFFECTS.add(potionIn); return potionIn; } 
	
	@SubscribeEvent
	public static void registerPotions(RegistryEvent.Register<Effect> event)
	{
		for(Effect potion : EFFECTS)
			event.getRegistry().register(potion);
		
		isRegistered = true;
	}
	
	/**
	 * Returns true if the entity has the given potion effect with a duration greater than 0 ticks
	 * @param entity
	 * @param potion
	 */
	public static boolean isPotionActive(LivingEntity entity, Effect potion)
	{
		return entity.isPotionActive(potion) || entity.getActivePotionEffect(potion) != null && entity.getActivePotionEffect(potion).getDuration() > 0;
	}
	
	public static boolean isParalysed(LivingEntity entity)
	{
		return paralysedByPotions(entity) || entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue() == 0D;
	}
	
	/** Returns true if the creature is paralysed by known potion effects */
	public static boolean paralysedByPotions(LivingEntity entity)
	{
		for(Effect effect : PARALYSIS_EFFECTS.keySet())
			if(entity.isPotionActive(effect) && PARALYSIS_EFFECTS.get(effect).apply(entity.getActivePotionEffect(effect)))
				return true;
		return false;
	}
	
	public static boolean isParalysisEffect(EffectInstance instance)
	{
		for(Effect effect : PARALYSIS_EFFECTS.keySet())
			if(effect == instance.getPotion() && PARALYSIS_EFFECTS.get(effect).apply(instance))
				return true;
		return false;
	}
	
	public static boolean isSilenced(LivingEntity entity)
	{
		for(Effect effect : SILENCE_EFFECTS.keySet())
			if(entity.isPotionActive(effect) && SILENCE_EFFECTS.get(effect).apply(entity.getActivePotionEffect(effect)))
				return true;
		return false;
	}
	
	static
	{
		PARALYSIS_EFFECTS.put(Effects.SLOWNESS, new Predicate<EffectInstance>()
		{
			public boolean apply(EffectInstance input)
			{
				return input.getAmplifier() >= 4;
			}
		});
//		PARALYSIS_EFFECTS.put(VOPotions.PETRIFIED, Predicates.alwaysTrue());
//		PARALYSIS_EFFECTS.put(VOPotions.ENTANGLED, Predicates.alwaysTrue());
		PARALYSIS_EFFECTS.put(VOPotions.PARALYSIS, Predicates.alwaysTrue());
		
//		SILENCE_EFFECTS.put(VOPotions.SILENCED, Predicates.alwaysTrue());
//		SILENCE_EFFECTS.put(VOPotions.PETRIFIED, Predicates.alwaysTrue());
	}
}
