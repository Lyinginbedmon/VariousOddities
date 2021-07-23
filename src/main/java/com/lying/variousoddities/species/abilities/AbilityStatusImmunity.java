package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;

public abstract class AbilityStatusImmunity extends Ability
{
	protected AbilityStatusImmunity(ResourceLocation registryNameIn)
	{
		super(registryNameIn);
	}
	
	public Type getType(){ return Type.DEFENSE; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public abstract boolean appliesToStatus(EffectInstance effectIn);
	
	public static class AbilityPoisonImmunity extends AbilityStatusImmunity
	{
		public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "poison_immunity");
		
		public AbilityPoisonImmunity()
		{
			super(REGISTRY_NAME);
		}
		
		public boolean appliesToStatus(EffectInstance effectIn){ return effectIn.getPotion() == Effects.POISON; }
		
		public static class Builder extends Ability.Builder
		{
			public Builder(){ super(REGISTRY_NAME); }
			public Ability create(CompoundNBT compound){ return new AbilityPoisonImmunity(); }
		}
	}
	
	public static class AbilityParalysisImmunity extends AbilityStatusImmunity
	{
		public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "paralysis_immunity");
		
		public AbilityParalysisImmunity()
		{
			super(REGISTRY_NAME);
		}
		
		public boolean appliesToStatus(EffectInstance effectIn){ return VOPotions.isParalysisEffect(effectIn); }
		
		public static class Builder extends Ability.Builder
		{
			public Builder(){ super(REGISTRY_NAME); }
			public Ability create(CompoundNBT compound){ return new AbilityParalysisImmunity(); }
		}
	}
}
