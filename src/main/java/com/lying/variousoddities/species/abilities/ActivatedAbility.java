package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.capabilities.LivingData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;

public abstract class ActivatedAbility extends Ability
{
	private final int default_cooldown;
	private int cooldown;
	
	protected ActivatedAbility(ResourceLocation registryName, int cooldownIn)
	{
		super(registryName);
		this.default_cooldown = this.cooldown = cooldownIn;
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.putInt("Cooldown", getCooldown());
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		this.cooldown = compound.contains("Cooldown", 3) ? compound.getInt("Cooldown") : this.default_cooldown;
	}
	
	/** Called to check if a given ability has the suitable context in which to function. */
	public boolean canTrigger(LivingEntity entity)
	{
		return AbilityRegistry.hasAbility(entity, getMapName()) && !LivingData.forEntity(entity).getAbilities().isAbilityOnCooldown(getMapName());
	}
	
	/** Called when an activated ability is triggered by its owner. */
	public abstract void trigger(LivingEntity entity, Dist side);
	
	public int getCooldown(){ return this.cooldown; }
	
	public void putOnCooldown(LivingEntity entity)
	{
		LivingData.forEntity(entity).getAbilities().putOnCooldown(getMapName(), getCooldown());
	}
	
	public void markForUpdate(LivingEntity entity)
	{
		LivingData.forEntity(entity).getAbilities().markDirty();
	}
}
