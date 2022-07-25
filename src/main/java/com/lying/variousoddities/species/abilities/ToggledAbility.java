package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;

public abstract class ToggledAbility extends ActivatedAbility
{
	protected boolean isActive = false;
	
	protected ToggledAbility(ResourceLocation registryName, int cooldownIn)
	{
		super(registryName, cooldownIn);
	}
	
	protected ToggledAbility(ResourceLocation registryName)
	{
		this(registryName, Reference.Values.TICKS_PER_SECOND / 2);
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
				putOnCooldown(entity, getCooldown());
				break;
		}
	}
	
	@Override
	public boolean isActive(){ return this.isActive; }
	
	public static abstract class Builder extends Ability.Builder
	{
		public Builder(ResourceLocation registryName){ super(registryName); }
		
		public Ability create(CompoundTag compound)
		{
			ToggledAbility ability = createAbility(compound);
			ability.readFromNBT(compound);
			return ability;
		}
		
		public abstract ToggledAbility createAbility(CompoundTag compound);
	}
}
