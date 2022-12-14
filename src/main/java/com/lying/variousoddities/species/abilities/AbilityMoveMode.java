package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

public abstract class AbilityMoveMode extends ToggledAbility
{
	protected AbilityMoveMode()
	{
		super(Reference.Values.TICKS_PER_SECOND);
	}
	
	public Type getType(){ return Type.UTILITY; }
}
