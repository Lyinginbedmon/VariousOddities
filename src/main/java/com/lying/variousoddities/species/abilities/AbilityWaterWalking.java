package com.lying.variousoddities.species.abilities;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class AbilityWaterWalking extends Ability
{
	private boolean affectWater = true;
	private boolean affectLava = false;
	
	public AbilityWaterWalking()
	{
		this(true, false);
	}
	
	public AbilityWaterWalking(boolean water, boolean lava)
	{
		super();
		this.affectWater = water;
		this.affectLava = lava;
	}
	
	protected Nature getDefaultNature(){ return Nature.SPELL_LIKE; }
	
	public Type getType(){ return Type.UTILITY; }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.putBoolean("Water", this.affectWater);
		compound.putBoolean("Lava", this.affectLava);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		this.affectWater = compound.getBoolean("Water");
		this.affectLava = compound.getBoolean("Lava");
	}
	
	public boolean affectsFluid(@Nonnull FluidState fluidIn)
	{
		return fluidIn.getFluidType() == Fluids.WATER.getFluidType() && this.affectWater || fluidIn.getFluidType() == Fluids.LAVA.getFluidType() && this.affectLava;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			boolean water = compound.contains("Water", 1) ? compound.getBoolean("Water") : true;
			boolean lava = compound.contains("Lava", 1) ? compound.getBoolean("Lava") : false;
			return new AbilityWaterWalking(water, lava);
		}
	}
}
