package com.lying.variousoddities.condition;

import com.lying.variousoddities.magic.IMagicEffect.MagicSchool;
import com.lying.variousoddities.magic.IMagicEffect.MagicSubType;
import com.lying.variousoddities.species.abilities.AbilityResistanceSpell;

import net.minecraft.world.entity.LivingEntity;

public class ConditionMindAffecting extends Condition
{
	private final MagicSchool school;
	private final MagicSubType descriptor;
	
	public ConditionMindAffecting(MagicSchool schoolIn)
	{
		this.school = schoolIn;
		this.descriptor = null;
	}
	
	public ConditionMindAffecting(MagicSubType descriptorIn)
	{
		this.school = null;
		this.descriptor = descriptorIn;
	}
	
	public boolean affectsMobTargeting() { return true; }
	
	public boolean canAffect(LivingEntity entity)
	{
		return entity.canChangeDimensions() && AbilityResistanceSpell.canSpellAffectMob(entity, school, descriptor);
	}
}
