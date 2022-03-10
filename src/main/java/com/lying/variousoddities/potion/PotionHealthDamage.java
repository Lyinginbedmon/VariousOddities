package com.lying.variousoddities.potion;

import java.util.UUID;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;

public class PotionHealthDamage extends PotionVO implements IStackingPotion
{
	private static final UUID DAMAGE_UUID = UUID.fromString("d1f56f59-ce94-4eb3-b586-a0ac40811798");
	
	public PotionHealthDamage()
	{
		this("health_damage", DAMAGE_UUID, 3146242);
	}
	
	protected PotionHealthDamage(String nameIn, UUID modifierID, int colorIn)
	{
		super(nameIn, EffectType.HARMFUL, colorIn);
		addAttributesModifier(Attributes.MAX_HEALTH, modifierID.toString(), -1D, AttributeModifier.Operation.ADDITION);
	}
	
	public void applyAttributesModifiersToEntity(LivingEntity entityLivingBaseIn, AttributeModifierManager attributeMapIn, int amplifier)
	{
		super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
		float max = entityLivingBaseIn.getMaxHealth();
		if(max <= 0)
		{
			if(!entityLivingBaseIn.attackEntityFrom(DamageSource.STARVE, Float.MAX_VALUE))
				entityLivingBaseIn.attackEntityFrom(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
		}
		else if(entityLivingBaseIn.getHealth() > max)
			entityLivingBaseIn.setHealth(max);
	}
}
