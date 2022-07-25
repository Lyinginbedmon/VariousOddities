package com.lying.variousoddities.potion;

import java.util.UUID;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class PotionHealthDamage extends PotionVO implements IStackingPotion
{
	private static final UUID DAMAGE_UUID = UUID.fromString("d1f56f59-ce94-4eb3-b586-a0ac40811798");
	
	public PotionHealthDamage()
	{
		this(DAMAGE_UUID, 3146242);
	}
	
	protected PotionHealthDamage(UUID modifierID, int colorIn)
	{
		super(MobEffectCategory.HARMFUL, colorIn);
		addAttributeModifier(Attributes.MAX_HEALTH, modifierID.toString(), -1D, AttributeModifier.Operation.ADDITION);
	}
	
	public void addAttributeModifiers(LivingEntity entityLivingBaseIn, AttributeMap attributeMapIn, int amplifier)
	{
		super.addAttributeModifiers(entityLivingBaseIn, attributeMapIn, amplifier);
		float max = entityLivingBaseIn.getMaxHealth();
		if(max <= 0)
		{
			if(!entityLivingBaseIn.hurt(DamageSource.STARVE, Float.MAX_VALUE))
				entityLivingBaseIn.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
		}
		else if(entityLivingBaseIn.getHealth() > max)
			entityLivingBaseIn.setHealth(max);
	}
}
