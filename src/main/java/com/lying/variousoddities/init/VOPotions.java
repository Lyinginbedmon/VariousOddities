package com.lying.variousoddities.init;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.potion.IVisualPotion;
import com.lying.variousoddities.potion.PotionDazed;
import com.lying.variousoddities.potion.PotionDazzled;
import com.lying.variousoddities.potion.PotionEntangled;
import com.lying.variousoddities.potion.PotionHealthDamage;
import com.lying.variousoddities.potion.PotionHealthDrain;
import com.lying.variousoddities.potion.PotionParalysis;
import com.lying.variousoddities.potion.PotionPetrified;
import com.lying.variousoddities.potion.PotionPetrifying;
import com.lying.variousoddities.potion.PotionSleep;
import com.lying.variousoddities.potion.PotionTempHP;
import com.lying.variousoddities.potion.PotionVO;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;

public class VOPotions
{
	private static final Map<ResourceLocation, MobEffect> EFFECTS = new HashMap<>();
	public static final Map<MobEffect, Integer> VISUALS = new HashMap<>();
	
	public static boolean isRegistered = false;
	
	public static final MobEffect SLEEP				= addPotion("sleep", new PotionSleep(3973574));
	public static final MobEffect PARALYSIS			= addPotion("paralysis", new PotionParalysis(-1));
	public static final MobEffect DAZZLED			= addPotion("dazzled", new PotionDazzled(-1));
	public static final MobEffect DAZED				= addPotion("dazed", new PotionDazed(-1));
	public static final MobEffect ARCANE_SIGHT		= addPotion("arcane_sight", new PotionVO(MobEffectCategory.BENEFICIAL, 10289404));
	public static final MobEffect DEAFENED			= addPotion("deafened", new PotionVO(MobEffectCategory.HARMFUL, 7815));
	public static final MobEffect SILENCED			= addPotion("silenced", new PotionVO(MobEffectCategory.HARMFUL, 7815));
	public static final MobEffect PETRIFIED			= addPotion("petrified", new PotionPetrified(9408399));
	public static final MobEffect PETRIFYING		= addPotion("petrifying", new PotionPetrifying(9408399));
	public static final MobEffect ENTANGLED			= addPotion("entangled", new PotionEntangled(9953313));
	public static final MobEffect ANCHORED			= addPotion("anchored", new PotionVO(MobEffectCategory.HARMFUL, 1400709));
	public static final MobEffect NEEDLED			= addPotion("needled", new PotionVO(MobEffectCategory.NEUTRAL, -1));
	public static final MobEffect TEMP_HP			= addPotion("temp_health", new PotionTempHP());
	public static final MobEffect HEALTH_DAMAGE		= addPotion("health_damage", new PotionHealthDamage());
	public static final MobEffect HEALTH_DRAIN		= addPotion("health_drain", new PotionHealthDrain());
	
	public static final Map<MobEffect, Predicate<MobEffectInstance>> PARALYSIS_EFFECTS = new HashMap<>();
	public static final Map<MobEffect, Predicate<MobEffectInstance>> SILENCE_EFFECTS = new HashMap<>();
	
	private static MobEffect addPotion(String nameIn, MobEffect potionIn) { return addPotion(new ResourceLocation(Reference.ModInfo.MOD_ID, nameIn), potionIn); }
	
	private static MobEffect addPotion(ResourceLocation registryName, MobEffect potionIn)
	{
		EFFECTS.put(registryName, potionIn);
		if(potionIn instanceof IVisualPotion)
			VISUALS.put(potionIn, VISUALS.size());
		return potionIn;
	}
	
	public static int getVisualPotionIndex(MobEffect potionIn)
	{
		if(VISUALS.containsKey(potionIn))
			return VISUALS.get(potionIn);
		return -1;
	}
	
	public static void init()
	{
		for(ResourceLocation potion : EFFECTS.keySet())
			ForgeRegistries.MOB_EFFECTS.register(potion, EFFECTS.get(potion));
		
		isRegistered = true;
	}
	
	/**
	 * Returns true if the entity has the given potion effect with a duration greater than 0 ticks
	 * @param entity
	 * @param potion
	 */
	public static boolean isPotionActive(LivingEntity entity, MobEffect potion)
	{
		return entity.hasEffect(potion) || entity.getEffect(potion) != null && entity.getEffect(potion).getDuration() > 0;
	}
	
	public static boolean isPotionVisible(LivingEntity entity, MobEffect potion)
	{
		LivingData data = LivingData.forEntity(entity);
		return data != null && potion instanceof IVisualPotion && data.getVisualPotion(potion);
	}
	
	public static boolean isParalysed(LivingEntity entity)
	{
		return paralysedByPotions(entity) || entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue() == 0D;
	}
	
	/** Returns true if the creature is paralysed by known potion effects */
	public static boolean paralysedByPotions(LivingEntity entity)
	{
		for(MobEffect effect : PARALYSIS_EFFECTS.keySet())
			if(entity.hasEffect(effect) && PARALYSIS_EFFECTS.get(effect).apply(entity.getEffect(effect)))
				return true;
		return false;
	}
	
	public static boolean isParalysisEffect(MobEffectInstance instance)
	{
		for(MobEffect effect : PARALYSIS_EFFECTS.keySet())
			if(effect == instance.getEffect() && PARALYSIS_EFFECTS.get(effect).apply(instance))
				return true;
		return false;
	}
	
	public static boolean isSilenced(LivingEntity entity)
	{
		for(MobEffect effect : SILENCE_EFFECTS.keySet())
			if(entity.hasEffect(effect) && SILENCE_EFFECTS.get(effect).apply(entity.getEffect(effect)))
				return true;
		return false;
	}
	
	static
	{
		PARALYSIS_EFFECTS.put(MobEffects.MOVEMENT_SLOWDOWN, new Predicate<MobEffectInstance>()
		{
			public boolean apply(MobEffectInstance input)
			{
				return input.getAmplifier() >= 4;
			}
		});
//		PARALYSIS_EFFECTS.put(VOPotions.PETRIFIED, Predicates.alwaysTrue());
//		PARALYSIS_EFFECTS.put(VOPotions.ENTANGLED, Predicates.alwaysTrue());
		PARALYSIS_EFFECTS.put(VOPotions.PARALYSIS, Predicates.alwaysTrue());
		
		SILENCE_EFFECTS.put(VOPotions.SILENCED, Predicates.alwaysTrue());
		SILENCE_EFFECTS.put(VOPotions.PETRIFIED, Predicates.alwaysTrue());
	}
}
