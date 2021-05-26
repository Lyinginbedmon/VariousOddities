package com.lying.variousoddities.potion;

import java.util.UUID;

import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.potion.EffectType;

public class PotionDazzled extends PotionVO
{
	private static final UUID DAZZLED_UUID = UUID.fromString("7ed62afd-18ce-47c6-83e1-d817fd42e21d");
	
	public PotionDazzled(int colorIn)
	{
		super("dazzled", EffectType.HARMFUL, colorIn);
		addAttributesModifier(Attributes.ATTACK_DAMAGE, DAZZLED_UUID.toString(), -1.0D, Operation.ADDITION);
	}
}
