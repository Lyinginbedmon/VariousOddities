package com.lying.variousoddities.potion;

import com.lying.variousoddities.init.VOPotions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.vector.Vector3d;

public class PotionSleep extends PotionVO
{
	public PotionSleep(int colorIn)
	{
		super("sleep", EffectType.HARMFUL, colorIn);
	}
	
    public boolean isReady(int duration, int amplifier){ return true; }
    
    public void performEffect(LivingEntity livingEntity, int amplifier)
    {
    	if(livingEntity.isPotionActive(VOPotions.SLEEP) && livingEntity.getActivePotionEffect(VOPotions.SLEEP).getDuration() > 0)
    	{
			if(livingEntity instanceof PlayerEntity)
			{
				PlayerEntity player = (PlayerEntity)livingEntity;
				if(!player.canUseCommandBlock())
				{
	//				player.openGui(VariousOddities.instance, Reference.GUI.GUI_PETRIFIED, player.getEntityWorld(), 0, 0, 0);
//					if(player.getForcedPose() != Pose.SLEEPING)
//						player.setForcedPose(Pose.SLEEPING);
					player.setMotion(Vector3d.ZERO);
				}
			}
			else
				setSleeping(livingEntity, true);
    	}
    }
    
    public void removeAttributesModifiersFromEntity(LivingEntity entityLivingBaseIn, AttributeModifierManager attributeMapIn, int amplifier)
    {
    	super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
    	
//    	if(entityLivingBaseIn instanceof PlayerEntity)
//			((PlayerEntity)entityLivingBaseIn).setForcedPose(null);
    	entityLivingBaseIn.setPose(Pose.STANDING);
    }
	
	public static void setSleeping(LivingEntity entity, boolean sleep)
	{
		entity.setPose(sleep ? Pose.SLEEPING : Pose.STANDING);
//		if(entity instanceof EntityOddity)
//			entity.getDataManager().set(EntityOddity.SLEEPING, sleep);
		
//		for(EntityAITaskEntry task : entity.tasks.taskEntries)
//			if(task.action instanceof EntityAISleep)
//			{
//				((EntityAISleep)task.action).setSleeping(sleep);
//				return;
//			}
	}
	
	public static boolean isSleeping(LivingEntity entity)
	{
//		if(entity instanceof LivingEntity)
//		{
//			for(EntityAITaskEntry task : ((LivingEntity)entity).tasks.taskEntries)
//				if(task.action instanceof EntityAISleep)
//					return ((EntityAISleep)task.action).sleepState();
//		}
//		else 
		if(entity instanceof PlayerEntity)
			return ((PlayerEntity)entity).isSleeping() || entity.getActivePotionEffect(VOPotions.SLEEP) != null && entity.getActivePotionEffect(VOPotions.SLEEP).getDuration() > 0;
		
		return false;
	}
	
	public static boolean hasSleepEffect(LivingEntity theMob)
	{
		return theMob.getActivePotionEffect(VOPotions.SLEEP) != null && theMob.getActivePotionEffect(VOPotions.SLEEP).getDuration() > 0;
	}
}
