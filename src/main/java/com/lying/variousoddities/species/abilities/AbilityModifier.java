package com.lying.variousoddities.species.abilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
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
	
	public ITextComponent translatedAmount(){ return new StringTextComponent(amount() <= 0 ? String.valueOf((int)amount()) : "+"+(int)amount); }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.putDouble("Amount", this.amount);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		this.amount = compound.getDouble("Amount");
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::applyModifier);
	}
	
	public abstract void applyModifier(LivingUpdateEvent event);
}
