package com.lying.variousoddities.types.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AbilityBlind extends AbilityStatusEffect
{
	public AbilityBlind()
	{
		super(new EffectInstance(Effects.BLINDNESS, Reference.Values.TICKS_PER_SECOND * 5, 4, false, false));
	}
	
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "blind");
	
	public ResourceLocation getRegistryName(){ return REGISTRY_NAME; }
	public ResourceLocation getMapName(){ return REGISTRY_NAME; }
	public ITextComponent translatedName(){ return new TranslationTextComponent("ability."+getMapName()); }
	
	public Type getType(){ return Ability.Type.WEAKNESS; }
	
	public static class Builder extends Ability.Builder
	{
		public Ability create(CompoundNBT compound)
		{
			return new AbilityBlind();
		}
	}
}
