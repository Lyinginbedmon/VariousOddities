package com.lying.variousoddities.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;

public class PotionTempHP extends PotionVO implements IStackingPotion
{
	public PotionTempHP()
	{
		super("temp_health", EffectType.BENEFICIAL, 16738895);
	}
	
	public EffectInstance stackInstances(EffectInstance applied, EffectInstance existing, LivingEntity living)
	{
		int existingHP = Math.min((int)living.getAbsorptionAmount(), existing.getAmplifier() + 1);
		return new EffectInstance(applied.getPotion(), applied.getDuration(), (existingHP + applied.getAmplifier() + 1) - 1, applied.isAmbient(), applied.doesShowParticles());
	}
	
	public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn, AttributeModifierManager attributeMapIn, int amplifier)
	{
		entityLivingBaseIn.setAbsorptionAmount(entityLivingBaseIn.getAbsorptionAmount() - (float)(amplifier + 1));
		super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
	}
	
	public void applyAttributesModifiersToEntity(LivingEntity entityLivingBaseIn, AttributeModifierManager attributeMapIn, int amplifier)
	{
		entityLivingBaseIn.setAbsorptionAmount(entityLivingBaseIn.getAbsorptionAmount() + (float)(amplifier + 1));
		super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
	}
}
