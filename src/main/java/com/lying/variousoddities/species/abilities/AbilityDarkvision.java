package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;

public class AbilityDarkvision extends AbilityStatusEffect
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "darkvision");
	
	public AbilityDarkvision()
	{
		super(REGISTRY_NAME, new EffectInstance(Effects.NIGHT_VISION, Reference.Values.TICKS_PER_SECOND * 10, 0, false, false));
	}
	
	public ResourceLocation getMapName(){ return getRegistryName(); }
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityDarkvision();
		}
	}
}
