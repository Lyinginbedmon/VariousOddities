package com.lying.variousoddities.types.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AbilityBlindsight extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "blindsight");
	
	private double range;
	private double rangeMin = 0D;
	
	public AbilityBlindsight(double rangeIn)
	{
		this.range = Math.max(4D, rangeIn);
	}
	
	public AbilityBlindsight(double rangeIn, double rangeMinIn)
	{
		this(rangeIn);
		this.rangeMin = rangeMinIn;
	}
	
	public ResourceLocation getRegistryName(){ return REGISTRY_NAME; }
	
	public ITextComponent translatedName(){ return new TranslationTextComponent("ability."+Reference.ModInfo.MOD_ID+".blindsight", (int)range); }
	
	public Type getType(){ return Ability.Type.UTILITY; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.putDouble("Max", this.range);
		compound.putDouble("Min", this.rangeMin);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		this.range = compound.getDouble("Max");
		this.rangeMin = compound.getDouble("Min");
	}
	
	public boolean isInRange(double range){ return range <= this.range && range >= this.rangeMin; }
	
	public static class Builder extends Ability.Builder
	{
		public Ability create(CompoundNBT compound)
		{
			double range = compound.contains("Max", 6) ? compound.getDouble("Max") : 16;
			return new AbilityBlindsight(range, compound.getDouble("Min"));
		}
	}
}
