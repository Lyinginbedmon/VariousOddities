package com.lying.variousoddities.entity.ai.hostile;

import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.passive.EntityWorg;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

public class EntityAIGoblinWorgBreed extends Goal
{
	private final Mob theGoblin;
	private final Level theWorld;
	private final PathNavigation theNavigator;
	
	private final Predicate<EntityWorg> searchPredicate = new Predicate<EntityWorg>()
			{
				public boolean apply(EntityWorg input)
				{
					return input.isAlive() && input.getAge() == 0 && !input.isInLove() && !input.isTame() && (input.getTarget() == null || !input.getTarget().isAlive());
				}
			};
	private EntityWorg worgA, worgB;
	
	private int breedingTimer = Reference.Values.TICKS_PER_SECOND * 2;
	private State currentState = null;
	
	public EntityAIGoblinWorgBreed(Mob creatureIn)
	{
		theGoblin = creatureIn;
		theWorld = creatureIn.getLevel();
		theNavigator = creatureIn.getNavigation();
        setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
	}
	
	public boolean canUse()
	{
		List<EntityWorg> eligibleWorgs = theWorld.<EntityWorg>getEntitiesOfClass(EntityWorg.class, theGoblin.getBoundingBox().inflate(8), searchPredicate);
		if(eligibleWorgs.size() > 6) return false;
		
		// Find Worg A
		double minDist = Double.MAX_VALUE;
		for(EntityWorg worg : eligibleWorgs)
		{
			double distance = theGoblin.distanceToSqr(worg);
			Path path = theNavigator.createPath(worg, 10);
			if(distance < minDist && path != null)
			{
				worgA = worg;
				minDist = distance;
			}
		}
		eligibleWorgs.remove(worgA);
		
		if(worgA != null)
		{
			// Find Worg B
			minDist = Double.MAX_VALUE;
			for(EntityWorg worg : eligibleWorgs)
			{
				double distance = worgA.distanceToSqr(worg);
				Path path = worgA.getNavigation().createPath(worg, 10);
				if(distance < minDist && path != null)
				{
					worgB = worg;
					minDist = distance;
				}
			}
		}
		
		return worgA != null && worgB != null && theGoblin.getRandom().nextInt(500) == 0;
	}
	
	public boolean shouldContinueExecuting()
	{
		return currentState != null;
	}
	
	public void resetTask()
	{
		if(worgA != null) worgA.setOrderedToSit(false);
		if(worgB != null) worgB.setOrderedToSit(false);
		currentState = null;
		worgA = worgB = null;
	}
	
	public void startExecuting()
	{
		theGoblin.getLookControl().setLookAt(worgA, (float)(theGoblin.getMaxHeadXRot() + 20), (float)theGoblin.getMaxHeadYRot());
		currentState = State.MOVING_TO_A;
	}
	
	public void tick()
	{
		if(!worgA.isAlive() || !worgB.isAlive() || worgA == null || worgB == null)
		{
			currentState = null;
			return;
		}
		
		switch(currentState)
		{
			case MOVING_TO_A:
				theGoblin.getLookControl().setLookAt(worgA, (float)(theGoblin.getMaxHeadXRot() + 20), (float)theGoblin.getMaxHeadYRot());
				if(theGoblin.distanceTo(worgA) > 1.5D)
				{
					if(theNavigator.isDone())
					{
						theNavigator.moveTo(worgA, 1.0D);
						if(theNavigator.isDone())
							currentState = null;
					}
				}
				else
				{
					worgA.setOrderedToSit(true);
					theNavigator.stop();
					breedingTimer = Reference.Values.TICKS_PER_SECOND * 2;
					currentState = State.BREEDING_A;
				}
				break;
			case BREEDING_A:
				if(theGoblin.distanceTo(worgA) < 1.5D)
				{
					if(--breedingTimer <= 0)
					{
						theGoblin.swing(InteractionHand.MAIN_HAND);
						worgA.setInLove(null);
						worgA.setOrderedToSit(false);
						worgA.getNavigation().moveTo(worgB, 1D);
						currentState = State.MOVING_TO_B;
					}
				}
				else currentState = State.MOVING_TO_A;
				break;
			case MOVING_TO_B:
				theGoblin.getLookControl().setLookAt(worgB, (float)(theGoblin.getMaxHeadXRot() + 20), (float)theGoblin.getMaxHeadYRot());
				if(theGoblin.distanceTo(worgB) > 1.5D)
				{
					if(theNavigator.isDone())
					{
						theNavigator.moveTo(worgB, 1.0D);
						if(theNavigator.isDone())
							currentState = null;
					}
				}
				else
				{
					worgB.setOrderedToSit(true);
					theNavigator.stop();
					breedingTimer = Reference.Values.TICKS_PER_SECOND * 2;
					currentState = State.BREEDING_B;
				}
				break;
			case BREEDING_B:
				if(theGoblin.distanceTo(worgB) < 1.5D)
				{
					if(--breedingTimer <= 0)
					{
						theGoblin.swing(InteractionHand.MAIN_HAND);
						worgB.setInLove(null);
						worgB.setOrderedToSit(false);
						worgB.getNavigation().moveTo(worgA, 1D);
						currentState = null;
					}
				}
				else currentState = State.MOVING_TO_B;
				break;
			default:
				currentState = null;
				break;
		}
	}
	
	private enum State
	{
		MOVING_TO_A,
		BREEDING_A,
		MOVING_TO_B,
		BREEDING_B;
	}
}
