package com.lying.variousoddities.potion;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.potion.EffectType;

public class PotionDazed extends PotionImmobility implements IVisualPotion
{
	public PotionDazed(int colorIn)
	{
		super("dazed", EffectType.HARMFUL, colorIn);
	}
	
	public void applyAttributesModifiersToEntity(LivingEntity entityLivingBaseIn, AttributeModifierManager attributeMapIn, int amplifier)
	{
		if(entityLivingBaseIn.getType() == EntityType.PLAYER)
			return;
		super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
	}
}
