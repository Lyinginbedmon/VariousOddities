package com.lying.variousoddities.entity.ai.hostile;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.hostile.EntityGoblin;

import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class EntityAIGoblinFlee extends Goal
{
	private final Level theWorld;
	private final EntityGoblin theGoblin;
	private final PathNavigation theNavigator;
	
	private Mob toAvoid;
	private final double fleeSpeed;
	
	public EntityAIGoblinFlee(EntityGoblin goblinIn, double speedIn)
	{
		theGoblin = goblinIn;
		theNavigator = goblinIn.getNavigation();
		theWorld = goblinIn.getLevel();
		fleeSpeed = speedIn;
	}
	
	public boolean canUse()
	{
		toAvoid = getNearestAvoid();
		return theGoblin.isBaby() && toAvoid != null && toAvoid.getDistance(theGoblin) < 5D;
	}
	
	public boolean shouldContinueExecuting()
	{
		return !theNavigator.noPath();
	}
	
	public void resetTask()
	{
		toAvoid = null;
	}
	
	public void startExecuting()
	{
		Path thePath = getPathAwayFrom(toAvoid);
		if(thePath != null)
		{
			theNavigator.clearPath();
			theNavigator.setPath(thePath, fleeSpeed);
		}
	}
	
	private Mob getNearestAvoid()
	{
		Mob avoid = null;
		double minDist = Double.MAX_VALUE;
		for(Mob living : theWorld.getEntitiesOfClass(Mob.class, theGoblin.getBoundingBox().inflate(6), new Predicate<Mob>()
			{
				public boolean apply(Mob input)
				{
					return input != theGoblin && input.getTarget() != null && input.getTarget() instanceof EntityGoblin;
				}
			}))
		{
			double distance = living.getDistanceSq(theGoblin);
			if(getPathAwayFrom(living) != null && distance < minDist)
			{
				minDist = distance;
				avoid = living;
			}
		}
		return avoid;
	}
	
	/** Returns a path to a random position that isn't closer to the flee target */
	private Path getPathAwayFrom(Mob fleeTarget)
	{
        Vec3 vec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(theGoblin, 16, 7, new Vec3(fleeTarget.getPosX(), fleeTarget.getPosY(), fleeTarget.getPosZ()));
        if(vec == null) return null;
        else if(fleeTarget.getDistanceSq(vec.x, vec.y, vec.z) < fleeTarget.getDistanceSq(theGoblin))
        	return null;
        else
        	return theNavigator.getPathToPos(vec.x, vec.y, vec.z, (int)theGoblin.getAttributeValue(Attributes.FOLLOW_RANGE));
	}
}
