package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.capabilities.AbilityData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;

public abstract class ActivatedAbility extends Ability
{
	private final int default_cooldown;
	protected int activeTicks = 0;
	protected int cooldown;
	
	protected ActivatedAbility(int cooldownIn)
	{
		super();
		this.default_cooldown = this.cooldown = cooldownIn;
	}
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.putInt("Cooldown", getCooldown());
		compound.putInt("Active", this.activeTicks);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		this.cooldown = compound.contains("Cooldown", 3) ? compound.getInt("Cooldown") : this.default_cooldown;
		this.activeTicks = compound.contains("Active", 3) ? compound.getInt("Active") : 0;
	}
	
	/** Called to check if a given ability has the suitable context in which to function. */
	public boolean canTrigger(LivingEntity entity)
	{
		return AbilityRegistry.hasAbilityOfMapName(entity, getMapName()) && !AbilityData.getCapability(entity).isAbilityOnCooldown(getMapName()) && !isActive();
	}
	
	public boolean isActive(){ return this.activeTicks > 0; }
	
	/** Called when an activated ability is triggered by its owner. */
	public abstract void trigger(LivingEntity entity, Dist side);
	
	public int getCooldown(){ return this.cooldown; }
	
	public void putOnCooldown(LivingEntity entity, int cooldown)
	{
		AbilityData.getCapability(entity).putOnCooldown(getMapName(), cooldown);
	}
	
	public void putOnCooldown(LivingEntity entity)
	{
		putOnCooldown(entity, getCooldown());
	}
	
	public void markForUpdate(LivingEntity entity)
	{
		AbilityData.getCapability(entity).markForRecache();
	}
}
