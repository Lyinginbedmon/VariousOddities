package com.lying.variousoddities.entity.ai.hostile;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.hostile.EntityGoblin;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class EntityAIGoblinFlee extends Goal
{
	private final World theWorld;
	private final EntityGoblin theGoblin;
	private final PathNavigator theNavigator;
	
	private MobEntity toAvoid;
	private final double fleeSpeed;
	
	public EntityAIGoblinFlee(EntityGoblin goblinIn, double speedIn)
	{
		theGoblin = goblinIn;
		theNavigator = goblinIn.getNavigator();
		theWorld = goblinIn.getEntityWorld();
		fleeSpeed = speedIn;
	}
	
	public boolean shouldExecute()
	{
		toAvoid = getNearestAvoid();
		return theGoblin.isChild() && toAvoid != null && toAvoid.getDistance(theGoblin) < 5D;
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
	
	private MobEntity getNearestAvoid()
	{
		MobEntity avoid = null;
		double minDist = Double.MAX_VALUE;
		for(MobEntity living : theWorld.getEntitiesWithinAABB(MobEntity.class, theGoblin.getBoundingBox().grow(6), new Predicate<MobEntity>()
			{
				public boolean apply(MobEntity input)
				{
					return input != theGoblin && input.getAttackTarget() != null && input.getAttackTarget() instanceof EntityGoblin;
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
	private Path getPathAwayFrom(MobEntity fleeTarget)
	{
        Vector3d vec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(theGoblin, 16, 7, new Vector3d(fleeTarget.getPosX(), fleeTarget.getPosY(), fleeTarget.getPosZ()));
        if(vec == null) return null;
        else if(fleeTarget.getDistanceSq(vec.x, vec.y, vec.z) < fleeTarget.getDistanceSq(theGoblin))
        	return null;
        else
        	return theNavigator.getPathToPos(vec.x, vec.y, vec.z, (int)theGoblin.getAttributeValue(Attributes.FOLLOW_RANGE));
	}
}
