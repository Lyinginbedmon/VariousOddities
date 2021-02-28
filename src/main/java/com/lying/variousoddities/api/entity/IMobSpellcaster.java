package com.lying.variousoddities.api.entity;

import com.lying.variousoddities.magic.IMagicEffect;

import net.minecraft.entity.LivingEntity;

public interface IMobSpellcaster
{
	/**
	 * Returns the caster level of this mob, akin to the caster level check for players
	 * @return
	 */
	public default int getCasterLevel()
	{
		return -1;
	};
	
	/**
	 * Returns the spell resistance of this mob, which can render some spells ineffective based on the user's caster level
	 * @return
	 */
	public default int getSpellResistance()
	{
		return 0;
	};
	
	/**
	 * Returns true if the given spell from the given caster can affect this mob.<br>
	 * Used to implement mob-specific resistances and vulnerabilities beyond the standard mob-type effects.
	 * @param spell
	 * @return
	 */
	public default boolean canSpellAffect(IMagicEffect spell, LivingEntity caster)
	{
		return true;
	};
}
