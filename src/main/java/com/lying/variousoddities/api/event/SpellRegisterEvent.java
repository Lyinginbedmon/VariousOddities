package com.lying.variousoddities.api.event;

import com.lying.variousoddities.magic.IMagicEffect;
import com.lying.variousoddities.magic.MagicEffects;

import net.minecraftforge.eventbus.api.Event;

/**
 * Fired by MagicEffects when all spells are registered during initialisation
 * @author Lying
 *
 */
public class SpellRegisterEvent extends Event
{
	public SpellRegisterEvent(){ }
	
	public boolean isRegistered(IMagicEffect effectIn){ return MagicEffects.spellIsRegistered(effectIn); }
	
	/**
	 * Registers the given spell to the effect registry, allowing it to appear in scrolls, wands, etc. and be used in spellcasting
	 * @return The given spell, or null if it failed to register
	 */
	public IMagicEffect registerSpell(IMagicEffect effectIn){ return MagicEffects.registerSpell(effectIn); }
	
	/**
	 * Registers the given spell, overwriting any existing spell with the same name
	 * @return The given spell, or null if it failed to register
	 */
	public IMagicEffect registerSpellWithOverwrite(IMagicEffect effectIn){ return MagicEffects.registerSpell(effectIn, true); }
}
