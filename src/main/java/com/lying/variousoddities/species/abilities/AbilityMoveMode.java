package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;

public abstract class AbilityMoveMode extends ActivatedAbility
{
	protected boolean isActive = true;
	
	protected AbilityMoveMode(ResourceLocation registryName)
	{
		super(registryName, Reference.Values.TICKS_PER_SECOND);
	}
	
	public Type getType(){ return Type.UTILITY; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		super.writeToNBT(compound);
		compound.putBoolean("IsActive", isActive);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		super.readFromNBT(compound);
		this.isActive = compound.getBoolean("IsActive");
	}
	
	public void trigger(LivingEntity entity, Dist side)
	{
		switch(side)
		{
			case CLIENT:
				break;
			default:
				this.isActive = !this.isActive;
				putOnCooldown(entity);
				break;
		}
	}
	
	public boolean active(){ return this.isActive; }
}
