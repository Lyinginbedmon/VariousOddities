package com.lying.variousoddities.entity.ai.hostile;

import java.util.EnumSet;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.entity.passive.EntityWorg;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EntityAIGoblinWorgHeal extends Goal
{
	private final EntityGoblin theGoblin;
	private final World theWorld;
	private final PathNavigator theNavigator;
	
	private Predicate<EntityWorg> searchPredicate = new Predicate<EntityWorg>()
			{
				public boolean apply(EntityWorg input)
				{
					return !input.isTamed() && input.getHealth() < input.getMaxHealth() && input.getAttackTarget() == null;
				}
			};
	private EntityWorg theWorg;

	private int healTimer = Reference.Values.TICKS_PER_SECOND * 2;
	private State currentState = null;
	
	public EntityAIGoblinWorgHeal(EntityGoblin goblinIn)
	{
		theGoblin = goblinIn;
		theWorld = goblinIn.getEntityWorld();
		theNavigator = goblinIn.getNavigator();
		setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	public boolean shouldExecute()
	{
		if(theGoblin.getAttackTarget() != null) return false;
		
		double minDist = Double.MAX_VALUE;
		for(EntityWorg worg : theWorld.getEntitiesWithinAABB(EntityWorg.class, theGoblin.getBoundingBox().grow(8), searchPredicate))
		{
			double distance = worg.getDistanceSq(theGoblin);
			if(distance < minDist && theNavigator.getPathToEntity(worg, 10) != null)
			{
				minDist = distance;
				theWorg = worg;
			}
		}
		return theWorg != null;
	}
	
	public boolean shouldContinueExecuting()
	{
		return currentState != null;
	}
	
	public void resetTask()
	{
		theWorg = null;
		healTimer = Reference.Values.TICKS_PER_SECOND * 2;
		currentState = null;
	}
	
	public void startExecuting()
	{
		if(theWorg.getDistance(theGoblin) > 1.5D) currentState = State.MOVING;
		else currentState = State.HEALING;
	}
	
	public void tick()
	{
		if(!theWorg.isAlive() || theWorg.getHealth() >= theWorg.getMaxHealth())
		{
			currentState = null;
			return;
		}
		theGoblin.getLookController().setLookPositionWithEntity(theWorg, (float)(theGoblin.getHorizontalFaceSpeed() + 20), (float)theGoblin.getVerticalFaceSpeed());
		
		switch(currentState)
		{
			case MOVING:
				if(theGoblin.getDistance(theWorg) > 1.5D)
				{
					if(theNavigator.noPath())
					{
						theNavigator.tryMoveToEntityLiving(theWorg, 1.0D);
						if(theNavigator.noPath())
							currentState = null;
					}
				}
				else
				{
					theNavigator.clearPath();
					healTimer = Reference.Values.TICKS_PER_SECOND * 2;
					currentState = State.HEALING;
				}
				break;
			case HEALING:
				if(theGoblin.getDistance(theWorg) <= 1.5D)
				{
					if(--healTimer <= 0)
					{
						theGoblin.swingArm(Hand.MAIN_HAND);
						theWorg.heal(Items.ROTTEN_FLESH.getFood().getHealing());
						theWorld.setEntityState(theWorg, (byte)7);
						currentState = null;
					}
				}
				else
					currentState = State.MOVING;
				break;
			default:
				currentState = null;
				break;
		}
	}
	
	private enum State
	{
		MOVING,
		HEALING;
	}
}
