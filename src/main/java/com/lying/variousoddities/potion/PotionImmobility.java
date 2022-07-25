package com.lying.variousoddities.potion;

import java.util.UUID;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

public abstract class PotionImmobility extends PotionVO
{
	private static final UUID PARALYSIS_UUID = UUID.fromString("94b3271f-7c76-4230-88d7-f294ee6d4f7f");
	
	protected PotionImmobility(MobEffectCategory typeIn, int color)
	{
		super(typeIn, color);
		addAttributeModifier(Attributes.MOVEMENT_SPEED, PARALYSIS_UUID.toString(), -1.0D, Operation.MULTIPLY_TOTAL);
		addAttributeModifier(Attributes.FLYING_SPEED, PARALYSIS_UUID.toString(), -1.0D, Operation.MULTIPLY_TOTAL);
		addAttributeModifier(Attributes.ATTACK_DAMAGE, PARALYSIS_UUID.toString(), -1.0D, Operation.MULTIPLY_TOTAL);
	}
	
    public boolean isReady(int duration, int amplifier){ return true; }
}
