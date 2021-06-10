package com.lying.variousoddities.species.abilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;

public abstract class AbilityMeleeDamage extends Ability
{
	protected AbilityMeleeDamage(ResourceLocation registryName)
	{
		super(registryName);
	}
	
	/** Returns true if the damage is caused by an entity using its bare hands */
	protected boolean isValidDamageSource(DamageSource source)
	{
		if(source instanceof EntityDamageSource && !((EntityDamageSource)source).getIsThornsDamage())
		{
			Entity trueSource = source.getTrueSource();
			if(source.getImmediateSource() == trueSource && trueSource != null && trueSource instanceof LivingEntity && trueSource.isAlive())
				return ((LivingEntity)trueSource).getHeldItemMainhand().isEmpty();
		}
		return false;
	}
}
