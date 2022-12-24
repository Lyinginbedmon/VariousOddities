package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.api.event.LivingBreathingEvent.LivingMaxAirEvent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityHoldBreath extends Ability
{
	public AbilityHoldBreath(){ super(); }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Ability.Type.UTILITY; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::onMaxAir);
	}
	
	public void onMaxAir(LivingMaxAirEvent event)
	{
		LivingEntity living = event.getEntity();
		if(AbilityRegistry.hasAbilityOfMapName(living, getRegistryName()))
			event.setMaxAir(event.maxAir() * 2);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityHoldBreath();
		}
	}
}
