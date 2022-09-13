package com.lying.variousoddities.entity.ai.hostile;

import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.entity.passive.EntityWorg;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

public class EntityAIGoblinWorgFight extends Goal
{
	private final EntityGoblin theGoblin;
	private final Level theWorld;
	
	private final Predicate<EntityWorg> searchPredicate = new Predicate<EntityWorg>()
			{
				public boolean apply(EntityWorg input)
				{
					return input.isAlive() && input.getTarget() == null && !input.isBaby() && !input.isTame() && !input.isSleeping() && !input.isInLove();
				}
			};
	private EntityWorg worgA, worgB;
	
	public EntityAIGoblinWorgFight(EntityGoblin goblinIn)
	{
		theGoblin = goblinIn;
		theWorld = goblinIn.getLevel();
		setFlags(EnumSet.of(Flag.LOOK));
	}
	
	public boolean canUse()
	{
		if(theGoblin.getTarget() != null || !theGoblin.isViolent()) return false;
		
		List<EntityWorg> worgs = theWorld.getEntitiesOfClass(EntityWorg.class, theGoblin.getBoundingBox().inflate(8D, 1D, 8D), searchPredicate);
		if(worgs.isEmpty() || worgs.size() <= 2) return false;
		
		worgA = worgB = null;
		for(EntityWorg worg : worgs)
		{
			if(worgA == null) worgA = worg;
			else if(worgB == null && worg.getNavigation().createPath(worgA, 10) != null)
			{
				worgB = worg;
				break;
			}
		}
		
		return worgA != null && worgB != null && theGoblin.getRandom().nextInt(1000) == 0;
	}
	
	public void startExecuting()
	{
		theGoblin.getLookControl().setLookAt(theGoblin.getRandom().nextBoolean() ? worgA : worgB, (float)(theGoblin.getMaxHeadXRot() + 20), (float)theGoblin.getMaxHeadYRot());
		theGoblin.swing(InteractionHand.MAIN_HAND);
		worgA.setTarget(worgB);
		theWorld.broadcastEntityEvent(worgA, (byte)6);
		worgB.setTarget(worgA);
		theWorld.broadcastEntityEvent(worgB, (byte)6);
	}
}
