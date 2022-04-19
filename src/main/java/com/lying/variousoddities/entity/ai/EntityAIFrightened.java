package com.lying.variousoddities.entity.ai;

import java.util.EnumSet;

import com.lying.variousoddities.capabilities.LivingData;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.EntityPredicates;

public class EntityAIFrightened extends AvoidEntityGoal<LivingEntity>
{
	public EntityAIFrightened(CreatureEntity mobIn)
	{
		super(mobIn, LivingEntity.class, (entity) -> { return LivingData.forEntity(mobIn).isAfraidOf(entity); }, 6F, 1D, 1.2D, EntityPredicates.CAN_AI_TARGET::test);
		this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
	}
}
