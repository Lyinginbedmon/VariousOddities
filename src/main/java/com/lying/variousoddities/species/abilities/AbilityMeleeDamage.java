package com.lying.variousoddities.species.abilities;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public abstract class AbilityMeleeDamage extends Ability
{
	protected AbilityMeleeDamage()
	{
		super();
	}
	
	/** Returns true if the damage is caused by an entity using its bare hands */
	protected boolean isValidDamageSource(DamageSource source)
	{
		if(source instanceof EntityDamageSource && !((EntityDamageSource)source).isThorns())
		{
			Entity trueSource = source.getEntity();
			if(source.getDirectEntity() == trueSource && trueSource != null && trueSource instanceof LivingEntity && trueSource.isAlive())
				return ((LivingEntity)trueSource).getMainHandItem().isEmpty();
		}
		return false;
	}
}
