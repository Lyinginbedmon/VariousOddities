package com.lying.variousoddities.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;

public interface IStackingPotion
{
	/** Returns the result of stacking two instances of this potion together on the given entity */
	public default EffectInstance stackInstances(EffectInstance instanceA, EffectInstance instanceB, LivingEntity living)
	{
		return new EffectInstance(instanceA.getPotion(), instanceA.getDuration(), instanceB.getAmplifier() + Math.max(1, instanceA.getAmplifier()), instanceA.isAmbient(), instanceA.doesShowParticles());
	}
}
