package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.api.event.CreatureTypeEvent.GetTypeActionsEvent;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType.Action;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityNaturalRegen extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "natural_regen");
	
	public AbilityNaturalRegen(){ super(REGISTRY_NAME); }
	
	public Type getType(){ return Type.UTILITY; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::addNaturalRegen);
	}
	
	public void addNaturalRegen(GetTypeActionsEvent event)
	{
		if(AbilityRegistry.hasAbility(event.getEntityLiving(), getMapName()))
			event.add(Action.REGENERATE);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityNaturalRegen();
		}
	}
}
