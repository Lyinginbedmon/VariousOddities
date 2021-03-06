package com.lying.variousoddities.species.abilities;

import java.util.UUID;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
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
	
	public ITextComponent translatedName()
	{
		return new TranslationTextComponent("ability.varodd.natural_armour", (int)amount);
	}
	
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
		bus.addListener(this::applyArmour);
	}
	
	public void applyArmour(LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		ModifiableAttributeInstance attribute = entity.getAttribute(Attributes.ARMOR);
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
				attribute.applyPersistentModifier(modifier);
			}
			
		}
		else if(attribute.getModifier(NATURAL_ARMOUR_UUID) != null)
			attribute.removeModifier(NATURAL_ARMOUR_UUID);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			double amount = compound.contains("Amount", 6) ? compound.getDouble("Amount") : 2F;
			return new AbilityNaturalArmour(amount);
		}
	}
}
