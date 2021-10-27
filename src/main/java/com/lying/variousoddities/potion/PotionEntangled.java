package com.lying.variousoddities.potion;

import java.util.UUID;

import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.EffectType;

public class PotionEntangled extends PotionImmobility implements IVisualPotion
{
	private static final UUID ENTANGLED_UUID = UUID.fromString("3cbd6a5c-84f9-434b-a617-e05c33ef4862");
	
	public PotionEntangled(int colorIn)
	{
		super("entangled", EffectType.HARMFUL, colorIn);
		
		addAttributesModifier(Attributes.MOVEMENT_SPEED, ENTANGLED_UUID.toString(), -0.99D, Operation.MULTIPLY_TOTAL);
		addAttributesModifier(Attributes.FLYING_SPEED, ENTANGLED_UUID.toString(), -0.99D, Operation.MULTIPLY_TOTAL);
	}
	
    public boolean isReady(int duration, int amplifier){ return true; }
}
