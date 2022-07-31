package com.lying.variousoddities.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;

public class EntityAIAvoidPlayer extends AvoidEntityGoal<Player>
{
	final PathfinderMob mob;
	
	public EntityAIAvoidPlayer(PathfinderMob entityIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn)
	{
		super(entityIn, Player.class, avoidDistanceIn, farSpeedIn, nearSpeedIn);
		mob = entityIn;
	}
	
	public boolean canUse()
	{
		return mob.getTarget() == null && super.canUse();
	}
}
