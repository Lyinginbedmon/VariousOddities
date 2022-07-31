package com.lying.variousoddities.entity.ai.hostile;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;

public class EntityAIRatAvoid<T extends LivingEntity> extends AvoidEntityGoal<T>
{
	private final PathfinderMob theRat;
	
	public EntityAIRatAvoid(PathfinderMob entityIn, Class<T> classToAvoidIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn)
	{
		super(entityIn, classToAvoidIn, avoidDistanceIn, farSpeedIn, nearSpeedIn);
		theRat = entityIn;
	}
	
	public boolean canUse()
	{
		return theRat.getTarget() == null && super.canUse();
	}
}
