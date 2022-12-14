package com.lying.variousoddities.species.abilities;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;

public class AbilityModifierCon extends AbilityModifier
{
	private static final UUID CON_MODIFIER = UUID.fromString("6444940e-a83a-4a76-a2ed-1847d8e1ebd0");
	
	public AbilityModifierCon(double amountIn)
	{
		super(amountIn);
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return this.amount >= 0 ? Type.DEFENSE : Type.WEAKNESS; }
	
	public boolean displayInSpecies(){ return false; }
	
	public Component translatedName()
	{
		return Component.translatable("ability.varodd.constitution_modifier", translatedAmount());
	}
	
	public void applyModifier(LivingTickEvent event)
	{
		LivingEntity entity = event.getEntity();
		AttributeInstance attribute = entity.getAttribute(Attributes.MAX_HEALTH);
		if(attribute == null)
			return;

		AbilityModifierCon armour = (AbilityModifierCon)AbilityRegistry.getAbilityByMapName(entity, getMapName());
		if(armour != null)
		{
			double amount = armour.amount;
			
			AttributeModifier modifier = attribute.getModifier(CON_MODIFIER);
			if(modifier != null && modifier.getAmount() != amount)
			{
				attribute.removeModifier(CON_MODIFIER);
				modifier = null;
			}
			
			if(modifier == null)
			{
				modifier = new AttributeModifier(CON_MODIFIER, "constitution_modifier", amount, Operation.ADDITION);
				attribute.addPermanentModifier(modifier);
			}
			
		}
		else if(attribute.getModifier(CON_MODIFIER) != null)
			attribute.removeModifier(CON_MODIFIER);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			double amount = compound.contains("Amount", 6) ? compound.getDouble("Amount") : 2F;
			return new AbilityModifierCon(amount);
		}
	}
}
