package com.lying.variousoddities.condition;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.variousoddities.init.VORegistries;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.RegistryObject;

/** Conditions are essentially status effects with data storage, allowing for more complex operations */
public abstract class Condition
{
	/** Returns true if this condition prevents mobs from targeting the originator */
	public boolean affectsMobTargeting() { return false; }
	
	public boolean canAffect(LivingEntity entity) { return true; }
	
	/** Called by LivingData when a condition is first applied */
	public void start(LivingEntity entity, UUID originID, CompoundTag storage) { }
	
	/** Called every tick by LivingData */
	public void tick(LivingEntity entity, UUID originID, CompoundTag storage, int ticksRemaining) { }
	
	/** Called by LivingData when a condition is removed before its time expires */
	public void reset(LivingEntity entity, UUID originID, CompoundTag storage) { }
	
	/** Called by LivingData when a condition expires */
	public void end(LivingEntity entity, UUID originID, CompoundTag storage) { }
	
	public @Nullable ResourceKey<Condition> getKey()
	{
		for(RegistryObject<Condition> entry : VORegistries.CONDITIONS.getEntries())
			if(entry.get() == this)
				return entry.getKey();
		return null;
	}
	
	public ResourceLocation getIconTexture(boolean affecting)
	{
		return new ResourceLocation(getKey().location().getNamespace(), "textures/condition/"+getKey().location().getPath().toLowerCase()+".png");
	}
	
	/** A simple does-nothing condition used mainly as a data point by other processes */
	public static class Dummy extends Condition { }
}
