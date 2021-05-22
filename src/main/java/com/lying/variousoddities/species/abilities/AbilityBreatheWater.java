package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.api.event.CreatureTypeEvent.GetTypeActionsEvent;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType.Action;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityBreatheWater extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "water_breathing");
	
	public AbilityBreatheWater(){ super(REGISTRY_NAME); }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Type.UTILITY; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::addWaterBreathing);
	}
	
	public void addWaterBreathing(GetTypeActionsEvent event)
	{
		if(AbilityRegistry.hasAbility(event.getEntityLiving(), getMapName()))
			if(event.getActions().breathes())
				event.add(Action.BREATHE_WATER);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityBreatheWater();
		}
	}
}
