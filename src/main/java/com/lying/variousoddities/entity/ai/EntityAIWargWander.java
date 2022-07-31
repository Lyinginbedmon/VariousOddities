package com.lying.variousoddities.entity.ai;

import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class EntityAIWargWander extends WaterAvoidingRandomWalkingGoal
{
	private final boolean idleWander;
	
	public EntityAIWargWander(PathfinderMob creature, double speedIn)
	{
		super(creature, speedIn);
		this.idleWander = true;
	}
	
	public boolean shouldExecute()
	{
		if(!this.mustUpdate)
		{
			if(idleWander && this.creature.getIdleTime() >= 100)
				return false;
			
			if(this.creature.getRandom().nextInt(this.executionChance) != 0)
				return false;
		}
		
		Vec3 dest = this.position();
		if(dest == null)
			return false;
		else
		{
			this.x = dest.x;
			this.y = dest.y;
			this.z = dest.z;
			this.mustUpdate = false;
			return true;
		}
	}
}
