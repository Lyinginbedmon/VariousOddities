package com.lying.variousoddities.entity.ai.hostile;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.AbstractGoblinWolf;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;

public class EntityAIWorgFollowGoblin extends Goal
{
	private final AbstractGoblinWolf theWorg;
	private final Level theWorld;
	private final PathNavigation theNavigator;
	
	private Predicate<EntityGoblin> searchPredicate = new Predicate<EntityGoblin>()
			{
				public boolean apply(EntityGoblin input)
				{
					return input.isAlive() && !input.isBaby();
				}
			};
	private EntityGoblin nearestGoblin = null;
	
	public EntityAIWorgFollowGoblin(AbstractGoblinWolf worgIn)
	{
		theWorg = worgIn;
		theWorld = worgIn.getLevel();
		theNavigator = worgIn.getNavigation();
	}
	
	public boolean canUse()
	{
		if(theWorg.isTame() || theWorg.getTarget() != null && theWorg.getTarget().isAlive() || theWorg.isOrderedToSit() || theWorg.getControllingPassenger() != null)
			return false;
		
		double minDist = Double.MAX_VALUE;
		for(EntityGoblin goblin : theWorld.getEntitiesOfClass(EntityGoblin.class, theWorg.getBoundingBox().inflate(16D), searchPredicate))
		{
			double dist = goblin.distanceToSqr(theWorg) / goblin.getGoblinType().authority;
			if(dist < minDist && theNavigator.createPath(goblin, (int)dist + 1) != null)
			{
				nearestGoblin = goblin;
				minDist = dist;
			}
		}
		if(nearestGoblin == null || !nearestGoblin.isAlive()) return false;
		
		return nearestGoblin.distanceTo(theWorg) > (theWorg.isBaby() ? 3D : 6D);
	}
	
	public void resetTask()
	{
		nearestGoblin = null;
	}
	
	public void startExecuting()
	{
		theWorg.getLookControl().setLookAt(nearestGoblin, (float)(theWorg.getMaxHeadXRot() + 20), (float)theWorg.getMaxHeadYRot());
		theWorg.setGoblinSight(Reference.Values.TICKS_PER_DAY);
		theNavigator.moveTo(nearestGoblin, 1.0D);
	}
}
