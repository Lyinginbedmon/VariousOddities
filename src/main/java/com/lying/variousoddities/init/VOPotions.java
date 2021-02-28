package com.lying.variousoddities.init;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.potion.PotionSleep;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
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
	
	public static final Effect SLEEP				= addPotion((new PotionSleep(3973574)).setIconIndex(9, 0));
	
	private static Effect addPotion(Effect potionIn){ EFFECTS.add(potionIn); return potionIn; } 
	
	@SubscribeEvent
	public static void registerPotions(RegistryEvent.Register<Effect> event)
	{
//		for(Effect potion : EFFECTS)
//			event.getRegistry().register(potion);
		
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
		return (
//				casterIn.isPotionActive(VOPotions.PETRIFIED) ||
//				casterIn.isPotionActive(VOPotions.ENTANGLED) ||
				(entity.isPotionActive(Effects.SLOWNESS) && entity.getActivePotionEffect(Effects.SLOWNESS).getAmplifier() >= 4) ||
				entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue() == 0D);
	}
	
	public static boolean isSilenced(LivingEntity entity)
	{
//		return (casterIn.isPotionActive(VOPotions.SILENCED) || casterIn.isPotionActive(VOPotions.PETRIFIED));
		return false;
	}
}
