package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.resources.ResourceLocation;

public abstract class AbilityMoveMode extends ToggledAbility
{
	protected AbilityMoveMode(ResourceLocation registryName)
	{
		super(registryName, Reference.Values.TICKS_PER_SECOND);
	}
	
	public Type getType(){ return Type.UTILITY; }
}
