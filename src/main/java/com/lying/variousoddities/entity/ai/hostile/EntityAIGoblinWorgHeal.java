package com.lying.variousoddities.entity.ai.hostile;

import java.util.EnumSet;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.entity.passive.EntityWorg;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class EntityAIGoblinWorgHeal extends Goal
{
	private final EntityGoblin theGoblin;
	private final Level theWorld;
	private final PathNavigation theNavigator;
	
	private Predicate<EntityWorg> searchPredicate = new Predicate<EntityWorg>()
			{
				public boolean apply(EntityWorg input)
				{
					return !input.isTame() && input.getHealth() < input.getMaxHealth() && input.getTarget() == null;
				}
			};
	private EntityWorg theWorg;

	private int healTimer = Reference.Values.TICKS_PER_SECOND * 2;
	private State currentState = null;
	
	public EntityAIGoblinWorgHeal(EntityGoblin goblinIn)
	{
		theGoblin = goblinIn;
		theWorld = goblinIn.getLevel();
		theNavigator = goblinIn.getNavigation();
		setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	public boolean canUse()
	{
		if(theGoblin.getTarget() != null) return false;
		
		double minDist = Double.MAX_VALUE;
		for(EntityWorg worg : theWorld.getEntitiesOfClass(EntityWorg.class, theGoblin.getBoundingBox().inflate(8), searchPredicate))
		{
			double distance = worg.distanceToSqr(theGoblin);
			if(distance < minDist && theNavigator.createPath(worg, 10) != null)
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
		if(theWorg.distanceTo(theGoblin) > 1.5D) currentState = State.MOVING;
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
				if(theGoblin.distanceTo(theWorg) > 1.5D)
				{
					if(theNavigator.isDone())
					{
						theNavigator.moveTo(theWorg, 1.0D);
						if(theNavigator.isDone())
							currentState = null;
					}
				}
				else
				{
					theNavigator.stop();
					healTimer = Reference.Values.TICKS_PER_SECOND * 2;
					currentState = State.HEALING;
				}
				break;
			case HEALING:
				if(theGoblin.distanceTo(theWorg) <= 1.5D)
				{
					if(--healTimer <= 0)
					{
						theGoblin.swingArm(InteractionHand.MAIN_HAND);
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
