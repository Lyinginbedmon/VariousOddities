package com.lying.variousoddities.entity.ai.hostile;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;

public class EntityAIRatAvoid<T extends LivingEntity> extends AvoidEntityGoal<T>
{
	private final CreatureEntity theRat;
	
	public EntityAIRatAvoid(CreatureEntity entityIn, Class<T> classToAvoidIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn)
	{
		super(entityIn, classToAvoidIn, avoidDistanceIn, farSpeedIn, nearSpeedIn);
		theRat = entityIn;
	}
	
	public boolean shouldExecute()
	{
		return theRat.getAttackTarget() == null && super.shouldExecute();
	}
}
