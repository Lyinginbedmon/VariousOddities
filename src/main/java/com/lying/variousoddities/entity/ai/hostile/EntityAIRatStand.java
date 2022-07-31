package com.lying.variousoddities.entity.ai.hostile;

import java.util.EnumSet;

import com.lying.variousoddities.entity.AbstractRat;

import net.minecraft.world.entity.ai.goal.Goal;

public class EntityAIRatStand extends Goal
{
	private final AbstractRat theRat;
	
	public EntityAIRatStand(AbstractRat rat)
	{
		theRat = rat;
		setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
	}
	
	public boolean canUse()
	{
		boolean clearHead = (theRat.getStandingHeight() > 1F ? theRat.level.isEmptyBlock(theRat.blockPosition().above()) : true);
		return theRat.getStandTime() <= 0 && clearHead && theRat.getRandom().nextInt(100) == 0;
	}
	
	public boolean canContinueToUse()
	{
		return false;
	}
	
	public void start()
	{
		theRat.startStanding(100 + theRat.getRandom().nextInt(100));
	}
}
