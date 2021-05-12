package com.lying.variousoddities.species.abilities;

import javax.annotation.Nonnull;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class AbilityWaterWalking extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "water_walking");
	
	private boolean affectWater = true;
	private boolean affectLava = false;
	
	public AbilityWaterWalking()
	{
		this(true, false);
	}
	
	public AbilityWaterWalking(boolean water, boolean lava)
	{
		super(REGISTRY_NAME);
		this.affectWater = water;
		this.affectLava = lava;
	}
	
	public Type getType(){ return Type.UTILITY; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.putBoolean("Water", this.affectWater);
		compound.putBoolean("Lava", this.affectLava);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		this.affectWater = compound.getBoolean("Water");
		this.affectLava = compound.getBoolean("Lava");
	}
	
	public boolean affectsFluid(@Nonnull FluidState fluidIn)
	{
		return fluidIn.getFluid() == Fluids.WATER && this.affectWater || fluidIn.getFluid() == Fluids.LAVA && this.affectLava;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			boolean water = compound.contains("Water", 1) ? compound.getBoolean("Water") : true;
			boolean lava = compound.contains("Lava", 1) ? compound.getBoolean("Lava") : false;
			return new AbilityWaterWalking(water, lava);
		}
	}
}
