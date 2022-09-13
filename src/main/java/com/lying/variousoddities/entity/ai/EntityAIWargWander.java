package com.lying.variousoddities.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.phys.Vec3;

public class EntityAIWargWander extends WaterAvoidingRandomStrollGoal
{
	private final boolean idleWander;
	
	public EntityAIWargWander(PathfinderMob creature, double speedIn)
	{
		super(creature, speedIn);
		this.idleWander = true;
	}
	
	public boolean shouldExecute()
	{
		if(!this.forceTrigger)
		{
			if(idleWander && this.mob.getNoActionTime() >= 100)
				return false;
			
			if(this.mob.getRandom().nextInt(this.interval) != 0)
				return false;
		}
		
		Vec3 dest = this.getPosition();
		if(dest == null)
			return false;
		else
		{
			this.wantedX = dest.x;
			this.wantedY = dest.y;
			this.wantedZ = dest.z;
			this.forceTrigger = false;
			return true;
		}
	}
}
