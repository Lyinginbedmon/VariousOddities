package com.lying.variousoddities.entity.ai;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.util.math.vector.Vector3d;

public class EntityAIWargWander extends WaterAvoidingRandomWalkingGoal
{
	private final boolean idleWander;
	
	public EntityAIWargWander(CreatureEntity creature, double speedIn)
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
			
			if(this.creature.getRNG().nextInt(this.executionChance) != 0)
				return false;
		}
		
		Vector3d dest = this.getPosition();
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
