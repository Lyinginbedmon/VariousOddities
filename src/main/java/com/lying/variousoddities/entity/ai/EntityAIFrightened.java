package com.lying.variousoddities.entity.ai;

import java.util.EnumSet;
import java.util.List;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.condition.Conditions;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class EntityAIFrightened extends Goal
{
	protected final Mob entity;
	private final double nearSpeed;
	protected LivingEntity avoidTarget;
	protected final float avoidDistance;
	protected Path path;
	protected final PathNavigation navigation;
	
	public EntityAIFrightened(Mob mobIn)
	{
		this(mobIn, 6F, 1.5D);
	}
	
	public EntityAIFrightened(Mob entityIn, float distance, double nearSpeedIn)
	{
		this.entity = entityIn;
		this.avoidDistance = distance;
		this.nearSpeed = nearSpeedIn;
		this.navigation = entityIn.getNavigation();
		this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE, Goal.Flag.TARGET));
	}
	
	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse()
	{
		LivingData data = LivingData.forEntity(entity);
		if(data == null)
			return false;
		
		List<LivingEntity> terrorisers = data.getMindControlled(Conditions.AFRAID, this.avoidDistance);
		terrorisers.removeIf((terroriser) -> { return terroriser.getDistance(entity) > avoidDistance || !entity.hasLineOfSight(terroriser); });
		if(terrorisers.isEmpty())
			return false;
		
		this.avoidTarget = null;
		double closest = Double.MAX_VALUE;
		for(LivingEntity terroriser : terrorisers)
		{
			double dist = terroriser.getDistanceSq(entity);
			if(dist < closest)
			{
				closest = dist;
				this.avoidTarget = terroriser;
			}
		}
		if(this.avoidTarget == null)
			return false;
		
		Vec3 randomPos = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.entity, 16, 7, this.avoidTarget.getPositionVec());
		if(randomPos == null)
			return false;
		else if(this.avoidTarget.getDistanceSq(randomPos.x, randomPos.y, randomPos.z) < this.avoidTarget.getDistanceSq(this.entity))
			return false;
		
		this.path = this.navigation.getPathToPos(randomPos.x, randomPos.y, randomPos.z, 0);
		return this.path != null;
	}
	
	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean canContinueToUse()
	{
		return !this.navigation.isDone();
	}
	
	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start()
	{
		this.navigation.moveTo(this.path, this.nearSpeed);
		this.entity.setTarget(null);
	}
	
	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void stop()
	{
		this.avoidTarget = null;
	}
	
	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick()
	{
		
	}
}
