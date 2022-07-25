package com.lying.variousoddities.species.abilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public abstract class AbilityModifier extends Ability
{
	protected double amount = 2F;
	
	public AbilityModifier(ResourceLocation registryName, double amountIn)
	{
		super(registryName);
		this.amount = amountIn;
	}
	
	public int compare(Ability abilityIn)
	{
		AbilityModifier armour = (AbilityModifier)abilityIn;
		return armour.amount < amount ? 1 : armour.amount > amount ? -1 : 0;
	}
	
	public double amount(){ return this.amount; }
	
	public Component translatedAmount(){ return Component.literal(amount() <= 0 ? String.valueOf((int)amount()) : "+"+(int)amount); }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.putDouble("Amount", this.amount);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		this.amount = compound.getDouble("Amount");
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::applyModifier);
	}
	
	public abstract void applyModifier(LivingTickEvent event);
}
