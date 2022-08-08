package com.lying.variousoddities.entity.ai.hostile;

import java.util.EnumSet;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.entity.passive.EntityWorg;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.Level;

public class EntityAIGoblinWorgHurt extends Goal
{
	private final EntityGoblin theGoblin;
	private final Level theWorld;
	
	private Predicate<EntityWorg> searchPredicate = new Predicate<EntityWorg>()
			{
				public boolean apply(EntityWorg input)
				{
					return !input.isBaby() && input.getTarget() == null && !input.isTame();
				}
			};
	private EntityWorg targetWorg = null;
	
	public EntityAIGoblinWorgHurt(EntityGoblin goblinIn)
	{
		theGoblin = goblinIn;
		theWorld = goblinIn.getLevel();
		
		setFlags(EnumSet.of(Flag.LOOK));
	}
	
	public boolean canUse()
	{
		if(theGoblin.isBaby() || !theGoblin.isViolent() || theGoblin.getTarget() != null) return false;
		
		targetWorg = null;
		for(EntityWorg worg : theWorld.getEntitiesOfClass(EntityWorg.class, theGoblin.getBoundingBox().inflate(1D, 0D, 1D), searchPredicate))
		{
			targetWorg = worg;
			break;
		}
		
		return targetWorg != null && theGoblin.getRandom().nextInt(100) == 0;
	}
	
	public void startExecuting()
	{
		theGoblin.getLookController().setLookPositionWithEntity(targetWorg, (float)(theGoblin.getHorizontalFaceSpeed() + 20), (float)theGoblin.getVerticalFaceSpeed());
		theGoblin.swingArm(InteractionHand.OFF_HAND);
		targetWorg.attackEntityFrom(DamageSource.mobAttack(theGoblin), 0F);
		
		for(EntityGoblin child : theWorld.getEntitiesOfClass(EntityGoblin.class, targetWorg.getBoundingBox().inflate(12, 2, 12), new Predicate<EntityGoblin>()
			{
				public boolean apply(EntityGoblin input){ return input.isBaby() && input.canEntityBeSeen(targetWorg) && input.canEntityBeSeen(theGoblin); }
			}))
			child.setViolent(true);
	}
}
