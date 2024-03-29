package com.lying.variousoddities.api.event;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.AbilityData;
import com.lying.variousoddities.species.abilities.Ability;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

public class AbilityEvent extends LivingEvent
{
	private final AbilityData abilities;
	
	public AbilityEvent(LivingEntity entity, AbilityData abilitiesIn)
	{
		super(entity);
		this.abilities = abilitiesIn;
	}
	
	public AbilityData getAbilities(){ return this.abilities; }
	
	public static class AbilityUpdateEvent extends AbilityEvent
	{
		public AbilityUpdateEvent(LivingEntity entity, AbilityData abilitiesIn)
		{
			super(entity, abilitiesIn);
		}
	}
	
	public static class AbilityAddEvent extends AbilityEvent
	{
		private final Ability ability;
		
		public AbilityAddEvent(LivingEntity entity, Ability abilityIn, AbilityData abilitiesIn)
		{
			super(entity, abilitiesIn);
			this.ability = abilityIn;
		}
		
		public Ability getAbility(){ return this.ability; }
	}
	
	public static class AbilityRemoveEvent extends AbilityEvent
	{
		private final Ability ability;
		
		public AbilityRemoveEvent(LivingEntity entity, Ability abilityIn, AbilityData abilitiesIn)
		{
			super(entity, abilitiesIn);
			this.ability = abilityIn;
		}
		
		public Ability getAbility(){ return this.ability; }
	}
	
	public static class AbilityAffectEntityEvent extends EntityEvent
	{
		private final Ability ability;
		private final LivingEntity owner;
		
		public AbilityAffectEntityEvent(Entity entity, Ability abilityIn, LivingEntity ownerIn)
		{
			super(entity);
			this.ability = abilityIn;
			this.owner = ownerIn;
		}
		
		public Ability getAbility() { return this.ability; }
		public LivingEntity getAbilityOwner() { return this.owner; }
		
		public boolean isCancelable() { return true; }
	}
	
	public static class AbilityGetBreathableFluidEvent extends AbilityEvent
	{
		protected List<TagKey<Fluid>> breathables = Lists.newArrayList();
		
		protected AbilityGetBreathableFluidEvent(LivingEntity entity)
		{
			this(entity, AbilityData.getCapability(entity));
		}
		
		protected AbilityGetBreathableFluidEvent(LivingEntity entity, AbilityData abilitiesIn)
		{
			super(entity, abilitiesIn);
		}
		
		/**
		 * Adds the given fluid as breathable for this entity.<br>
		 * Note that NULL is treated as AIR.
		 */
		public void add(@Nullable TagKey<Fluid> fluid)
		{
			if(!breathables.contains(fluid))
				breathables.add(fluid);
		}
		
		public boolean includes(TagKey<Fluid> fluid) { return this.breathables.contains(fluid); }
		
		public List<TagKey<Fluid>> getFluids(){ return this.breathables; }
		
		public static class Add extends AbilityGetBreathableFluidEvent
		{
			public Add(LivingEntity entity)
			{
				super(entity);
			}
			
			public Add(LivingEntity entity, AbilityData abilitiesIn)
			{
				super(entity, abilitiesIn);
			}
		}
		
		public static class Remove extends AbilityGetBreathableFluidEvent
		{
			public Remove(LivingEntity entity)
			{
				super(entity);
			}
			
			public Remove(LivingEntity entity, AbilityData abilitiesIn)
			{
				super(entity, abilitiesIn);
			}
			
			public void remove(TagKey<Fluid> fluid) { this.breathables.remove(fluid); }
		}
	}
}
