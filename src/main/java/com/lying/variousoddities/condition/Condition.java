package com.lying.variousoddities.condition;

import java.util.Map.Entry;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/** Conditions are essentially status effects with data storage, allowing for more complex operations */
public abstract class Condition
{
	public static final ResourceKey<Registry<Condition>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(Reference.ModInfo.MOD_ID, "conditions"));
	
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
		for(Entry<ResourceKey<Condition>, Condition> entry : VORegistries.CONDITIONS_REGISTRY.get().getEntries())
			if(entry.getValue() == this)
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
