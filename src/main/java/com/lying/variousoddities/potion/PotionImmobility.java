package com.lying.variousoddities.potion;

import java.util.UUID;

import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.potion.EffectType;

public abstract class PotionImmobility extends PotionVO
{
	private static final UUID PARALYSIS_UUID = UUID.fromString("94b3271f-7c76-4230-88d7-f294ee6d4f7f");
	
	protected PotionImmobility(String name, EffectType typeIn, int color)
	{
		super(name, typeIn, color);
		addAttributesModifier(Attributes.MOVEMENT_SPEED, PARALYSIS_UUID.toString(), -1.0D, Operation.MULTIPLY_TOTAL);
		addAttributesModifier(Attributes.FLYING_SPEED, PARALYSIS_UUID.toString(), -1.0D, Operation.MULTIPLY_TOTAL);
		addAttributesModifier(Attributes.ATTACK_DAMAGE, PARALYSIS_UUID.toString(), -1.0D, Operation.MULTIPLY_TOTAL);
	}
	
    public boolean isReady(int duration, int amplifier){ return true; }
}
