package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class AbilityHoldBreath extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "hold_breath");
	
	public AbilityHoldBreath(){ super(REGISTRY_NAME); }
	
	public Type getType(){ return Ability.Type.UTILITY; }
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityHoldBreath();
		}
	}
}
