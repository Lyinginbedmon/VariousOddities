package com.lying.variousoddities.species.abilities;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.lying.variousoddities.api.event.AbilityEvent.AbilityGetBreathableFluidEvent;
import com.lying.variousoddities.api.event.GatherAbilitiesEvent;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityBreatheFluid extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "fluid_breathing");
	
	private ResourceLocation fluidTag = null;
	private boolean isRemove = false;
	
	public AbilityBreatheFluid(){ super(REGISTRY_NAME); }
	
	public AbilityBreatheFluid(@Nonnull ITag.INamedTag<Fluid> fluid)
	{
		this();
		
		if(fluid != null)
			this.fluidTag = fluid.getName();
	}
	
	public ResourceLocation getMapName() { return new ResourceLocation(Reference.ModInfo.MOD_ID, (isRemove ? "no_" : "")+(isAirBreathing() ? "air" : this.fluidTag.getPath().toLowerCase())+"_breathing"); }
	
	public ITextComponent translatedName()
	{
		String translation = "ability."+Reference.ModInfo.MOD_ID+".fluid_breathing";
		if(isRemove)
			translation += ".remove";
		
		return new TranslationTextComponent(translation, isAirBreathing() ? "air" : fluidTag.getPath());
	}
	
	public ITextComponent description()
	{
		return new TranslationTextComponent("ability."+getRegistryName()+(isRemove ? ".remove" : "")+".desc", isAirBreathing() ? "air" : fluidTag.getPath().toLowerCase());
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Type.UTILITY; }
	
	public boolean displayInSpecies(){ return !isAirBreathing(); }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		if(fluidTag != null)
			compound.putString("Fluid", this.fluidTag.toString());
		compound.putBoolean("Cannot", this.isRemove);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		if(compound.contains("Fluid", 8))
			this.fluidTag = new ResourceLocation(compound.getString("Fluid"));
		this.isRemove = compound.getBoolean("Cannot");
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::addFluidBreathing);
		bus.addListener(this::removeFluidBreathing);
		bus.addListener(EventPriority.LOWEST, this::gatherAbilities);
	}
	
	public void addFluidBreathing(AbilityGetBreathableFluidEvent.Add event)
	{
		for(AbilityBreatheFluid breathing : AbilityRegistry.getAbilitiesOfType(event.getEntityLiving(), AbilityBreatheFluid.class))
			if(!breathing.isRemove)
				event.add(breathing.getFluid());
	}
	
	public void removeFluidBreathing(AbilityGetBreathableFluidEvent.Remove event)
	{
		for(AbilityBreatheFluid breathing : AbilityRegistry.getAbilitiesOfType(event.getEntityLiving(), AbilityBreatheFluid.class))
			if(breathing.isRemove)
				event.add(breathing.getFluid());
	}
	
	public void gatherAbilities(GatherAbilitiesEvent event)
	{
		List<AbilityBreatheFluid> removals = Lists.newArrayList();
		for(Ability ability : event.getAbilityMap().values())
			if(ability.getRegistryName().equals(REGISTRY_NAME) && ((AbilityBreatheFluid)ability).isRemove)
				removals.add((AbilityBreatheFluid)ability);
		
		for(AbilityBreatheFluid removal : removals)
			event.removeAbility(removal.getTargetFluid());
	}
	
	public boolean isAirBreathing() { return this.fluidTag == null; }
	
	public ITag.INamedTag<Fluid> getFluid()
	{
		for(ITag.INamedTag<Fluid> fluid : FluidTags.getAllTags())
			if(fluid.getName().equals(fluidTag))
				return fluid;
		return null;
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
	
	public static boolean breathes(LivingEntity entity) { return AbilityRegistry.hasAbility(entity, AbilityBreatheFluid.class); }
	public static boolean canBreatheIn(LivingEntity entity, ITag.INamedTag<Fluid> fluid) { return AbilityRegistry.hasAbility(entity, new ResourceLocation(Reference.ModInfo.MOD_ID, fluid.getName().getPath().toLowerCase()+"_breathing")); }
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			AbilityBreatheFluid breathing = new AbilityBreatheFluid();
			breathing.readFromNBT(compound);
			return breathing;
		}
	}
}
