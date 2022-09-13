package com.lying.variousoddities.entity.ai.passive;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import org.apache.commons.compress.utils.Lists;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.passive.EntityKobold;
import com.lying.variousoddities.item.ItemHeldFlag;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityAIKoboldParade extends Goal
{
	private final Level world;
	private final EntityKobold kobold;
    private double speedModifier;
	
    private int paradeIndex;
    private LivingEntity paradeLeader = null;
    private int paradeTime = 0;
    
    private static final int INDEX_RANGE = 10000;
    private static final Random rand = new Random();
    
	public EntityAIKoboldParade(EntityKobold entityIn, double speed)
	{
		this.world = entityIn.getLevel();
		this.kobold = entityIn;
		this.speedModifier = speed;
		
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}
	
    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean canUse()
    {
		this.paradeIndex = getParadeIndex(this.kobold);
    	if(!isJune() || !world.isDay() || this.kobold.getTarget() != null)
    		return false;
    	
        if(!this.kobold.isLeashed())
        {
            this.paradeLeader = getBestLeader();
            return !(paradeLeader == null || this.kobold.distanceTo(this.paradeLeader) < 6.0D);
        }
        
        return false;
    }
    
    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse()
    {
        return timeToParade() && isValidParadeLeader(this.paradeLeader) && --paradeTime > 0;
    }
    
    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop()
    {
        this.paradeLeader = null;
        this.paradeTime = 0;
    }
    
    public void start()
    {
    	this.paradeTime = (Reference.Values.TICKS_PER_SECOND * 15) + rand.nextInt(Reference.Values.TICKS_PER_MINUTE);
    }
    
    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick()
    {
        if(isValidParadeLeader(this.paradeLeader))
        {
        	// Occasionally wave flag
        	if(this.kobold.getMainHandItem().isEmpty() || this.kobold.getMainHandItem().getItem() instanceof ItemHeldFlag)
	        	if(!this.kobold.swinging && rand.nextInt(Reference.Values.TICKS_PER_SECOND * 5) == 0)
	        		this.kobold.swing(InteractionHand.MAIN_HAND);
        	
        	// Move towards parade leader
            double dist = (double)this.kobold.distanceTo(paradeLeader);
            if(dist > 4D)
            {
            	this.kobold.getLookControl().setLookAt(this.paradeLeader, 10.0F, (float)this.kobold.getMaxHeadXRot());
            	
            	Vec3 leaderPos = paradeLeader.position();
            	Vec3 koboldPos = this.kobold.position();
            	Vec3 direction = leaderPos.subtract(koboldPos).normalize();
            	
	            Vec3 position = direction.scale(Math.max(dist - 2.0D, 0.0D));
	            this.kobold.getNavigation().moveTo(this.kobold.getX() + position.x, this.kobold.getY() + position.y, this.kobold.getZ() + position.z, this.speedModifier);
            }
        }
    }
    
    public boolean timeToParade()
    {
    	return isJune() && this.world.isDay();
    }
    
    public boolean isJune()
    {
    	Calendar calendar = new GregorianCalendar();
        return calendar.get(Calendar.MONTH) == Calendar.JUNE;
    }
    
    public int getParadeIndex(Entity entityIn)
    {
    	Random rng = (new Random(entityIn.getUUID().getLeastSignificantBits()));
    	if(entityIn instanceof Player)
    		return this.paradeIndex - rng.nextInt(INDEX_RANGE / 1000);
    	
    	return rng.nextInt(INDEX_RANGE);
    }
    
    public LivingEntity getBestLeader()
    {
    	LivingEntity leader = null;
    	
    	AABB searchArea = this.kobold.getBoundingBox().inflate(16.0D, 4.0D, 16.0D);
    	List<LivingEntity> paraders = Lists.newArrayList();
    	paraders.addAll(world.getEntitiesOfClass(EntityKobold.class, searchArea));
    	paraders.addAll(this.world.getEntitiesOfClass(Player.class, searchArea));
    	
    	paraders.removeIf(new Predicate<LivingEntity>()
    	{
    		public boolean apply(LivingEntity input)
    		{
    			return !isValidParadeLeader(input) || getParadeIndex(input) >= paradeIndex;
    		}
    	});
    	
        double paradeDistance = Double.MAX_VALUE;
        int closestIndex = 0;
        for(LivingEntity parader : paraders)
        {
        	int index = getParadeIndex(parader);
            double dist = this.kobold.distanceToSqr(parader);
            if((index >= closestIndex && dist <= paradeDistance) || leader == null)
            {
                paradeDistance = dist;
                closestIndex = index;
                leader = parader;
            }
        }
        
        return leader;
    }
    
    public boolean isValidParadeLeader(LivingEntity entityIn)
    {
    	if(entityIn == null || !entityIn.isAlive() || !world.canSeeSky(entityIn.blockPosition())) return false;
    	
    	if(entityIn instanceof Player)
    	{
    		Player player = (Player)entityIn;
    		ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
    		ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
    		
			if(!main.isEmpty() && main.getItem() instanceof ItemHeldFlag)
				return true;
			else if(!off.isEmpty() && off.getItem() instanceof ItemHeldFlag)
				return true;
			else
				return false;
    	}
    	else if(entityIn instanceof Mob)
    	{
    		if(((Mob)entityIn).isLeashed()) return false;
    	}
    	
    	return true;
    }
}
