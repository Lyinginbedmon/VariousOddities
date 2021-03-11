package com.lying.variousoddities.entity;

import java.util.Random;

import javax.annotation.Nullable;

import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.ai.EntityAIWargWander;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.OwnerHurtByTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtTargetGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Abstract class defining the shared properties between all wolves raised and bred by goblins.
 * @author Lying
 */
public abstract class AbstractGoblinWolf extends TameableEntity
{
	public static final DataParameter<Integer>	COLOR		= EntityDataManager.<Integer>createKey(AbstractGoblinWolf.class, DataSerializers.VARINT);
	public static final DataParameter<Boolean>	BEGGING		= EntityDataManager.<Boolean>createKey(AbstractGoblinWolf.class, DataSerializers.BOOLEAN);
	public static final DataParameter<Boolean>	JAW_OPEN	= EntityDataManager.<Boolean>createKey(AbstractGoblinWolf.class, DataSerializers.BOOLEAN);
	
    private int openJawCounter;
    private float jawOpenness;
    private float prevJawOpenness;
    
    private float headRotationCourse;
    private float headRotationCourseOld;
    
    private boolean isWet;
    private boolean isShaking;
    private float timeShaking;
    private float prevTimeShaking;
	
	protected AbstractGoblinWolf(EntityType<? extends AbstractGoblinWolf> type, World worldIn)
	{
		super(type, worldIn);
	}
	
	protected void registerData()
	{
		super.registerData();
		getDataManager().register(COLOR, 0);
		getDataManager().register(BEGGING, false);
		getDataManager().register(JAW_OPEN, false);
	}
	
	public void registerGoals()
	{
		super.registerGoals();
		this.goalSelector.addGoal(1, new SwimGoal(this));
		this.goalSelector.addGoal(2, new SitGoal(this));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
		this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(8, new EntityAIWargWander(this, 1.0D));
		this.goalSelector.addGoal(10, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(10, new LookRandomlyGoal(this));
		
		if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(getType()))
		{
			this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
			this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
			this.targetSelector.addGoal(3, (new HurtByTargetGoal(this)).setCallsForHelp());
		}
	}
	
    public static boolean canSpawnAt(EntityType<? extends MobEntity> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        return world.getDifficulty() != Difficulty.PEACEFUL && CreatureEntity.canSpawnOn(animal, world, reason, pos, random);
    }
    
    public void writeAdditional(CompoundNBT compound)
    {
    	super.writeAdditional(compound);
    	CompoundNBT display = new CompoundNBT();
    		display.putInt("Color", getColor());
    	compound.put("Display", display);
    }
    
    public void readAdditional(CompoundNBT compound)
    {
    	super.readAdditional(compound);
    	CompoundNBT display = compound.getCompound("Display");
    		setColor(display.getInt("Color"));
    }
    
    public int getColor(){ return getDataManager().get(COLOR).intValue(); }
    public void setColor(int par1Int){ getDataManager().set(COLOR, MathHelper.clamp(par1Int, 0, 2)); }
    
    public boolean isJawOpen(){ return getDataManager().get(JAW_OPEN).booleanValue(); }
    public void setJawOpen(boolean par1Bool){ getDataManager().set(JAW_OPEN, par1Bool); }
	public float getJawState(float partialTicks)
	{
        return this.prevJawOpenness + (this.jawOpenness - this.prevJawOpenness) * partialTicks;
	}
    private void openJaw()
    {
        if(!this.world.isRemote)
        {
            this.openJawCounter = 1;
            setJawOpen(true);
        }
    }
    
