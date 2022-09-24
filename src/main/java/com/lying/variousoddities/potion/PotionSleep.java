package com.lying.variousoddities.potion;

import com.lying.variousoddities.init.VOMobEffects;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class PotionSleep extends PotionVO
{
	public PotionSleep(int colorIn)
	{
		super(MobEffectCategory.HARMFUL, colorIn);
	}
	
    public boolean isReady(int duration, int amplifier){ return true; }
	
	public static boolean isSleeping(LivingEntity entity)
	{
		if(entity instanceof Player)
			return ((Player)entity).isSleeping() || hasSleepEffect(entity);
		
		return false;
	}
	
	public static boolean hasSleepEffect(LivingEntity theMob)
	{
		return theMob.getEffect(VOMobEffects.SLEEP) != null && theMob.getEffect(VOMobEffects.SLEEP).getDuration() > 0;
	}
}
