package com.lying.variousoddities.species.abilities;

import java.util.Map;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityDamageCap extends Ability
{
	private float hard = 20F;
	private float soft = -1F;
	
	public AbilityDamageCap(float hardCap)
	{
		super();
		this.hard = hardCap;
	}
	
	public AbilityDamageCap(float hardCap, float softCap)
	{
		this(Math.max(hardCap, softCap));
		this.soft = Math.min(hardCap, softCap);
	}
	
	public int compare(Ability abilityIn)
	{
		AbilityDamageCap damage = (AbilityDamageCap)abilityIn;
		if(damage.hard != hard)
			return damage.hard < hard ? -1 : 1;
		return damage.soft < soft ? -1 : damage.soft > soft ? 1 : 0;
	}
	
	public Component translatedName()
	{
		if(this.hard > 0 || this.soft > 0)
			return Component.translatable("ability."+Reference.ModInfo.MOD_ID+".epic_resilience.value", (int)(this.hard > 0 ? this.hard : this.soft));
		else
			return super.translatedName();
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Type.DEFENSE; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::limitDamage);
	}
	
	public void limitDamage(LivingDamageEvent event)
	{
		LivingEntity entity = event.getEntity();
		Map<ResourceLocation, Ability> abilities = AbilityRegistry.getCreatureAbilities(entity);
		if(abilities.containsKey(getRegistryName()))
		{
			AbilityDamageCap cap = (AbilityDamageCap)abilities.get(getRegistryName());
			float amount = event.getAmount();
			
			if(cap.soft > 0 && amount > cap.soft)
				amount = cap.soft + (amount - cap.soft) * 0.1F;
			
			if(cap.hard > 0)
				amount = Math.min(cap.hard, amount);
			
			event.setAmount(amount);
		}
	}
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		if(this.hard > 0)
			compound.putFloat("Max", this.hard);
		if(this.soft > 0)
			compound.putFloat("SoftCap", this.soft);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		this.hard = compound.contains("Max", 5) ? compound.getFloat("Max") : -1;
		this.soft = compound.contains("SoftCap", 5) ? compound.getFloat("SoftCap") : -1;
		
		if(this.hard > 0 && this.soft > 0)
		{
			float valA = this.hard;
			float valB = this.soft;
			this.hard = Math.max(valA, valB);
			this.soft = Math.min(valA, valB);
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			float hard = compound.contains("Max", 5) ? compound.getFloat("Max") : -1;
			float soft = compound.contains("SoftCap", 5) ? compound.getFloat("SoftCap") : -1;
			
			if(hard > 0 && soft > 0)
			{
				float valA = hard;
				float valB = soft;
				hard = Math.max(valA, valB);
				soft = Math.min(valA, valB);
			}
			
			return new AbilityDamageCap(hard, soft);
		}
	}
}
