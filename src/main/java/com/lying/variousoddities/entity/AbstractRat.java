package com.lying.variousoddities.entity;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.ai.hostile.EntityAIRatGnawing;
import com.lying.variousoddities.entity.ai.hostile.EntityAIRatStand;
import com.lying.variousoddities.utility.DataHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public abstract class AbstractRat extends PathfinderMob
{
	public static final EntityDataAccessor<Byte>		EYES		= SynchedEntityData.defineId(AbstractRat.class, EntityDataSerializers.BYTE);
	public static final EntityDataAccessor<Integer>	STAND_TIME	= SynchedEntityData.defineId(AbstractRat.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer>	BREED		= SynchedEntityData.defineId(AbstractRat.class, EntityDataSerializers.INT);
	
	private static final int STAND_CAP = 10;
	public int standTimer = 0;
	
	private final int size;
	
	protected AbstractRat(EntityType<? extends AbstractRat> type, Level worldIn, int sizeIn)
	{
		super(type, worldIn);
		setPose(Pose.CROUCHING);
		size = sizeIn;
	}
	
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		DataHelper.Booleans.registerBooleanByte(getEntityData(), EYES, false);
		getEntityData().define(STAND_TIME, Integer.valueOf(0));
		getEntityData().define(BREED, 0);
	}
	
	protected void registerGoals()
	{
		this.goalSelector.addGoal(3, new EntityAIRatStand(this));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		
	    if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(getType()))
	    	this.goalSelector.addGoal(3, new EntityAIRatGnawing(this, 6, 100));
	}
	
	protected abstract EntityDimensions getStandingSize();
	protected abstract EntityDimensions getCrouchingSize();
	
	protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn)
	{
		switch(poseIn)
		{
			case STANDING:
				return getStandingSize().height * 0.75F;
			default:
				return getCrouchingSize().height * 0.85F;
		}
	}
	
	public ImmutableList<Pose> getAvailablePoses()
	{
		return ImmutableList.of(Pose.CROUCHING, Pose.STANDING);
	}
	
	public EntityDimensions getSize(Pose pose)
	{
		switch(pose)
		{
			case STANDING:
				return getStandingSize();
			default:
				return getCrouchingSize();
		}
	}
	
	public boolean getStanding()
	{
		if(getStandingHeight() > 1F)
		{
			BlockPos blockUp = blockPosition().relative(Direction.UP);
			Level theWorld = this.getLevel();
			if(theWorld == null || !theWorld.isEmptyBlock(blockUp)) return false;
		}
		return getPose() == Pose.STANDING;
	}
	
	public void setStanding(boolean standingState)
	{
		setPose(standingState ? Pose.STANDING : Pose.CROUCHING);
	}
	public float getStandingHeight(){ return getStandingSize().height; }
	
	public float getStand(){ return getStandTime() < 0 ? 1F : (float)getStandTime() / (float)STAND_CAP; }
	public int getStandTime(){ return getEntityData().get(STAND_TIME).intValue(); }
	public void setStandTime(int par1Int){ getEntityData().set(STAND_TIME, Math.min(STAND_CAP, par1Int)); }
	
	public void startStanding(int par1Int)
	{
		this.standTimer = par1Int;
		setStanding(true);
	}
	
	public EnumRatBreed getRatBreed(){ return EnumRatBreed.getByID(getBreed()); }
	public int getBreed(){ return getEntityData().get(BREED).intValue(); }
	public void setBreed(int par1Int){ getEntityData().set(BREED, par1Int); }
	
	public boolean getEyesGlow(){ return DataHelper.Booleans.getBooleanByte(getEntityData(), EYES); }
	public void setEyesGlow(boolean par1Bool){ DataHelper.Booleans.setBooleanByte(getEntityData(), par1Bool, EYES); }
	
	public abstract int getRandomBreed();
    
    public void addAdditionalSaveData(CompoundTag compound)
    {
    	super.addAdditionalSaveData(compound);
    	CompoundTag display = new CompoundTag();
	    	display.putBoolean("Eyes", getEyesGlow());
	        display.putInt("Breed", getBreed());
        compound.put("Display", display);
        
        CompoundTag standing = new CompoundTag();
	        standing.putInt("StandTime", getStandTime());
	        standing.putInt("StandTimer", this.standTimer);
        compound.put("Standing", standing);
    }
    
    public void readAdditionalSaveData(CompoundTag compound)
    {
    	super.readAdditionalSaveData(compound);
    	CompoundTag display = compound.getCompound("Display");
	    	setEyesGlow(display.getBoolean("Eyes"));
	    	setBreed(display.getInt("Breed"));
	    
	    CompoundTag standing = new CompoundTag();
		    setStandTime(standing.getInt("StandTime"));
		    this.standTimer = standing.getInt("StandTimer");
    }
	
	public void tick()
	{
		super.tick();
		setStandTime(getStandTime() + (getStanding() ? 1 : -(int)Math.signum(getStandTime())));
	}
	
    protected void customServerAiStep()
    {
    	super.customServerAiStep();
		if(getStanding())
			setStanding(--standTimer > 0);
    }
    
	public boolean doHurtTarget(Entity entityIn)
	{
		if(super.doHurtTarget(entityIn))
		{
	    	if(entityIn instanceof LivingEntity && this.getRatBreed() == EnumRatBreed.PLAGUE)
	    		((LivingEntity)entityIn).addEffect(new MobEffectInstance(MobEffects.POISON, 10, ratSize()));
			
			return true;
		}
		return false;
	}
    
    protected void doPush(Entity entityIn)
    {
    	if(entityIn instanceof AbstractRat && ((AbstractRat)entityIn).ratSize() != ratSize())
    		return;
    	super.doPush(entityIn);
    }
	
	public int ratSize(){ return this.size; }
	
    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.SILVERFISH_AMBIENT;
    }
    
    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return SoundEvents.SILVERFISH_HURT;
    }
    
    protected SoundEvent getDeathSound()
    {
        return SoundEvents.SILVERFISH_DEATH;
    }
    
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag)
    {
    	int random = getRandomBreed();
		setBreed(random);
		setEyesGlow(EnumRatBreed.getByID(random).getEyeGlow());
		return spawnDataIn;
    }
    
	public enum EnumRatBreed
	{
		WHITE("white", true, 0.8F),
		BROWN("brown", false, 0.9F),
		BLACK("black", false, 0.95F),
		PLAGUE("plague", true, 1.1F);
		
		private final String name;
		private final boolean eyeGlow;
		private final float scaleAdjust;
		
		private EnumRatBreed(String nameIn, boolean eyeIn, float scaleIn)
		{
			name = nameIn;
			eyeGlow = eyeIn;
			scaleAdjust = scaleIn;
		}
		
		public String getName(){ return name; }
		public boolean getEyeGlow(){ return eyeGlow; }
		public float getScale(){ return scaleAdjust; }
		
		public static int getID(EnumRatBreed breedIn)
		{
			int index = 0;
			for(EnumRatBreed value : values())
			{
				if(value == breedIn){ return index; }
				index++;
			}
			return -1;
		}
		
		public static EnumRatBreed getByID(int par1Int)
		{
			if(par1Int < 0 || par1Int >= values().length){ return null; }
			return values()[par1Int];
		}
	}
}
