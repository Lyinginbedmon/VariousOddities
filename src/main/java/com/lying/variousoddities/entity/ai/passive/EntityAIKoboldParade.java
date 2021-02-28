package com.lying.variousoddities.entity.ai.passive;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.passive.EntityKobold;
import com.lying.variousoddities.item.ItemHeldFlag;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class EntityAIKoboldParade extends Goal
{
	private final World world;
	private final EntityKobold kobold;
    private double speedModifier;
	
    private int paradeIndex;
    private LivingEntity paradeLeader = null;
    private int paradeTime = 0;
    
    private static final int INDEX_RANGE = 10000;
    private static final Random rand = new Random();
    
	public EntityAIKoboldParade(EntityKobold entityIn, double speed)
	{
		this.world = entityIn.getEntityWorld();
		this.kobold = entityIn;
		this.speedModifier = speed;
		
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
	}
	
    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
		this.paradeIndex = getParadeIndex(this.kobold);
    	if(!isJune() || !world.isDaytime() || this.kobold.getAttackTarget() != null)
    		return false;
    	
        if(!this.kobold.getLeashed())
        {
            this.paradeLeader = getBestLeader();
            return !(paradeLeader == null || this.kobold.getDistanceSq(this.paradeLeader) < 6.0D);
        }
        
        return false;
    }
    
    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting()
    {
        return timeToParade() && isValidParadeLeader(this.paradeLeader) && --paradeTime > 0;
    }
    
    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask()
    {
        this.paradeLeader = null;
        this.paradeTime = 0;
    }
    
    public void startExecuting()
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
        	if(this.kobold.getHeldItemMainhand().isEmpty() || this.kobold.getHeldItemMainhand().getItem() instanceof ItemHeldFlag)
	        	if(!this.kobold.isSwingInProgress && rand.nextInt(Reference.Values.TICKS_PER_SECOND * 5) == 0)
	        		this.kobold.swingArm(Hand.MAIN_HAND);
        	
        	// Move towards parade leader
            double dist = (double)this.kobold.getDistance(paradeLeader);
            if(dist > 4D)
            {
            	this.kobold.getLookController().setLookPositionWithEntity(this.paradeLeader, 10.0F, (float)this.kobold.getVerticalFaceSpeed());
            	
            	Vector3d leaderPos = paradeLeader.getPositionVec();
            	Vector3d koboldPos = this.kobold.getPositionVec();
            	Vector3d direction = leaderPos.subtract(koboldPos).normalize();
            	
	            Vector3d position = direction.scale(Math.max(dist - 2.0D, 0.0D));
	            this.kobold.getNavigator().tryMoveToXYZ(this.kobold.getPosX() + position.x, this.kobold.getPosY() + position.y, this.kobold.getPosZ() + position.z, this.speedModifier);
            }
        }
    }
    
    public boolean timeToParade()
    {
    	return isJune() && this.world.isDaytime();
    }
    
    public boolean isJune()
    {
    	Calendar calendar = new GregorianCalendar();
        return calendar.get(Calendar.MONTH) == Calendar.JUNE;
    }
    
    public int getParadeIndex(Entity entityIn)
    {
    	Random rng = (new Random(entityIn.getUniqueID().getLeastSignificantBits()));
    	if(entityIn instanceof PlayerEntity)
    		return this.paradeIndex - rng.nextInt(INDEX_RANGE / 1000);
    	
    	return rng.nextInt(INDEX_RANGE);
    }
    
    public LivingEntity getBestLeader()
    {
    	LivingEntity leader = null;
    	
    	AxisAlignedBB searchArea = this.kobold.getBoundingBox().grow(16.0D, 4.0D, 16.0D);
    	List<LivingEntity> paraders = this.world.getEntitiesWithinAABB(EntityKobold.class, searchArea);
    	paraders.addAll(this.world.getEntitiesWithinAABB(PlayerEntity.class, searchArea));
    	
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
            double dist = this.kobold.getDistanceSq(parader);
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
    	if(entityIn == null || !entityIn.isAlive() || !world.canSeeSky(entityIn.getPosition())) return false;
    	
    	if(entityIn instanceof PlayerEntity)
    	{
    		PlayerEntity player = (PlayerEntity)entityIn;
    		ItemStack main = player.getHeldItem(Hand.MAIN_HAND);
    		ItemStack off = player.getHeldItem(Hand.OFF_HAND);
    		
			if(!main.isEmpty() && main.getItem() instanceof ItemHeldFlag)
				return true;
			else if(!off.isEmpty() && off.getItem() instanceof ItemHeldFlag)
				return true;
			else
				return false;
    	}
    	else if(entityIn instanceof MobEntity)
    	{
    		if(((MobEntity)entityIn).getLeashed()) return false;
    	}
    	
    	return true;
    }
}
