package com.lying.variousoddities.types;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;

public class TypeHandlerAquatic extends TypeHandler
{
	public void onMobUpdateEvent(LivingEntity living)
	{
		if(living.areEyesInFluid(FluidTags.WATER))
		{
			EffectInstance conduitPower = new EffectInstance(Effects.CONDUIT_POWER, 10 * Reference.Values.TICKS_PER_SECOND, 0, true, false);
			living.addPotionEffect(conduitPower);
		}
		else if(living.isPotionActive(Effects.CONDUIT_POWER))
		{
			EffectInstance instance = living.getActivePotionEffect(Effects.CONDUIT_POWER);
			if(instance.isAmbient() && !instance.doesShowParticles())
				living.removeActivePotionEffect(Effects.CONDUIT_POWER);
		}
	}
	
	public void onRemove(LivingEntity living)
	{
		if(living.isPotionActive(Effects.CONDUIT_POWER))
		{
			EffectInstance instance = living.getActivePotionEffect(Effects.CONDUIT_POWER);
			if(instance.isAmbient() && !instance.doesShowParticles())
				living.removeActivePotionEffect(Effects.CONDUIT_POWER);
		}
	}
}
