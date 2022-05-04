package com.lying.variousoddities.condition;

import java.util.UUID;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

/** Conditions are essentially status effects with data storage, allowing for more complex operations */
public abstract class Condition extends ForgeRegistryEntry<Condition>
{
	/** Returns true if this condition prevents mobs from targeting the originator */
	public boolean affectsMobTargeting() { return false; }
	
	public boolean canAffect(LivingEntity entity) { return true; }
	
	/** Called by LivingData when a condition is first applied */
	public void start(LivingEntity entity, UUID originID, CompoundNBT storage) { }
	
	/** Called every tick by LivingData */
	public void tick(LivingEntity entity, UUID originID, CompoundNBT storage, int ticksRemaining) { }
	
	/** Called by LivingData when a condition is removed before its time expires */
	public void reset(LivingEntity entity, UUID originID, CompoundNBT storage) { }
	
	/** Called by LivingData when a condition expires */
	public void end(LivingEntity entity, UUID originID, CompoundNBT storage) { }
	
	public ResourceLocation getIconTexture(boolean affecting)
	{
		return new ResourceLocation(getRegistryName().getNamespace(), "textures/condition/"+getRegistryName().getPath().toLowerCase()+".png");
	}
	
	/** A simple does-nothing condition used mainly as a data point by other processes */
	public static class Dummy extends Condition { }
}
