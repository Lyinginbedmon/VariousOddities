package com.lying.variousoddities.potion;

import java.util.UUID;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class PotionDazzled extends PotionVO
{
	private static final UUID DAZZLED_UUID = UUID.fromString("7ed62afd-18ce-47c6-83e1-d817fd42e21d");
	
	public PotionDazzled(int colorIn)
	{
		super(MobEffectCategory.HARMFUL, colorIn);
		addAttributeModifier(Attributes.ATTACK_DAMAGE, DAZZLED_UUID.toString(), -1.0D, Operation.ADDITION);
	}
}
