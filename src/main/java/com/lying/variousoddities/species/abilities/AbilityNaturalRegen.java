package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.api.event.CreatureTypeEvent.GetTypeActionsEvent;
import com.lying.variousoddities.species.types.EnumCreatureType.Action;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityNaturalRegen extends Ability
{
	public AbilityNaturalRegen(){ super(); }
	
	public Type getType(){ return Type.UTILITY; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::addNaturalRegen);
	}
	
	public void addNaturalRegen(GetTypeActionsEvent event)
	{
		if(AbilityRegistry.hasAbilityOfMapName(event.getEntity(), getMapName()))
			event.add(Action.REGENERATE);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityNaturalRegen();
		}
	}
}
