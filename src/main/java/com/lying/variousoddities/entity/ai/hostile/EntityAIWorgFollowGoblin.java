package com.lying.variousoddities.entity.ai.hostile;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.AbstractGoblinWolf;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.world.World;

public class EntityAIWorgFollowGoblin extends Goal
{
	private final AbstractGoblinWolf theWorg;
	private final World theWorld;
	private final PathNavigator theNavigator;
	
	private Predicate<EntityGoblin> searchPredicate = new Predicate<EntityGoblin>()
			{
				public boolean apply(EntityGoblin input)
				{
					return input.isAlive() && !input.isChild();
				}
			};
	private EntityGoblin nearestGoblin = null;
	
	public EntityAIWorgFollowGoblin(AbstractGoblinWolf worgIn)
	{
		theWorg = worgIn;
		theWorld = worgIn.getEntityWorld();
		theNavigator = worgIn.getNavigator();
	}
	
	public boolean shouldExecute()
	{
		if(theWorg.isTamed() || theWorg.getAttackTarget() != null && theWorg.getAttackTarget().isAlive() || theWorg.isSitting() || theWorg.getControllingPassenger() != null)
			return false;
		
		double minDist = Double.MAX_VALUE;
		for(EntityGoblin goblin : theWorld.getEntitiesWithinAABB(EntityGoblin.class, theWorg.getBoundingBox().grow(16D), searchPredicate))
		{
			double dist = goblin.getDistanceSq(theWorg) / goblin.getGoblinType().authority;
			if(dist < minDist && theWorg.getNavigator().getPathToEntity(goblin, (int)dist + 1) != null)
			{
				nearestGoblin = goblin;
				minDist = dist;
			}
		}
		if(nearestGoblin == null || !nearestGoblin.isAlive()) return false;
		
		return nearestGoblin.getDistance(theWorg) > (theWorg.isChild() ? 3D : 6D);
	}
	
	public void resetTask()
	{
		nearestGoblin = null;
	}
	
	public void startExecuting()
	{
		theWorg.getLookController().setLookPositionWithEntity(nearestGoblin, (float)(theWorg.getHorizontalFaceSpeed() + 20), (float)theWorg.getVerticalFaceSpeed());
		theWorg.setGoblinSight(Reference.Values.TICKS_PER_DAY);
		theNavigator.tryMoveToEntityLiving(nearestGoblin, 1.0D);
	}
}
