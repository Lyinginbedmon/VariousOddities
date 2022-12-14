package com.lying.variousoddities.species.abilities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public abstract class AbilityPhasing extends Ability implements IPhasingAbility
{
	protected AbilityPhasing()
	{
		super();
	}
	
	public Type getType(){ return Type.UTILITY; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::ignoreNonMagicDamage);
	}
	
	public void ignoreNonMagicDamage(LivingHurtEvent event)
	{
		LivingEntity living = event.getEntity();
		if(!DamageType.getDamageTypes(event.getSource()).contains(DamageType.MAGIC))
			AbilityRegistry.getAbilitiesOfClass(living, AbilityPhasing.class).forEach((ability) -> { if(ability.ignoresNonMagicDamage()) event.setCanceled(true); });
	}
	
	public abstract boolean ignoresNonMagicDamage();
}
