package com.lying.variousoddities.types;

import java.util.Collection;
import java.util.EnumSet;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.types.EnumCreatureType.Action;
import com.lying.variousoddities.types.EnumCreatureType.ActionSet;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;

public class TypeHandlerAquatic extends TypeHandler
{
	private final boolean breathesAir;
	
	public TypeHandlerAquatic(boolean breatheAir)
	{
		this.breathesAir = breatheAir;
	}
	
	public EnumSet<Action> applyActions(EnumSet<Action> actions, Collection<EnumCreatureType> types)
	{
		if(new ActionSet(actions).breathes())
		{
			actions.add(Action.BREATHE_WATER);
			if(!breathesAir && !types.contains(EnumCreatureType.AMPHIBIOUS))
				actions.remove(Action.BREATHE_AIR);
		}
		return actions;
	}
	
	public void onMobUpdateEvent(LivingEntity living)
	{
		if(living.areEyesInFluid(FluidTags.WATER))
		{
			EffectInstance conduitPower = new EffectInstance(Effects.CONDUIT_POWER, 10 * Reference.Values.TICKS_PER_SECOND, 0, true, false);
			living.addPotionEffect(conduitPower);
		}
		else if(living.isPotionActive(Effects.CONDUIT_POWER))
		{
			EffectInstance instance = living.getActivePotionEffect(Effects.CONDUIT_POWER);
			if(instance.isAmbient() && !instance.doesShowParticles())
				living.removeActivePotionEffect(Effects.CONDUIT_POWER);
		}
	}
	
	public void onRemove(LivingEntity living)
	{
		if(living.isPotionActive(Effects.CONDUIT_POWER))
		{
			EffectInstance instance = living.getActivePotionEffect(Effects.CONDUIT_POWER);
			if(instance.isAmbient() && !instance.doesShowParticles())
				living.removeActivePotionEffect(Effects.CONDUIT_POWER);
		}
	}
}
