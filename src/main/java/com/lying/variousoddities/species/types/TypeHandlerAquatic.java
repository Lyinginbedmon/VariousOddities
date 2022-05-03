package com.lying.variousoddities.species.types;

import java.util.UUID;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.AbilityBreatheFluid;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;

public class TypeHandlerAquatic extends TypeHandler
{
	public TypeHandlerAquatic(UUID idIn, boolean breatheAir)
	{
		super(idIn);
		addAbility(AbilityBreatheFluid.water());
		
		if(!breatheAir)
			addAbility(AbilityBreatheFluid.noAir());
	}
	
	public void onLivingTick(LivingEntity living)
	{
		if(living.areEyesInFluid(FluidTags.WATER))
		{
			if(!living.isPotionActive(Effects.CONDUIT_POWER) || living.getActivePotionEffect(Effects.CONDUIT_POWER).getDuration() < Reference.Values.TICKS_PER_SECOND * 7)
			{
				EffectInstance conduitPower = new EffectInstance(Effects.CONDUIT_POWER, 10 * Reference.Values.TICKS_PER_SECOND, 0, true, false);
				living.addPotionEffect(conduitPower);
			}
		}
		else
			onRemove(living);
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
