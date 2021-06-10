package com.lying.variousoddities.api.event;

import com.lying.variousoddities.capabilities.Abilities;
import com.lying.variousoddities.species.abilities.Ability;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class AbilityEvent extends LivingEvent
{
	private final Abilities abilities;
	
	public AbilityEvent(LivingEntity entity, Abilities abilitiesIn)
	{
		super(entity);
		this.abilities = abilitiesIn;
	}
	
	public Abilities getAbilities(){ return this.abilities; }
	
	public static class AbilityUpdateEvent extends AbilityEvent
	{
		public AbilityUpdateEvent(LivingEntity entity, Abilities abilitiesIn)
		{
			super(entity, abilitiesIn);
		}
	}
	
	public static class AbilityAddEvent extends AbilityEvent
	{
		private final Ability ability;
		
		public AbilityAddEvent(LivingEntity entity, Ability abilityIn, Abilities abilitiesIn)
		{
			super(entity, abilitiesIn);
			this.ability = abilityIn;
		}
		
		public Ability getAbility(){ return this.ability; }
	}
	
	public static class AbilityRemoveEvent extends AbilityEvent
	{
		private final Ability ability;
		
		public AbilityRemoveEvent(LivingEntity entity, Ability abilityIn, Abilities abilitiesIn)
		{
			super(entity, abilitiesIn);
			this.ability = abilityIn;
		}
		
		public Ability getAbility(){ return this.ability; }
	}
}
