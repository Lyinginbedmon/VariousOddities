package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.api.event.AbilityEvent.AbilityGetBreathableFluidEvent;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityAmphibious extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "amphibious");
	
	public AbilityAmphibious()
	{
		super(REGISTRY_NAME);
	}
	
	public Type getType() { return Type.UTILITY; }
	
	protected Nature getDefaultNature() { return Nature.EXTRAORDINARY; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::suppressAirSuffocation);
		bus.addListener(EventPriority.LOWEST, this::suppressAirSuffocation2);
	}
	
	/** Prevents suppression of air-breathing by the Aquatic type */
	public void suppressAirSuffocation(AbilityGetBreathableFluidEvent.Add event)
	{
		if(AbilityRegistry.hasAbility(event.getEntity(), REGISTRY_NAME) && !event.getFluids().contains(null))
			event.add(null);
	}
	
	/** Prevents suppression of air-breathing by the Aquatic type */
	public void suppressAirSuffocation2(AbilityGetBreathableFluidEvent.Remove event)
	{
		if(AbilityRegistry.hasAbility(event.getEntity(), REGISTRY_NAME) && event.getFluids().contains(null))
			event.remove(null);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder() { super(REGISTRY_NAME); }
		
		public Ability create(CompoundTag compound){ return new AbilityAmphibious(); }
		
	}
}
