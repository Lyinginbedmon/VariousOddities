package com.lying.variousoddities.entity.ai;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.player.PlayerEntity;

public class EntityAIAvoidPlayer extends AvoidEntityGoal<PlayerEntity>
{
	final CreatureEntity mob;
	
	public EntityAIAvoidPlayer(CreatureEntity entityIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn)
	{
		super(entityIn, PlayerEntity.class, avoidDistanceIn, farSpeedIn, nearSpeedIn);
		mob = entityIn;
	}
	
	public boolean shouldExecute()
	{
		return mob.getAttackTarget() == null && super.shouldExecute();
	}
}
