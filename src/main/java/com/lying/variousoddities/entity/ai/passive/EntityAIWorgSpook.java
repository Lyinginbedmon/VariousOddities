package com.lying.variousoddities.entity.ai.passive;

import com.lying.variousoddities.entity.passive.EntityWorg;

import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.util.SoundEvents;

public class EntityAIWorgSpook extends PanicGoal
{
	private final EntityWorg creature;
	
	public EntityAIWorgSpook(EntityWorg entity, double speed)
	{
		super(entity, speed);
		this.creature = entity;
	}
	
	public boolean shouldExecute()
	{
		if(this.creature.isSpooked())
		{
			if(!creature.getGenetics().gene(5))
				this.creature.unSpook();
			else
				return findRandomPosition();
		}
		return false;
	}
	
	public void resetTask()
	{
		super.resetTask();
		if(this.creature.getRNG().nextInt(3) == 0)
			this.creature.unSpook();
	}
	
	public void startExecuting()
	{
		super.startExecuting();
		if(this.creature.isSitting())
			this.creature.func_233687_w_(false);
		makeWhine();
	}
	
	public void tick()
	{
		if(this.creature.getRNG().nextInt(100) == 0)
			makeWhine();
	}
	
	public void makeWhine()
	{
    	this.creature.getEntityWorld().playSound(null, this.creature.getPosition(), SoundEvents.ENTITY_WOLF_HURT, this.creature.getSoundCategory(), 0.4F, 1.0F);
	}
}
