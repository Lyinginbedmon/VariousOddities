package com.lying.variousoddities.species.abilities;

import net.minecraft.nbt.CompoundTag;

public class AbilityImmunityCrits extends Ability
{
	public AbilityImmunityCrits()
	{
		super();
	}
	
	public Type getType(){ return Type.DEFENSE; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		public Ability create(CompoundTag compound){ return new AbilityImmunityCrits(); }
	}
}
