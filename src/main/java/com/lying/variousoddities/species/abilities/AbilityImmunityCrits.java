package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class AbilityImmunityCrits extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "critical_hit_immunity");
	
	public AbilityImmunityCrits()
	{
		super(REGISTRY_NAME);
	}
	
	public Type getType(){ return Type.DEFENSE; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		public Ability create(CompoundTag compound){ return new AbilityImmunityCrits(); }
	}
}
