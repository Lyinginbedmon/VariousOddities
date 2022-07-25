package com.lying.variousoddities.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class PotionTempHP extends PotionVO implements IStackingPotion
{
	public PotionTempHP()
	{
		super(MobEffectCategory.BENEFICIAL, 16738895);
	}
	
	public MobEffectInstance stackInstances(MobEffectInstance applied, MobEffectInstance existing, LivingEntity living)
	{
		int existingHP = Math.min((int)living.getAbsorptionAmount(), existing.getAmplifier() + 1);
		return new MobEffectInstance(applied.getEffect(), applied.getDuration(), (existingHP + applied.getAmplifier() + 1) - 1, applied.isAmbient(), applied.isVisible());
	}
	
	public void removeAttributeModifiers(LivingEntity entityLivingBaseIn, AttributeMap attributeMapIn, int amplifier)
	{
		entityLivingBaseIn.setAbsorptionAmount(entityLivingBaseIn.getAbsorptionAmount() - (float)(amplifier + 1));
		super.removeAttributeModifiers(entityLivingBaseIn, attributeMapIn, amplifier);
	}
	
	public void addAttributeModifiers(LivingEntity entityLivingBaseIn, AttributeMap attributeMapIn, int amplifier)
	{
		entityLivingBaseIn.setAbsorptionAmount(entityLivingBaseIn.getAbsorptionAmount() + (float)(amplifier + 1));
		super.addAttributeModifiers(entityLivingBaseIn, attributeMapIn, amplifier);
	}
}
