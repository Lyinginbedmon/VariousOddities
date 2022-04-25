package com.lying.variousoddities.potion;

import com.lying.variousoddities.init.VOPotions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectType;

public class PotionSleep extends PotionVO
{
	public PotionSleep(int colorIn)
	{
		super("sleep", EffectType.HARMFUL, colorIn);
	}
	
    public boolean isReady(int duration, int amplifier){ return true; }
	
	public static boolean isSleeping(LivingEntity entity)
	{
		if(entity instanceof PlayerEntity)
			return ((PlayerEntity)entity).isSleeping() || hasSleepEffect(entity);
		
		return false;
	}
	
	public static boolean hasSleepEffect(LivingEntity theMob)
	{
		return theMob.getActivePotionEffect(VOPotions.SLEEP) != null && theMob.getActivePotionEffect(VOPotions.SLEEP).getDuration() > 0;
	}
}
