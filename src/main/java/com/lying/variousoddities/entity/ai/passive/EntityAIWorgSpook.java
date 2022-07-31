package com.lying.variousoddities.entity.ai.passive;

import com.lying.variousoddities.entity.passive.EntityWorg;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.PanicGoal;

public class EntityAIWorgSpook extends PanicGoal
{
	private final EntityWorg creature;
	
	public EntityAIWorgSpook(EntityWorg entity, double speed)
	{
		super(entity, speed);
		this.creature = entity;
	}
	
	public boolean canUse()
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
	
	public void stop()
	{
		super.stop();
		if(this.creature.getRandom().nextInt(3) == 0)
			this.creature.unSpook();
	}
	
	public void start()
	{
		super.start();
		if(this.creature.isOrderedToSit())
			this.creature.setOrderedToSit(false);
		makeWhine();
	}
	
	public void tick()
	{
		if(this.creature.getRandom().nextInt(100) == 0)
			makeWhine();
	}
	
	public void makeWhine()
	{
    	this.creature.getLevel().playSound(null, this.creature.blockPosition(), SoundEvents.WOLF_HURT, this.creature.getSoundSource(), 0.4F, 1.0F);
	}
}
