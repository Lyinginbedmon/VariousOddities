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

public class AbilityModifierStr extends AbilityModifier
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "strength_modifier");
	private static final UUID STRENGTH_UUID = UUID.fromString("6444940e-a83a-4a76-a2ed-1847d8e1ebd0");
	
	public AbilityModifierStr(double amountIn)
	{
		super(REGISTRY_NAME, amountIn);
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return this.amount >= 0 ? Type.ATTACK : Type.WEAKNESS; }
	
	public ITextComponent translatedName()
	{
		return new TranslationTextComponent("ability.varodd.strength_modifier", translatedAmount());
	}
	
	public void applyModifier(LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		ModifiableAttributeInstance attribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
		if(attribute == null)
			return;
		
		if(AbilityRegistry.hasAbility(entity, getMapName()))
		{
			AbilityModifierStr armour = (AbilityModifierStr)AbilityRegistry.getAbilityByName(entity, getMapName());
			double amount = armour.amount;
			
			AttributeModifier modifier = attribute.getModifier(STRENGTH_UUID);
			if(modifier != null && modifier.getAmount() != amount)
			{
				attribute.removeModifier(STRENGTH_UUID);
				modifier = null;
			}
			
			if(modifier == null)
			{
				modifier = new AttributeModifier(STRENGTH_UUID, "strength_modifier", amount, Operation.ADDITION);
				attribute.applyPersistentModifier(modifier);
			}
			
		}
		else if(attribute.getModifier(STRENGTH_UUID) != null)
			attribute.removeModifier(STRENGTH_UUID);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			double amount = compound.contains("Amount", 6) ? compound.getDouble("Amount") : 2F;
			return new AbilityModifierStr(amount);
		}
	}
}
