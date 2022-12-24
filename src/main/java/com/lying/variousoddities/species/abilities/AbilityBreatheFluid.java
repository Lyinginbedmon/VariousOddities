package com.lying.variousoddities.species.abilities;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.lying.variousoddities.api.event.AbilityEvent.AbilityGetBreathableFluidEvent;
import com.lying.variousoddities.api.event.GatherAbilitiesEvent;
import com.lying.variousoddities.api.event.LivingBreathingEvent.LivingCanBreatheFluidEvent;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityBreatheFluid extends Ability
{
	private ResourceLocation fluidTag = null;
	private boolean isRemove = false;
	
	public AbilityBreatheFluid(){ super(); }
	
	public AbilityBreatheFluid(@Nonnull TagKey<Fluid> fluid)
	{
		this();
		
		if(fluid != null)
			this.fluidTag = fluid.location();
	}
	
	public ResourceLocation getMapName() { return new ResourceLocation(Reference.ModInfo.MOD_ID, (isRemove ? "no_" : "")+(isAirBreathing() ? "air" : this.fluidTag.getPath().toLowerCase())+"_breathing"); }
	
	public Component translatedName()
	{
		String translation = "ability."+Reference.ModInfo.MOD_ID+".fluid_breathing";
		if(isRemove)
			translation += ".remove";
		
		return Component.translatable(translation, isAirBreathing() ? "air" : fluidTag.getPath());
	}
	
	public Component description()
	{
		return Component.translatable("ability."+getRegistryName()+(isRemove ? ".remove" : "")+".desc", isAirBreathing() ? "air" : fluidTag.getPath().toLowerCase());
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Type.UTILITY; }
	
	public boolean displayInSpecies(){ return !isAirBreathing(); }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		if(fluidTag != null)
			compound.putString("Fluid", this.fluidTag.toString());
		compound.putBoolean("Cannot", this.isRemove);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		if(compound.contains("Fluid", 8))
			this.fluidTag = new ResourceLocation(compound.getString("Fluid"));
		this.isRemove = compound.getBoolean("Cannot");
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::canBreathe);
		bus.addListener(this::addFluidBreathing);
		bus.addListener(this::removeFluidBreathing);
		bus.addListener(EventPriority.LOWEST, this::gatherAbilities);
	}
	
	public void addFluidBreathing(AbilityGetBreathableFluidEvent.Add event)
	{
		for(AbilityBreatheFluid breathing : AbilityRegistry.getAbilitiesOfClass(event.getEntity(), AbilityBreatheFluid.class))
			if(!breathing.isRemove)
				event.add(breathing.getFluid());
	}
	
	public void removeFluidBreathing(AbilityGetBreathableFluidEvent.Remove event)
	{
		for(AbilityBreatheFluid breathing : AbilityRegistry.getAbilitiesOfClass(event.getEntity(), AbilityBreatheFluid.class))
			if(breathing.isRemove)
				event.add(breathing.getFluid());
	}
	
	public void canBreathe(LivingCanBreatheFluidEvent event)
	{
		for(AbilityBreatheFluid breathing : AbilityRegistry.getAbilitiesOfClass(event.getEntity(), AbilityBreatheFluid.class))
			if(event.state().is(breathing.getFluid()) || event.state().isEmpty() && breathing.isAirBreathing())
				if(breathing.isRemove)
				{
					event.setResult(Result.DENY);
					return;
				}
				else
					event.setResult(Result.ALLOW);
	}
	
	public void gatherAbilities(GatherAbilitiesEvent event)
	{
		List<AbilityBreatheFluid> removals = Lists.newArrayList();
		for(Ability ability : event.getAbilityMap().values())
			if(ability.getRegistryName().equals(getRegistryName()) && ((AbilityBreatheFluid)ability).isRemove)
				removals.add((AbilityBreatheFluid)ability);
		
		for(AbilityBreatheFluid removal : removals)
			event.removeAbility(removal.getTargetFluid());
	}
	
	public boolean isAirBreathing() { return this.fluidTag == null; }
	
	public TagKey<Fluid> getFluid()
	{
		return TagKey.create(Registry.FLUID_REGISTRY, fluidTag);
	}
	
	public ResourceLocation getTargetFluid()
	{
		return new ResourceLocation(Reference.ModInfo.MOD_ID, (isAirBreathing() ? "air" : this.fluidTag.getPath().toLowerCase())+"_breathing");
	}
	
	public static AbilityBreatheFluid air() { return new AbilityBreatheFluid(null); }
	public static AbilityBreatheFluid noAir()
	{
		AbilityBreatheFluid ability = air();
		ability.isRemove = true;
		return ability;
	}
	public static AbilityBreatheFluid lava() { return new AbilityBreatheFluid(FluidTags.LAVA); }
	public static AbilityBreatheFluid water() { return new AbilityBreatheFluid(FluidTags.WATER); }
	
	public static boolean breathes(LivingEntity entity) { return AbilityRegistry.hasAbilityOfClass(entity, AbilityBreatheFluid.class); }
	public static boolean canBreatheIn(LivingEntity entity, TagKey<Fluid> fluid) { return AbilityRegistry.hasAbilityOfMapName(entity, new ResourceLocation(Reference.ModInfo.MOD_ID, fluid.location().getPath().toLowerCase()+"_breathing")); }
	
	/**
	 * Returns a list of all fluid tags from all instances of AbilityBreatheFluid on the given entity
	 * @param entity
	 * @return
	 */
	public static List<TagKey<Fluid>> getBreathableFluids(LivingEntity entity)
	{
		AbilityGetBreathableFluidEvent.Add event1 = new AbilityGetBreathableFluidEvent.Add(entity);
		MinecraftForge.EVENT_BUS.post(event1);
		
		List<TagKey<Fluid>> breathables = event1.getFluids();
		if(!breathables.isEmpty())
		{
			AbilityGetBreathableFluidEvent.Remove event2 = new AbilityGetBreathableFluidEvent.Remove(entity);
			MinecraftForge.EVENT_BUS.post(event2);
			
			event2.getFluids().forEach((fluid) -> { breathables.remove(fluid); });
		}
		
		return breathables;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			AbilityBreatheFluid breathing = new AbilityBreatheFluid();
			breathing.readFromNBT(compound);
			return breathing;
		}
	}
}
