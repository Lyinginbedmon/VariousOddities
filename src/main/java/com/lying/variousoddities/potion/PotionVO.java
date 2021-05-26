package com.lying.variousoddities.potion;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;

public class PotionVO extends Effect
{
	public PotionVO(String nameIn, EffectType badEffectIn, int colorIn)
	{
		super(badEffectIn, colorIn);
		setRegistryName(new ResourceLocation(Reference.ModInfo.MOD_ID, nameIn));
	}
}
