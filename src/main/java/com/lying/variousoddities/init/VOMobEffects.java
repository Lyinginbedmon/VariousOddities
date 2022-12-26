package com.lying.variousoddities.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.Lists;

import com.google.common.base.Predicate;
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

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class VOMobEffects
{
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Reference.ModInfo.MOD_ID);
	public static final Map<MobEffect, Integer> VISUALS = new HashMap<>();
	
	public static final RegistryObject<MobEffect> SLEEP				= EFFECTS.register("sleep", () -> new PotionSleep(3973574));
	public static final RegistryObject<MobEffect> PARALYSIS			= EFFECTS.register("paralysis", () -> new PotionParalysis(-1));
	public static final RegistryObject<MobEffect> DAZZLED			= EFFECTS.register("dazzled", () -> new PotionDazzled(-1));
	public static final RegistryObject<MobEffect> DAZED				= EFFECTS.register("dazed", () -> new PotionDazed(-1));
	public static final RegistryObject<MobEffect> ARCANE_SIGHT		= EFFECTS.register("arcane_sight", () -> new PotionVO(MobEffectCategory.BENEFICIAL, 10289404));
	public static final RegistryObject<MobEffect> DEAFENED			= EFFECTS.register("deafened", () -> new PotionVO(MobEffectCategory.HARMFUL, 7815));
	public static final RegistryObject<MobEffect> SILENCED			= EFFECTS.register("silenced", () -> new PotionVO(MobEffectCategory.HARMFUL, 7815));
	public static final RegistryObject<MobEffect> PETRIFIED			= EFFECTS.register("petrified", () -> new PotionPetrified(9408399));
	public static final RegistryObject<MobEffect> PETRIFYING		= EFFECTS.register("petrifying", () -> new PotionPetrifying(9408399));
	public static final RegistryObject<MobEffect> ENTANGLED			= EFFECTS.register("entangled", () -> new PotionEntangled(9953313));
	public static final RegistryObject<MobEffect> ANCHORED			= EFFECTS.register("anchored", () -> new PotionVO(MobEffectCategory.HARMFUL, 1400709));
	public static final RegistryObject<MobEffect> NEEDLED			= EFFECTS.register("needled", () -> new PotionVO(MobEffectCategory.NEUTRAL, -1));
	public static final RegistryObject<MobEffect> TEMP_HP			= EFFECTS.register("temp_health", () -> new PotionTempHP());
	public static final RegistryObject<MobEffect> HEALTH_DAMAGE		= EFFECTS.register("health_damage", () -> new PotionHealthDamage());
	public static final RegistryObject<MobEffect> HEALTH_DRAIN		= EFFECTS.register("health_drain", () -> new PotionHealthDrain());
	
	public static final List<Predicate<MobEffectInstance>> PARALYSIS_EFFECTS = Lists.newArrayList();
	public static final List<Predicate<MobEffectInstance>> SILENCE_EFFECTS = Lists.newArrayList();
    
	public static void registerVisualPotion(MobEffect potionIn)
	{
		if(potionIn instanceof IVisualPotion)
			VISUALS.put(potionIn, VISUALS.size());
	}
	
	// TODO Reimplement registration of visual potions
//    private static <T extends MobEffect> MobEffect register(String name, MobEffect potionIn)
//    {
//		if(potionIn instanceof IVisualPotion)
//			VISUALS.put(potionIn, VISUALS.size());
//        return EFFECTS.register(name, () -> potionIn).get();
//    }
    
	public static void init() { }
	
	public static int getVisualPotionIndex(MobEffect potionIn)
	{
		if(VISUALS.containsKey(potionIn))
			return VISUALS.get(potionIn);
		return -1;
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
		LivingData data = LivingData.getCapability(entity);
		return data != null && potion instanceof IVisualPotion && data.getVisualPotion(potion);
	}
	
	public static boolean isParalysed(LivingEntity entity)
	{
		return paralysedByPotions(entity) || entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue() == 0D;
	}
	
	/** Returns true if the creature is paralysed by known potion effects */
	public static boolean paralysedByPotions(LivingEntity entity)
	{
		for(MobEffectInstance instance : entity.getActiveEffects())
			if(isParalysisEffect(instance))
				return true;
		return false;
	}
	
	public static boolean isParalysisEffect(MobEffectInstance instance)
	{
		for(Predicate<MobEffectInstance> predicate : PARALYSIS_EFFECTS)
			if(predicate.apply(instance))
				return true;
		return false;
	}
	
	public static boolean isSilenceEffect(MobEffectInstance instance)
	{
		for(Predicate<MobEffectInstance> predicate : SILENCE_EFFECTS)
			if(predicate.apply(instance))
				return true;
		return false;
	}
	
	public static boolean isSilenced(LivingEntity entity)
	{
		for(MobEffectInstance instance : entity.getActiveEffects())
			if(isSilenceEffect(instance))
				return true;
		return false;
	}
	
	static
	{
		PARALYSIS_EFFECTS.add((input) -> input.getEffect() == MobEffects.MOVEMENT_SLOWDOWN && input.getAmplifier() >= 4);
		PARALYSIS_EFFECTS.add((input) -> input.getEffect() == VOMobEffects.PETRIFIED.get());
		PARALYSIS_EFFECTS.add((input) -> input.getEffect() == VOMobEffects.ENTANGLED.get());
		PARALYSIS_EFFECTS.add((input) -> input.getEffect() == VOMobEffects.PARALYSIS.get());
		
		SILENCE_EFFECTS.add((input) -> input.getEffect() == VOMobEffects.SILENCED.get());
		SILENCE_EFFECTS.add((input) -> input.getEffect() == VOMobEffects.PETRIFIED.get());
	}
}