    protected SoundEvent getAmbientSound()
    {
    	openJaw();
        if(getRNG().nextInt(3) == 0)
        	return (isTamed() && getHealth() < 10.0F) ? SoundEvents.ENTITY_WOLF_WHINE : SoundEvents.ENTITY_WOLF_PANT;
        else
        	return super.getAmbientSound();
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
    
    @OnlyIn(Dist.CLIENT)
    public boolean isWet(){ return this.isWet; }
    
    @OnlyIn(Dist.CLIENT)
    public float getShadingWhileWet(float partialTicks)
    {
    	return Math.min(0.5F + MathHelper.lerp(partialTicks, this.prevTimeShaking, this.timeShaking) / 2.0F * 0.5F, 1.0F);
    }
    
    @OnlyIn(Dist.CLIENT)
    public float getShakeAngle(float partialTicks, float offset)
    {
        float f = (this.prevTimeShaking + (this.timeShaking - this.prevTimeShaking) * partialTicks + offset) / 1.8F;
        f = Math.max(0F, Math.min(1F, f));
        return MathHelper.sin(f * (float)Math.PI) * MathHelper.sin(f * (float)Math.PI * 11.0F) * 0.15F * (float)Math.PI;
    }
    
    @OnlyIn(Dist.CLIENT)
    public float getInterestedAngle(float partialTicks)
    {
        return (this.headRotationCourseOld + (this.headRotationCourse - this.headRotationCourseOld) * partialTicks) * 0.15F * (float)Math.PI;
    }
    
    public float getTailRotation()
    {
        if(getAttackTarget() != null)
        	return 1.5393804F;
        else
        {
        	float health = 1F - (getHealth() / getMaxHealth());
        	return 0.55F - health * ((float)Math.PI * 0.33F);
        }
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
        
        if(isInWaterRainOrBubbleColumn())
        {
        	this.isWet = true;
        	if(this.isShaking && !getEntityWorld().isRemote)
        	{
        		this.getEntityWorld().setEntityState(this, (byte)56);
        		resetShaking();
        	}
        }
        else if((this.isWet || this.isShaking) && this.isShaking)
        {
        	if(this.timeShaking == 0F)
        		playSound(SoundEvents.ENTITY_WOLF_SHAKE, this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
        	
        	this.prevTimeShaking = this.timeShaking;
        	this.timeShaking += 0.05F;
        	if(this.prevTimeShaking >= 2F)
        	{
        		this.isWet = false;
        		resetShaking();
        	}
        	
        	if(this.timeShaking > 0.4F)
        	{
        		int i = (int)(MathHelper.sin((this.timeShaking - 0.4F) * (float)Math.PI) * 7F);
        		Vector3d motion = this.getMotion();
        		do
        		{
        			double randX = getPosX() + ((getRNG().nextDouble() * 2D - 1D) - getWidth() * 0.5D);
        			double randZ = getPosZ() + ((getRNG().nextDouble() * 2D - 1D) - getWidth() * 0.5D);
        			this.world.addParticle(ParticleTypes.SPLASH, randX, (double)(getPosY() + 0.8F), randZ, motion.x, motion.y, motion.z);
        		}
        		while(--i > 0);
        	}
        }
    }
    
    private void resetShaking()
    {
    	this.isShaking = false;
    	this.timeShaking = 0F;
    	this.prevTimeShaking = 0F;
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
        
        if(!this.getEntityWorld().isRemote && this.isWet && !this.isShaking && !this.hasPath() && this.onGround)
        {
        	resetShaking();
        	this.isShaking = true;
        	this.getEntityWorld().setEntityState(this, (byte)8);
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public void handleStatusUpdate(byte id)
    {
    	switch(id)
    	{
	    	case 8:
	    		this.isShaking = true;
	    		this.timeShaking = 0F;
	    		this.prevTimeShaking = 0F;
	    		break;
	    	case 56:
	    		resetShaking();
	    		break;
    		default:
    			super.handleStatusUpdate(id);
    			break;
    	}
    }
    
    public boolean isNoDespawnRequired(){ return true; }
    
    @Nullable
    public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag)
    {
    	setColor(getRNG().nextInt(3));
		return spawnDataIn;
    }
}
