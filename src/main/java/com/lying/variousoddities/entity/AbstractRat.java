package com.lying.variousoddities.entity;

import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.ai.hostile.EntityAIRatGnawing;
import com.lying.variousoddities.entity.ai.hostile.EntityAIRatStand;
import com.lying.variousoddities.utility.DataHelper;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public abstract class AbstractRat extends CreatureEntity
{
	public static final DataParameter<Byte>		EYES		= EntityDataManager.<Byte>createKey(AbstractRat.class, DataSerializers.BYTE);
	public static final DataParameter<Integer>	STAND_TIME	= EntityDataManager.<Integer>createKey(AbstractRat.class, DataSerializers.VARINT);
	public static final DataParameter<Integer>	BREED		= EntityDataManager.<Integer>createKey(AbstractRat.class, DataSerializers.VARINT);
	
	private static final int STAND_CAP = 10;
	public int standTimer = 0;
	
	private final int size;
	
	protected AbstractRat(EntityType<? extends AbstractRat> type, World worldIn, int sizeIn)
	{
		super(type, worldIn);
		setPose(Pose.CROUCHING);
		size = sizeIn;
	}
	
	protected void registerData()
	{
		super.registerData();
		DataHelper.Booleans.registerBooleanByte(getDataManager(), EYES, false);
		getDataManager().register(STAND_TIME, Integer.valueOf(0));
		getDataManager().register(BREED, 0);
	}
	
	protected void registerGoals()
	{
		this.goalSelector.addGoal(3, new EntityAIRatStand(this));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
		
	    if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(getType()))
	    	this.goalSelector.addGoal(3, new EntityAIRatGnawing(this, 6, 100));
	}
	
	protected abstract EntitySize getStandingSize();
	protected abstract EntitySize getCrouchingSize();
	
	protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn)
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
	
	public EntitySize getSize(Pose pose)
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
			BlockPos blockUp = getPosition().offset(Direction.UP);
			World theWorld = this.getEntityWorld();
			if(theWorld == null || !theWorld.isAirBlock(blockUp)) return false;
		}
		return getPose() == Pose.STANDING;
	}
	
	public void setStanding(boolean standingState)
	{
		setPose(standingState ? Pose.STANDING : Pose.CROUCHING);
	}
	public float getStandingHeight(){ return getStandingSize().height; }
	
	public float getStand(){ return getStandTime() < 0 ? 1F : (float)getStandTime() / (float)STAND_CAP; }
	public int getStandTime(){ return getDataManager().get(STAND_TIME).intValue(); }
	public void setStandTime(int par1Int){ getDataManager().set(STAND_TIME, Math.min(STAND_CAP, par1Int)); }
	
	public void startStanding(int par1Int)
	{
		this.standTimer = par1Int;
		setStanding(true);
	}
	
	public EnumRatBreed getRatBreed(){ return EnumRatBreed.getByID(getBreed()); }
	public int getBreed(){ return getDataManager().get(BREED).intValue(); }
	public void setBreed(int par1Int){ getDataManager().set(BREED, par1Int); }
	
	public boolean getEyesGlow(){ return DataHelper.Booleans.getBooleanByte(getDataManager(), EYES); }
	public void setEyesGlow(boolean par1Bool){ DataHelper.Booleans.setBooleanByte(getDataManager(), par1Bool, EYES); }
	
	public abstract int getRandomBreed();
	
    public static boolean canSpawnAt(EntityType<? extends MobEntity> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        return CreatureEntity.canSpawnOn(animal, world, reason, pos, random);
    }
    
    public void writeAdditional(CompoundNBT compound)
    {
    	super.writeAdditional(compound);
    	CompoundNBT display = new CompoundNBT();
	    	display.putBoolean("Eyes", getEyesGlow());
	        display.putInt("Breed", getBreed());
        compound.put("Display", display);
        
        CompoundNBT standing = new CompoundNBT();
	        standing.putInt("StandTime", getStandTime());
	        standing.putInt("StandTimer", this.standTimer);
        compound.put("Standing", standing);
    }
    
    public void readAdditional(CompoundNBT compound)
    {
    	super.readAdditional(compound);
    	CompoundNBT display = compound.getCompound("Display");
	    	setEyesGlow(display.getBoolean("Eyes"));
	    	setBreed(display.getInt("Breed"));
	    
	    CompoundNBT standing = new CompoundNBT();
		    setStandTime(standing.getInt("StandTime"));
		    this.standTimer = standing.getInt("StandTimer");
    }
	
	public void tick()
	{
		super.tick();
		setStandTime(getStandTime() + (getStanding() ? 1 : -(int)Math.signum(getStandTime())));
	}
	
    protected void updateAITasks()
    {
    	super.updateAITasks();
		if(getStanding()){ setStanding(--standTimer > 0); }
    }
    
	public boolean attackEntityAsMob(Entity entityIn)
	{
		if(super.attackEntityAsMob(entityIn))
		{
	    	if(entityIn instanceof LivingEntity && this.getRatBreed() == EnumRatBreed.PLAGUE)
	    		((LivingEntity)entityIn).addPotionEffect(new EffectInstance(Effects.POISON, 10, ratSize()));
			
			return true;
		}
		return false;
	}
    
    protected void collideWithEntity(Entity entityIn)
    {
    	if(entityIn instanceof AbstractRat && ((AbstractRat)entityIn).ratSize() != ratSize())
    		return;
    	super.collideWithEntity(entityIn);
    }
	
	public int ratSize(){ return this.size; }
	
    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.ENTITY_SILVERFISH_AMBIENT;
    }
    
    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return SoundEvents.ENTITY_SILVERFISH_HURT;
    }
    
    protected SoundEvent getDeathSound()
    {
        return SoundEvents.ENTITY_SILVERFISH_DEATH;
    }
    
    @Nullable
    public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag)
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
