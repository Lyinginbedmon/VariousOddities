package com.lying.variousoddities.entity.ai.hostile;

import java.util.EnumSet;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.entity.passive.EntityWorg;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EntityAIGoblinWorgTame extends Goal
{
	private final World theWorld;
	private final EntityGoblin theGoblin;
	private final PathNavigator theNavigator;
	
	private final Predicate<WolfEntity> searchPredicate = new Predicate<WolfEntity>()
	{
		public boolean apply(WolfEntity input)
		{
			return input.isAlive() && !input.isTamed() && !input.isChild() && input.getAttackTarget() == null;
		}
	};
	private WolfEntity targetWolf;
	
	private State currentState = null;
	private int tamingTimer = Reference.Values.TICKS_PER_SECOND * 10;
	
	public EntityAIGoblinWorgTame(EntityGoblin goblinIn)
	{
		this.theGoblin = goblinIn;
		theWorld = goblinIn.getEntityWorld();
		theNavigator = goblinIn.getNavigator();
		setMutexFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
	}
	
	public boolean shouldExecute()
	{
		double minDist = Double.MAX_VALUE;
		for(WolfEntity wolf : theWorld.<WolfEntity>getEntitiesWithinAABB(WolfEntity.class, theGoblin.getBoundingBox().grow(8D), searchPredicate))
		{
			double dist = wolf.getDistanceSq(theGoblin);
			if(dist < minDist)
			{
				targetWolf = wolf;
				minDist = dist;
			}
		}
		
		return targetWolf != null && theGoblin.getAttackTarget() == null && theGoblin.getRNG().nextInt(100) == 0;
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
		if(targetWolf.getDistance(theGoblin) < 1D) currentState = State.TAMING;
		else currentState = State.MOVING;
	}
	
	public void tick()
	{
		if(!targetWolf.isAlive())
		{
			currentState = null;
			return;
		}
		else theGoblin.getLookController().setLookPositionWithEntity(targetWolf, (float)(theGoblin.getHorizontalFaceSpeed() + 20), (float)theGoblin.getVerticalFaceSpeed());
		
		switch(currentState)
		{
			case MOVING:
				if(targetWolf.getDistance(theGoblin) >= 1D)
				{
					if(theNavigator.noPath())
					{
						theNavigator.tryMoveToEntityLiving(targetWolf, 1.0D);
						if(theNavigator.noPath())
							currentState = null;
					}
				}
				else currentState = State.TAMING;
				break;
			case TAMING:
//				targetWolf.addPotionEffect(new PotionEffect(VOPotions.ENTANGLED, Reference.Values.TICKS_PER_SECOND * 5, 1, false, false));
				targetWolf.addPotionEffect(new EffectInstance(Effects.SLOWNESS, Reference.Values.TICKS_PER_SECOND * 5, 10, false, false));
				if(--tamingTimer <= 0)
				{
					// TODO Ensure goblin arm swing during worg taming
					theGoblin.swing(Hand.MAIN_HAND, true);
					EntityWorg theWorg = VOEntities.WORG.create(theWorld);
					theWorg.setHealth(targetWolf.getHealth());
//					theWorg.addPotionEffect(new EffectInstance(VOPotions.ENTANGLED, Reference.Values.TICKS_PER_SECOND * 5, 1, false, false));
					theWorg.addPotionEffect(new EffectInstance(Effects.SLOWNESS, Reference.Values.TICKS_PER_SECOND * 5, 10, false, false));
					
					if(targetWolf.hasCustomName())
						theWorg.setCustomName(targetWolf.getCustomName());
					
					theWorg.setPositionAndRotation(targetWolf.getPosX(), targetWolf.getPosY(), targetWolf.getPosZ(), targetWolf.rotationYaw, targetWolf.rotationPitch);
					
					targetWolf.remove();
					theWorld.addEntity(theWorg);
					theWorld.setEntityState(theWorg, (byte)7);
					
					currentState = null;
				}
				else if(tamingTimer%Reference.Values.TICKS_PER_SECOND == 0)
				{
					theGoblin.swing(Hand.MAIN_HAND, true);
					theWorld.setEntityState(targetWolf, (byte)6);
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
