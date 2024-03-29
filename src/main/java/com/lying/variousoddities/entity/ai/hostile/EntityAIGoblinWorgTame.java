package com.lying.variousoddities.entity.ai.hostile;

import java.util.EnumSet;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.entity.passive.EntityWorg;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.init.VOMobEffects;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;

public class EntityAIGoblinWorgTame extends Goal
{
	private final Level theWorld;
	private final EntityGoblin theGoblin;
	private final PathNavigation theNavigator;
	
	private final Predicate<Wolf> searchPredicate = new Predicate<Wolf>()
	{
		public boolean apply(Wolf input)
		{
			return input.isAlive() && !input.isTame() && !input.isBaby() && input.getTarget() == null;
		}
	};
	private Wolf targetWolf;
	
	private State currentState = null;
	private int tamingTimer = Reference.Values.TICKS_PER_SECOND * 10;
	
	public EntityAIGoblinWorgTame(EntityGoblin goblinIn)
	{
		this.theGoblin = goblinIn;
		theWorld = goblinIn.getLevel();
		theNavigator = goblinIn.getNavigation();
		setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
	}
	
	public boolean canUse()
	{
		double minDist = Double.MAX_VALUE;
		for(Wolf wolf : theWorld.<Wolf>getEntitiesOfClass(Wolf.class, theGoblin.getBoundingBox().inflate(8D), searchPredicate))
		{
			double dist = wolf.distanceToSqr(theGoblin);
			if(dist < minDist)
			{
				targetWolf = wolf;
				minDist = dist;
			}
		}
		
		return targetWolf != null && theGoblin.getTarget() == null && theGoblin.getRandom().nextInt(100) == 0;
	}
	
	public boolean shouldContinueExecuting()
	{
		return currentState != null;
	}
	
	public void resetTask()
	{
		targetWolf = null;
		currentState = null;
		tamingTimer = Reference.Values.TICKS_PER_SECOND * 10;
	}
	
	public void startExecuting()
	{
		if(targetWolf.distanceTo(theGoblin) < 1D) currentState = State.TAMING;
		else currentState = State.MOVING;
	}
	
	public void tick()
	{
		if(!targetWolf.isAlive())
		{
			currentState = null;
			return;
		}
		else theGoblin.getLookControl().setLookAt(targetWolf, (float)(theGoblin.getMaxHeadXRot() + 20), (float)theGoblin.getMaxHeadYRot());
		
		switch(currentState)
		{
			case MOVING:
				if(targetWolf.distanceTo(theGoblin) >= 1D)
				{
					if(theNavigator.isDone())
					{
						theNavigator.moveTo(targetWolf, 1.0D);
						if(theNavigator.isDone())
							currentState = null;
					}
				}
				else currentState = State.TAMING;
				break;
			case TAMING:
				targetWolf.addEffect(new MobEffectInstance(VOMobEffects.ENTANGLED.get(), Reference.Values.TICKS_PER_SECOND * 5, 1, false, false));
				if(--tamingTimer <= 0)
				{
					// TODO Ensure goblin arm swing during worg taming
					theGoblin.swing(InteractionHand.MAIN_HAND, true);
					EntityWorg theWorg = VOEntities.WORG.get().create(theWorld);
					theWorg.setHealth(targetWolf.getHealth());
					theWorg.addEffect(new MobEffectInstance(VOMobEffects.ENTANGLED.get(), Reference.Values.TICKS_PER_SECOND * 5, 1, false, false));
					
					if(targetWolf.hasCustomName())
						theWorg.setCustomName(targetWolf.getCustomName());
					
					theWorg.absMoveTo(targetWolf.getX(), targetWolf.getY(), targetWolf.getZ(), targetWolf.getYRot(), targetWolf.getXRot());
					
					targetWolf.setRemoved(RemovalReason.DISCARDED);
					theWorld.addFreshEntity(theWorg);
					theWorld.broadcastEntityEvent(theWorg, (byte)7);
					
					currentState = null;
				}
				else if(tamingTimer%Reference.Values.TICKS_PER_SECOND == 0)
				{
					theGoblin.swing(InteractionHand.MAIN_HAND, true);
					theWorld.broadcastEntityEvent(targetWolf, (byte)6);
				}
				break;
			default: currentState = null; return;
		}
	}
	
	private enum State
	{
		MOVING,
		TAMING;
	}
}
