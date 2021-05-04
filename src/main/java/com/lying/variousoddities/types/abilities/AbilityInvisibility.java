package com.lying.variousoddities.types.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;

public class AbilityInvisibility extends AbilityStatusEffect
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "invisibility");
	
	public AbilityInvisibility()
	{
		super(new EffectInstance(Effects.INVISIBILITY, Reference.Values.TICKS_PER_SECOND * 10, 0, false, true));
	}
	
	public ResourceLocation getRegistryName(){ return REGISTRY_NAME; }
	
	public ResourceLocation getMapName(){ return getRegistryName(); }
	
	public static class Builder extends Ability.Builder
	{
		public Ability create(CompoundNBT compound)
		{
			return new AbilityInvisibility();
		}
	}
}
