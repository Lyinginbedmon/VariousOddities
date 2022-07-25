package com.lying.variousoddities.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class PotionDazed extends PotionImmobility implements IVisualPotion
{
	public PotionDazed(int colorIn)
	{
		super(MobEffectCategory.HARMFUL, colorIn);
	}
	
	public void addAttributeModifiers(LivingEntity entityLivingBaseIn, AttributeMap attributeMapIn, int amplifier)
	{
		if(entityLivingBaseIn.getType() == EntityType.PLAYER)
			return;
		super.addAttributeModifiers(entityLivingBaseIn, attributeMapIn, amplifier);
	}
}
