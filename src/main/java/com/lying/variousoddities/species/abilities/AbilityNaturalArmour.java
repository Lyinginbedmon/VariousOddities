package com.lying.variousoddities.species.abilities;

import java.util.UUID;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityNaturalArmour extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "natural_armour");
	private static final UUID NATURAL_ARMOUR_UUID = UUID.fromString("edf63428-6a6b-43b4-a914-b72d963746c8");
	
	private double amount = 2F;
	
	public AbilityNaturalArmour(double amountIn)
	{
		super(REGISTRY_NAME);
		this.amount = amountIn;
	}
	
	public int compare(Ability abilityIn)
	{
		AbilityNaturalArmour armour = (AbilityNaturalArmour)abilityIn;
		return armour.amount < amount ? 1 : armour.amount > amount ? -1 : 0;
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Type.DEFENSE; }
	
	public boolean displayInSpecies(){ return false; }
	
	public double amount(){ return this.amount; }
	
	public Component translatedName()
	{
		return Component.translatable("ability.varodd.natural_armour", (int)amount);
	}
	
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
		bus.addListener(this::applyArmour);
	}
	
	public void applyArmour(LivingTickEvent event)
	{
		LivingEntity entity = event.getEntity();
		AttributeInstance attribute = entity.getAttribute(Attributes.ARMOR);
		if(attribute == null)
			return;
		
		if(AbilityRegistry.hasAbility(entity, getMapName()))
		{
			AbilityNaturalArmour armour = (AbilityNaturalArmour)AbilityRegistry.getAbilityByName(entity, getMapName());
			double amount = armour.amount;
			
			AttributeModifier modifier = attribute.getModifier(NATURAL_ARMOUR_UUID);
			if(modifier != null && modifier.getAmount() != amount)
			{
				attribute.removeModifier(NATURAL_ARMOUR_UUID);
				modifier = null;
			}
			
			if(modifier == null)
			{
				modifier = new AttributeModifier(NATURAL_ARMOUR_UUID, "natural_armour", amount, Operation.ADDITION);
				attribute.addPermanentModifier(modifier);
			}
			
		}
		else if(attribute.getModifier(NATURAL_ARMOUR_UUID) != null)
			attribute.removeModifier(NATURAL_ARMOUR_UUID);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundTag compound)
		{
			double amount = compound.contains("Amount", 6) ? compound.getDouble("Amount") : 2F;
			return new AbilityNaturalArmour(amount);
		}
	}
}
