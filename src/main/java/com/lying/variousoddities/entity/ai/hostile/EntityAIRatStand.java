package com.lying.variousoddities.entity.ai.hostile;

import java.util.EnumSet;

import com.lying.variousoddities.entity.AbstractRat;

import net.minecraft.entity.ai.goal.Goal;

public class EntityAIRatStand extends Goal
{
	private final AbstractRat theRat;
	
	public EntityAIRatStand(AbstractRat rat)
	{
		theRat = rat;
		setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
	}
	
	public boolean shouldExecute()
	{
		boolean clearHead = (theRat.getStandingHeight() > 1F ? theRat.world.isAirBlock(theRat.getPosition().up()) : true);
		return theRat.getStandTime() <= 0 && clearHead && theRat.getRNG().nextInt(100) == 0;
	}
	
	public boolean shouldContinueExecuting()
	{
		return false;
	}
	
	public void startExecuting()
	{
		theRat.startStanding(100 + theRat.getRNG().nextInt(100));
	}
}
