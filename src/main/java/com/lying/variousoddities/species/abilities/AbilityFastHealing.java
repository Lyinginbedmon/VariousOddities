package com.lying.variousoddities.species.abilities;

import com.google.common.base.Predicate;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityFastHealing extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "fast_healing");
	
	/** Returns true for any damage that isn't a form of starvation or suffocation */
	private static final Predicate<DamageSource> DAMAGE = new Predicate<DamageSource>()
			{
				public boolean apply(DamageSource input)
				{
					return !(input == DamageSource.STARVE || input == DamageSource.IN_WALL || input == DamageSource.DROWN);
				}
			};
	
	private int ticksSinceHeal = 0;
	private float damageToHeal = 0;
	
	private float rate = 2F;
	
	public AbilityFastHealing(float rate)
	{
		super(REGISTRY_NAME);
		this.rate = rate;
	}
	
	public ITextComponent translatedName(){ return new TranslationTextComponent("ability."+Reference.ModInfo.MOD_ID+".fast_healing", (int)rate); }
	
	public Type getType(){ return Type.DEFENSE; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.putFloat("Rate", this.rate);
		
		compound.putFloat("Damage", this.damageToHeal);
		compound.putInt("Ticks", this.ticksSinceHeal);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		this.rate = compound.getFloat("Rate");
		
		this.damageToHeal = compound.getFloat("Damage");
		this.ticksSinceHeal = compound.getInt("Ticks");
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::onLivingUpdate);
		bus.addListener(this::onLivingDamage);
	}
	
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		// Increase tick time if damage is > 0, then heal and reset tick time
		LivingEntity entity = event.getEntityLiving();
		if(AbilityRegistry.hasAbility(entity, getMapName()))
		{
			AbilityFastHealing ability = (AbilityFastHealing)AbilityRegistry.getAbilityByName(entity, getMapName());
			if(entity.getHealth() < entity.getMaxHealth() && entity.isAlive())
			{
				if(ability.damageToHeal > 0F)
				{
					if(++ability.ticksSinceHeal >= Reference.Values.TICKS_PER_SECOND * 6)
					{
						float amount = Math.min(ability.damageToHeal, ability.rate);
						entity.heal(amount);
						ability.damageToHeal -= amount;
						ability.ticksSinceHeal = 0;
					}
				}
				else if(ability.ticksSinceHeal > 0)
					ability.ticksSinceHeal = 0;
			}
			else
			{
				ability.ticksSinceHeal = 0;
				ability.damageToHeal = 0F;
			}
		}
	}
	
	public void onLivingDamage(LivingDamageEvent event)
	{
		// Increase the damage to heal Unless it's an invalid damage type
		LivingEntity entity = event.getEntityLiving();
		if(AbilityRegistry.hasAbility(entity, getMapName()) && DAMAGE.apply(event.getSource()))
		{
			AbilityFastHealing ability = (AbilityFastHealing)AbilityRegistry.getAbilityByName(entity, getMapName());
			ability.damageToHeal += event.getAmount();
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			float rate = compound.contains("Rate", 5) ? compound.getFloat("Rate") : 2;
			return new AbilityFastHealing(rate);
		}
	}
}
