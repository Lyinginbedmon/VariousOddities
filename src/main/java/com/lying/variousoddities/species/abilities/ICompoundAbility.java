package com.lying.variousoddities.species.abilities;

import java.util.List;

public interface ICompoundAbility
{
	public List<Ability> getSubAbilities();
	
	public default int abilityCount(){ return getSubAbilities().size(); }
}
