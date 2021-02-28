package com.lying.variousoddities.types;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;

public class TypeHandlerAquatic extends TypeHandler
{
	public EnumDamageResist getDamageResist(DamageSource source)
	{
		return source == DamageSource.DROWN ? EnumDamageResist.IMMUNE : EnumDamageResist.NORMAL;
	}
	
	public void onMobUpdateEvent(LivingEntity living)
	{
		if(living.isWet())
			living.addPotionEffect(new EffectInstance(Effects.CONDUIT_POWER, 260, 0, true, false));
	}
}
