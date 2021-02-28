package com.lying.variousoddities.entity.ai.hostile;

import java.util.EnumSet;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.hostile.EntityRatGiant;
import com.lying.variousoddities.entity.passive.EntityRat;

import net.minecraft.entity.ai.goal.Goal;

public class EntityAIRatFollowGiant extends Goal
{
	private final EntityRat theRat;
	private EntityRatGiant theGiantRat;
	
	private static final double DISTANCE_MIN = 9.0D;
	private static final double DISTANCE_MAX = 256.0D;
	
	private final double moveSpeed;
	private int delayTimer = 0;
	
	public EntityAIRatFollowGiant(EntityRat ratIn, double par2Speed)
	{
		theRat = ratIn;
		moveSpeed = par2Speed;
		setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
	}
	
	public boolean shouldExecute()
	{
		if(theRat.getAttackTarget() != null)
			return false;
		
	    double distanceMin = DISTANCE_MAX;
	    for (EntityRatGiant giantRat : this.theRat.world.<EntityRatGiant>getEntitiesWithinAABB(EntityRatGiant.class, theRat.getBoundingBox().grow(8.0D, 4.0D, 8.0D), new Predicate<EntityRatGiant>()
	    {
	    	public boolean apply(EntityRatGiant input)
	    	{
	    		return input.getRatBreed() == theRat.getRatBreed();
	    	}
	    }))
	    {
            double distance = this.theRat.getDistanceSq(giantRat);
            if(distance <= distanceMin)
            {
                distanceMin = distance;
                this.theGiantRat = giantRat;
            }
	    }
	    
	    return !(this.theGiantRat == null || distanceMin < 9.0D);
	}
	
	public boolean shouldContinueExecuting()
	{
        double distance = this.theRat.getDistanceSq(this.theGiantRat);
        return this.theGiantRat.isAlive() && distance >= DISTANCE_MIN && distance <= DISTANCE_MAX;
	}
	
	public void resetTask()
	{
		this.theGiantRat = null;
	}
	
	public void startExecuting()
	{
		this.delayTimer = 0;
	}
	
    public void tick()
    {
        if(--this.delayTimer <= 0)
        {
            this.delayTimer = 10;
            this.theRat.getNavigator().tryMoveToEntityLiving(this.theGiantRat, this.moveSpeed);
        }
    }
}
