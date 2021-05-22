package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AbilityClimb extends AbilityMoveMode
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "climb");
	
	public AbilityClimb()
	{
		super(REGISTRY_NAME);
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public ITextComponent translatedName(){ return new TranslationTextComponent("ability.varodd.climb."+(active() ? "active" : "inactive")); }
	
	public static class Builder extends Ability.Builder
	{
		public Builder()
		{
			super(REGISTRY_NAME);
		}
		
		public Ability create(CompoundNBT compound)
		{
			AbilityClimb climb = new AbilityClimb();
			climb.isActive = compound.getBoolean("IsActive");
			return climb;
		}
	}
}
