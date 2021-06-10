package com.lying.variousoddities.species.abilities;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public abstract class AbilityPhasing extends Ability implements IPhasingAbility
{
	protected AbilityPhasing(ResourceLocation registryNameIn)
	{
		super(registryNameIn);
	}
	
	public Type getType(){ return Type.UTILITY; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::ignoreNonMagicDamage);
	}
	
	public void ignoreNonMagicDamage(LivingHurtEvent event)
	{
		LivingEntity living = event.getEntityLiving();
		if(!DamageType.getDamageTypes(event.getSource()).contains(DamageType.MAGIC))
			AbilityRegistry.getAbilitiesOfType(living, AbilityPhasing.class).forEach((ability) -> { if(ability.ignoresNonMagicDamage()) event.setCanceled(true); });
	}
	
	public abstract boolean ignoresNonMagicDamage();
}
