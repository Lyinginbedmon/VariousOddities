package com.lying.variousoddities.entity.ai.hostile;

import java.util.EnumSet;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.hostile.EntityRatGiant;
import com.lying.variousoddities.entity.passive.EntityRat;

import net.minecraft.world.entity.ai.goal.Goal;

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
		setFlags(EnumSet.of(Goal.Flag.MOVE));
	}
	
	public boolean canUse()
	{
		if(theRat.getTarget() != null)
			return false;
		
	    double distanceMin = DISTANCE_MAX;
	    for (EntityRatGiant giantRat : this.theRat.level.<EntityRatGiant>getEntitiesOfClass(EntityRatGiant.class, theRat.getBoundingBox().inflate(8.0D, 4.0D, 8.0D), new Predicate<EntityRatGiant>()
	    {
	    	public boolean apply(EntityRatGiant input)
	    	{
	    		return input.getRatBreed() == theRat.getRatBreed();
	    	}
	    }))
	    {
            double distance = this.theRat.distanceTo(giantRat);
            if(distance <= distanceMin)
            {
                distanceMin = distance;
                this.theGiantRat = giantRat;
            }
	    }
	    
	    return !(this.theGiantRat == null || distanceMin < 9.0D);
	}
	
	public boolean canContinueToUse()
	{
        double distance = this.theRat.distanceTo(this.theGiantRat);
        return this.theGiantRat.isAlive() && distance >= DISTANCE_MIN && distance <= DISTANCE_MAX;
	}
	
	public void stop()
	{
		this.theGiantRat = null;
	}
	
	public void start()
	{
		this.delayTimer = 0;
	}
	
    public void tick()
    {
        if(--this.delayTimer <= 0)
        {
            this.delayTimer = 10;
            this.theRat.getNavigation().moveTo(this.theGiantRat, this.moveSpeed);
        }
    }
}
