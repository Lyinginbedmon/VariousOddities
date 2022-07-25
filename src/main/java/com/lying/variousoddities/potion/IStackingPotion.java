package com.lying.variousoddities.potion;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public interface IStackingPotion
{
	/** Returns the result of stacking two instances of this potion together on the given entity */
	public default MobEffectInstance stackInstances(MobEffectInstance instanceA, MobEffectInstance instanceB, LivingEntity living)
	{
		return new MobEffectInstance(instanceA.getEffect(), instanceA.getDuration(), instanceB.getAmplifier() + Math.max(1, instanceA.getAmplifier()), instanceA.isAmbient(), instanceA.isVisible());
	}
}
