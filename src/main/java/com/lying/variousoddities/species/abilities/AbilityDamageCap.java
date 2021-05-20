package com.lying.variousoddities.species.abilities;

import java.util.Map;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityDamageCap extends Ability
{
	public static final ResourceLocation REGISTRY_NAME	= new ResourceLocation(Reference.ModInfo.MOD_ID, "epic_resilience");
	
	private float hard = 20F;
	private float soft = -1F;
	
	public AbilityDamageCap(float hardCap)
	{
		super(REGISTRY_NAME);
		this.hard = hardCap;
	}
	
	public AbilityDamageCap(float hardCap, float softCap)
	{
		this(Math.max(hardCap, softCap));
		this.soft = Math.min(hardCap, softCap);
	}
	
	public ITextComponent translatedName()
	{
		if(this.hard > 0 || this.soft > 0)
			return new TranslationTextComponent("ability."+Reference.ModInfo.MOD_ID+".epic_resilience.value", (int)(this.hard > 0 ? this.hard : this.soft));
		else
			return super.translatedName();
	}
	
	public Type getType(){ return Type.DEFENSE; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::limitDamage);
	}
	
	public void limitDamage(LivingDamageEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		Map<ResourceLocation, Ability> abilities = AbilityRegistry.getCreatureAbilities(entity);
		if(abilities.containsKey(REGISTRY_NAME))
		{
			AbilityDamageCap cap = (AbilityDamageCap)abilities.get(REGISTRY_NAME);
			float amount = event.getAmount();
			
			if(cap.soft > 0 && amount > cap.soft)
				amount = cap.soft + (amount - cap.soft) * 0.1F;
			
			if(cap.hard > 0)
				amount = Math.min(cap.hard, amount);
			
			event.setAmount(amount);
		}
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		if(this.hard > 0)
			compound.putFloat("Max", this.hard);
		if(this.soft > 0)
			compound.putFloat("SoftCap", this.soft);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
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
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
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
