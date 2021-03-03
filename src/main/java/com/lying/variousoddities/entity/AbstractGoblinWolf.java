package com.lying.variousoddities.entity;

import java.util.Random;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public abstract class AbstractGoblinWolf extends TameableEntity
{
	public static final DataParameter<Boolean>	BEGGING		= EntityDataManager.<Boolean>createKey(AbstractGoblinWolf.class, DataSerializers.BOOLEAN);
	public static final DataParameter<Boolean>	JAW_OPEN	= EntityDataManager.<Boolean>createKey(AbstractGoblinWolf.class, DataSerializers.BOOLEAN);
	
    private int openJawCounter;
    private float jawOpenness;
    private float prevJawOpenness;
    
    /** Float used to smooth the rotation of the wolf head */
    private float headRotationCourse;
    private float headRotationCourseOld;
    
    /** true is the wolf is wet else false */
    private boolean isWet;
    
    /** True if the wolf is shaking else False */
    private boolean isShaking;
    /** This time increases while wolf is shaking and emitting water particles. */
    private float timeShaking;
    private float prevTimeShaking;
	
	protected AbstractGoblinWolf(EntityType<? extends AbstractGoblinWolf> type, World worldIn)
	{
		super(type, worldIn);
	}
	
	protected void registerData()
	{
		super.registerData();
		getDataManager().register(BEGGING, false);
		getDataManager().register(JAW_OPEN, false);
	}
	
    public static boolean canSpawnAt(EntityType<? extends MobEntity> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        return world.getDifficulty() != Difficulty.PEACEFUL && CreatureEntity.canSpawnOn(animal, world, reason, pos, random);
    }
    
    public boolean isJawOpen(){ return getDataManager().get(JAW_OPEN).booleanValue(); }
    public void setJawOpen(boolean par1Bool){ getDataManager().set(JAW_OPEN, par1Bool); }
    
	public float getJawState(float partialTicks)
	{
        return this.prevJawOpenness + (this.jawOpenness - this.prevJawOpenness) * partialTicks;
	}
    
    private void openJaw()
    {
        if (!this.world.isRemote)
        {
            this.openJawCounter = 1;
            setJawOpen(true);
        }
    }
    
    protected SoundEvent getAmbientSound()
    {
    	openJaw();
        if(getRNG().nextInt(3) == 0)
        	return (/*isTameable() ||*/ getHealth() < 10.0F) ? SoundEvents.ENTITY_WOLF_WHINE : SoundEvents.ENTITY_WOLF_PANT;
        else return super.getAmbientSound();
    }
    
    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
    	openJaw();
        return super.getHurtSound(damageSourceIn);
    }
    
    protected SoundEvent getDeathSound()
    {
    	openJaw();
        return super.getDeathSound();
    }
    
    public float getShakeAngle(float p_70923_1_, float p_70923_2_)
    {
        float f = (this.prevTimeShaking + (this.timeShaking - this.prevTimeShaking) * p_70923_1_ + p_70923_2_) / 1.8F;
        f = Math.max(0F, Math.min(1F, f));
        return MathHelper.sin(f * (float)Math.PI) * MathHelper.sin(f * (float)Math.PI * 11.0F) * 0.15F * (float)Math.PI;
    }
    
    public float getInterestedAngle(float p_70917_1_)
    {
        return (this.headRotationCourseOld + (this.headRotationCourse - this.headRotationCourseOld) * p_70917_1_) * 0.15F * (float)Math.PI;
    }
    
    public float getTailRotation()
    {
        if(getAttackTarget() != null) return 1.5393804F;
        else return 0.55F - (this.getMaxHealth() - getHealth() * 0.02F) * ((float)Math.PI * 0.33F);
    }
    
    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 0.4F;
    }
    
    public boolean isBegging(){ return getDataManager().get(BEGGING).booleanValue(); }
    public void setBegging(boolean par1Bool){ getDataManager().set(BEGGING, par1Bool); }
    
    public void tick()
    {
        super.tick();
        
        if(!this.world.isRemote && this.isWet && !this.isShaking && !this.hasPath() && this.onGround)
        {
            this.isShaking = true;
            this.timeShaking = 0.0F;
            this.prevTimeShaking = 0.0F;
            this.world.setEntityState(this, (byte)8);
        }
    }
    
    public void livingTick()
    {
        super.livingTick();
        this.headRotationCourseOld = this.headRotationCourse;
        if(this.isBegging()) this.headRotationCourse += (1.0F - this.headRotationCourse) * 0.4F;
        else this.headRotationCourse += (0.0F - this.headRotationCourse) * 0.4F;
        
        if (this.openJawCounter > 0 && ++this.openJawCounter > 20)
        {
            this.openJawCounter = 0;
            setJawOpen(false);
        }
        this.prevJawOpenness = this.jawOpenness;
        if(isJawOpen()){ this.jawOpenness += (1.0F - this.jawOpenness) * 0.1F + 0.05F; }
        else{ this.jawOpenness += (-this.jawOpenness) * 0.1F - 0.05F; }
        this.jawOpenness = Math.max(0F, Math.min(1F, this.jawOpenness));
        
        if(this.isWet())
        {
            this.isWet = true;
            this.isShaking = false;
            this.timeShaking = 0.0F;
            this.prevTimeShaking = 0.0F;
        }
        else if(this.isShaking)
        {
            if(this.timeShaking == 0.0F) this.playSound(SoundEvents.ENTITY_WOLF_SHAKE, this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            this.prevTimeShaking = this.timeShaking;
            this.timeShaking += 0.05F;
            
            if(this.prevTimeShaking >= 2.0F)
            {
                this.isWet = false;
                this.isShaking = false;
                this.prevTimeShaking = 0.0F;
                this.timeShaking = 0.0F;
            }
            
            if(this.timeShaking > 0.4F)
            {
                float yBase = (float)this.getBoundingBox().minY;
                for(int j = 0; j < (int)(MathHelper.sin((this.timeShaking - 0.4F) * (float)Math.PI) * 7.0F); ++j)
                {
                    float xOff = (this.rand.nextFloat() * 2.0F - 1.0F) * this.getWidth() * 0.5F;
                    float zOff = (this.rand.nextFloat() * 2.0F - 1.0F) * this.getWidth() * 0.5F;
                    this.world.addParticle(ParticleTypes.SPLASH, this.getPosX() + (double)xOff, (double)(yBase + 0.8F), this.getPosZ() + (double)zOff, this.getMotion().x, this.getMotion().y, this.getMotion().z);
                }
            }
        }
    }
    
    public boolean isNoDespawnRequired(){ return true; }
}
