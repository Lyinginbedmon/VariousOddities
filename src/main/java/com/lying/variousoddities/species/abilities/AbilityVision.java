package com.lying.variousoddities.species.abilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public abstract class AbilityVision extends Ability
{
	protected double range;
	private double rangeMin = 0D;
	
	public AbilityVision(ResourceLocation registryName, double rangeIn)
	{
		super(registryName);
		this.range = Math.max(4D, rangeIn);
	}
	
	public AbilityVision(ResourceLocation registryName, double rangeIn, double rangeMinIn)
	{
		this(registryName, rangeIn);
		this.rangeMin = rangeMinIn;
	}
	
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
	
	public abstract boolean testEntity(Entity entity, LivingEntity owner);
}
