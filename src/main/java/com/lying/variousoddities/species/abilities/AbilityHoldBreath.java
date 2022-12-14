package com.lying.variousoddities.species.abilities;

import net.minecraft.nbt.CompoundTag;

public class AbilityHoldBreath extends Ability
{
	public AbilityHoldBreath(){ super(); }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Ability.Type.UTILITY; }
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityHoldBreath();
		}
	}
}
