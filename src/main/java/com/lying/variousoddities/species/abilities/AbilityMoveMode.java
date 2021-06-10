package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;

public abstract class AbilityMoveMode extends ActivatedAbility
{
	protected boolean isActive = false;
	
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
	
	public boolean canTrigger(LivingEntity entity)
	{
		return AbilityRegistry.hasAbility(entity, getMapName()) && !LivingData.forEntity(entity).getAbilities().isAbilityOnCooldown(getMapName());
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
	
	public boolean isActive(){ return this.isActive; }
}
