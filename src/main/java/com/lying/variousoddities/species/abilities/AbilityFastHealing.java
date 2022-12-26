package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityFastHealing extends Ability
{
	private int ticksSinceHeal = 0;
	
	private float rate = 2F;
	
	public AbilityFastHealing(float rate)
	{
		super();
		this.rate = rate;
	}
	
	public int compare(Ability abilityIn)
	{
		AbilityFastHealing healing = (AbilityFastHealing)abilityIn;
		return healing.rate < rate ? -1 : healing.rate > rate ? 1 : 0;
	}
	
	public Component translatedName(){ return Component.translatable("ability."+Reference.ModInfo.MOD_ID+".fast_healing", (int)rate); }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Type.DEFENSE; }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.putFloat("Rate", this.rate);
		compound.putInt("Ticks", this.ticksSinceHeal);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		this.rate = compound.getFloat("Rate");
		this.ticksSinceHeal = compound.getInt("Ticks");
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::onLivingUpdate);
	}
	
	public void onLivingUpdate(LivingTickEvent event)
	{
		// Increase tick time if damage is > 0, then heal and reset tick time
		LivingEntity entity = event.getEntity();
		AbilityFastHealing ability = (AbilityFastHealing)AbilityRegistry.getAbilityByMapName(entity, getMapName());
		if(ability != null)
		{
			LivingData data = LivingData.getCapability(entity);
			if((entity.getHealth() < entity.getMaxHealth() || data.getBludgeoning() > 0F) && entity.isAlive())
			{
				if(++ability.ticksSinceHeal >= Reference.Values.TICKS_PER_SECOND * 6)
				{
					if(data.getBludgeoning() > 0F)
						data.addBludgeoning(-ability.rate);
					else
						entity.heal(ability.rate);
					ability.ticksSinceHeal = 0;
				}
			}
			else
				ability.ticksSinceHeal = 0;
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			float rate = compound.contains("Rate", 5) ? compound.getFloat("Rate") : 2;
			return new AbilityFastHealing(rate);
		}
	}
}
