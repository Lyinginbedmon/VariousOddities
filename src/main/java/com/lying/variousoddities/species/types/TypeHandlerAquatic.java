package com.lying.variousoddities.species.types;

import java.util.UUID;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.AbilityBreatheFluid;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluids;

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
		if(living.isEyeInFluidType(Fluids.WATER.getFluidType()))
		{
			if(!living.hasEffect(MobEffects.CONDUIT_POWER) || living.getEffect(MobEffects.CONDUIT_POWER).getDuration() < Reference.Values.TICKS_PER_SECOND * 7)
			{
				MobEffectInstance conduitPower = new MobEffectInstance(MobEffects.CONDUIT_POWER, 10 * Reference.Values.TICKS_PER_SECOND, 0, true, false);
				living.addEffect(conduitPower);
			}
		}
		else
			onRemove(living);
	}
	
	public void onRemove(LivingEntity living)
	{
		if(living.hasEffect(MobEffects.CONDUIT_POWER))
		{
			MobEffectInstance instance = living.getEffect(MobEffects.CONDUIT_POWER);
			if(instance.isAmbient() && !instance.isVisible())
				living.removeEffect(MobEffects.CONDUIT_POWER);
		}
	}
}
