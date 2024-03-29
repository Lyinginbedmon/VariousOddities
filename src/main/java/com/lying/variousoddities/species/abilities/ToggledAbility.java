package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.capabilities.AbilityData;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;

public abstract class ToggledAbility extends ActivatedAbility
{
	protected boolean isActive = false;
	
	protected ToggledAbility(int cooldownIn)
	{
		super(cooldownIn);
	}
	
	protected ToggledAbility()
	{
		this(Reference.Values.TICKS_PER_SECOND / 2);
	}
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		super.writeToNBT(compound);
		compound.putBoolean("IsActive", isActive);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		super.readFromNBT(compound);
		this.isActive = compound.getBoolean("IsActive");
	}
	
	public boolean canTrigger(LivingEntity entity)
	{
		return AbilityRegistry.hasAbilityOfMapName(entity, getMapName()) && !AbilityData.getCapability(entity).isAbilityOnCooldown(getMapName());
	}
	
	public void trigger(LivingEntity entity, Dist side)
	{
		switch(side)
		{
			case CLIENT:
				break;
			default:
				this.isActive = !this.isActive;
				putOnCooldown(entity, getCooldown());
				break;
		}
	}
	
	@Override
	public boolean isActive(){ return this.isActive; }
	
	public static abstract class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			ToggledAbility ability = createAbility(compound);
			ability.readFromNBT(compound);
			return ability;
		}
		
		public abstract ToggledAbility createAbility(CompoundTag compound);
	}
}
