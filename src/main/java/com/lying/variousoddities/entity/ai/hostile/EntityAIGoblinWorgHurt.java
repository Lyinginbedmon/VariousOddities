package com.lying.variousoddities.entity.ai.hostile;

import java.util.EnumSet;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.entity.passive.EntityWorg;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EntityAIGoblinWorgHurt extends Goal
{
	private final EntityGoblin theGoblin;
	private final World theWorld;
	
	private Predicate<EntityWorg> searchPredicate = new Predicate<EntityWorg>()
			{
				public boolean apply(EntityWorg input)
				{
					return !input.isChild() && input.getAttackTarget() == null && !input.isTamed();
				}
			};
	private EntityWorg targetWorg = null;
	
	public EntityAIGoblinWorgHurt(EntityGoblin goblinIn)
	{
		theGoblin = goblinIn;
		theWorld = goblinIn.getEntityWorld();
		
		setMutexFlags(EnumSet.of(Flag.LOOK));
	}
	
	public boolean shouldExecute()
	{
		if(theGoblin.isChild() || !theGoblin.isViolent() || theGoblin.getAttackTarget() != null) return false;
		
		targetWorg = null;
		for(EntityWorg worg : theWorld.getEntitiesWithinAABB(EntityWorg.class, theGoblin.getBoundingBox().grow(1D, 0D, 1D), searchPredicate))
		{
			targetWorg = worg;
			break;
		}
		
		return targetWorg != null && theGoblin.getRNG().nextInt(100) == 0;
	}
	
	public void startExecuting()
	{
		theGoblin.getLookController().setLookPositionWithEntity(targetWorg, (float)(theGoblin.getHorizontalFaceSpeed() + 20), (float)theGoblin.getVerticalFaceSpeed());
		theGoblin.swingArm(Hand.OFF_HAND);
		targetWorg.attackEntityFrom(DamageSource.causeMobDamage(theGoblin), 0F);
		
		for(EntityGoblin child : theWorld.getEntitiesWithinAABB(EntityGoblin.class, targetWorg.getBoundingBox().grow(12, 2, 12), new Predicate<EntityGoblin>()
			{
				public boolean apply(EntityGoblin input){ return input.isChild() && input.canEntityBeSeen(targetWorg) && input.canEntityBeSeen(theGoblin); }
			}))
			child.setViolent(true);
	}
}
