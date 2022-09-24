package com.lying.variousoddities.entity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.data.VOItemTags;
import com.lying.variousoddities.entity.ai.EntityAIWargWander;
import com.lying.variousoddities.entity.ai.hostile.EntityAIWorgFollowGoblin;
import com.lying.variousoddities.entity.ai.passive.EntityAIGoblinWolfBeg;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.utility.DataHelper;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Abstract class defining the shared properties between all wolves raised and bred by goblins.
 * @author Lying
 */
public abstract class AbstractGoblinWolf extends TamableAnimal
{
	protected static final EntityDataAccessor<Integer> GENETICS	= SynchedEntityData.defineId(AbstractGoblinWolf.class, EntityDataSerializers.INT);
	
	public static final EntityDataAccessor<Integer>	COLOR		= SynchedEntityData.defineId(AbstractGoblinWolf.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Boolean>	BEGGING		= SynchedEntityData.defineId(AbstractGoblinWolf.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Boolean>	JAW_OPEN	= SynchedEntityData.defineId(AbstractGoblinWolf.class, EntityDataSerializers.BOOLEAN);
	
    private List<Tuple<Goal, Integer>> aggressiveBehaviours;
    
    private int openJawCounter;
    private float jawOpenness;
    private float prevJawOpenness;
    
    private float headRotationCourse;
    private float headRotationCourseOld;
    
    private boolean isWet;
    private boolean isShaking;
    private float timeShaking;
    private float prevTimeShaking;
    
    private int goblinTimer = 0;
    private int eatTicks = 0;
	
	protected AbstractGoblinWolf(EntityType<? extends AbstractGoblinWolf> type, Level worldIn)
	{
		super(type, worldIn);
	}
	
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		getEntityData().define(GENETICS, Genetics.DEFAULT.toVal());
		getEntityData().define(COLOR, 0);
		getEntityData().define(BEGGING, false);
		getEntityData().define(JAW_OPEN, false);
	}
	
	public void registerGoals()
	{
		super.registerGoals();
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(5, new EntityAIWorgFollowGoblin(this));
		this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
		this.goalSelector.addGoal(8, new EntityAIWargWander(this, 1.0D));
		this.goalSelector.addGoal(9, new EntityAIGoblinWolfBeg(this, 8F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
		
		if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(getType()))
		{
		    this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
			this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
			this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
			this.targetSelector.addGoal(3, (new HurtByTargetGoal(this, AbstractGoblinWolf.class)).setAlertOthers());
		}
		
		applyGeneticAI();
	}
	
	public boolean checkSpawnRules(LevelAccessor world, MobSpawnType reason)
    {
        return level.getDifficulty() != Difficulty.PEACEFUL && super.checkSpawnRules(world, reason);
    }
    
    public void addAdditionalSaveData(CompoundTag compound)
    {
    	super.addAdditionalSaveData(compound);
    	compound.putInt("Genes", getEntityData().get(GENETICS).intValue());
    	CompoundTag display = new CompoundTag();
    		display.putInt("Color", getColor());
    	compound.put("Display", display);
    }
    
    public void readAdditionalSaveData(CompoundTag compound)
    {
    	super.readAdditionalSaveData(compound);
    	setGenetics(compound.getInt("Genes"));
    	CompoundTag display = compound.getCompound("Display");
    		setColor(display.getInt("Color"));
    }
    
    public boolean isFoodItem(ItemStack stack)
    {
    	return stack.is(VOItemTags.WORG_FOOD);
    }
    
    public int getColor(){ return getEntityData().get(COLOR).intValue(); }
    public void setColor(int par1Int){ getEntityData().set(COLOR, Mth.clamp(par1Int, 0, 2)); }
    
    public boolean isJawOpen(){ return getEntityData().get(JAW_OPEN).booleanValue(); }
    public void setJawOpen(boolean par1Bool){ getEntityData().set(JAW_OPEN, par1Bool); }
	public float getJawState(float partialTicks)
	{
        return this.prevJawOpenness + (this.jawOpenness - this.prevJawOpenness) * partialTicks;
	}
    private void openJaw()
    {
        if(!this.level.isClientSide)
        {
            this.openJawCounter = 1;
            setJawOpen(true);
        }
    }
    
    protected SoundEvent getAmbientSound()
    {
    	openJaw();
        if(getRandom().nextInt(3) == 0)
        	return (isTame() && getHealth() < 10.0F) ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT;
        else
        	return SoundEvents.WOLF_AMBIENT;
    }
    
    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
    	openJaw();
        return SoundEvents.WOLF_HURT;
    }
    
    protected SoundEvent getDeathSound()
    {
    	openJaw();
        return SoundEvents.WOLF_DEATH;
    }
    
    @OnlyIn(Dist.CLIENT)
    public boolean isWet(){ return this.isWet; }
    
    @OnlyIn(Dist.CLIENT)
    public float getShadingWhileWet(float partialTicks)
    {
    	return Math.min(0.5F + Mth.lerp(partialTicks, this.prevTimeShaking, this.timeShaking) / 2.0F * 0.5F, 1.0F);
    }
    
    @OnlyIn(Dist.CLIENT)
    public float getShakeAngle(float partialTicks, float offset)
    {
        float f = (this.prevTimeShaking + (this.timeShaking - this.prevTimeShaking) * partialTicks + offset) / 1.8F;
        f = Math.max(0F, Math.min(1F, f));
        return Mth.sin(f * (float)Math.PI) * Mth.sin(f * (float)Math.PI * 11.0F) * 0.15F * (float)Math.PI;
    }
    
    @OnlyIn(Dist.CLIENT)
    public float getInterestedAngle(float partialTicks)
    {
        return (this.headRotationCourseOld + (this.headRotationCourse - this.headRotationCourseOld) * partialTicks) * 0.15F * (float)Math.PI;
    }
    
    public float getTailRotation()
    {
        if(getTarget() != null)
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
    
    public boolean isBegging(){ return getEntityData().get(BEGGING).booleanValue(); }
    public void setBegging(boolean par1Bool){ getEntityData().set(BEGGING, par1Bool); }
    
    public void tick()
    {
        super.tick();
        
        if(isInWaterRainOrBubble())
        {
        	this.isWet = true;
        	if(this.isShaking && !getLevel().isClientSide)
        	{
        		this.getLevel().broadcastEntityEvent(this, (byte)56);
        		resetShaking();
        	}
        }
        else if((this.isWet || this.isShaking) && this.isShaking)
        {
        	if(this.timeShaking == 0F)
        		playSound(SoundEvents.WOLF_SHAKE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
        	
        	this.prevTimeShaking = this.timeShaking;
        	this.timeShaking += 0.05F;
        	if(this.prevTimeShaking >= 2F)
        	{
        		this.isWet = false;
        		resetShaking();
        	}
        	
        	if(this.timeShaking > 0.4F)
        	{
        		int i = (int)(Mth.sin((this.timeShaking - 0.4F) * (float)Math.PI) * 7F);
        		Vec3 motion = this.getDeltaMovement();
        		do
        		{
        			double randX = getX() + ((getRandom().nextDouble() * 2D - 1D) - getBbWidth() * 0.5D);
        			double randZ = getZ() + ((getRandom().nextDouble() * 2D - 1D) - getBbWidth() * 0.5D);
        			this.level.addParticle(ParticleTypes.SPLASH, randX, (double)(getY() + 0.8F), randZ, motion.x, motion.y, motion.z);
        		}
        		while(--i > 0);
        	}
        }
    }
    
    public boolean isTameable()
    {
    	return !this.isTame() && this.goblinTimer == 0;
    }
    
    private void resetShaking()
    {
    	this.isShaking = false;
    	this.timeShaking = 0F;
    	this.prevTimeShaking = 0F;
    }
    
    public void aiStep()
    {
        super.aiStep();
        
        // Head tilting for begging
        this.headRotationCourseOld = this.headRotationCourse;
        if(this.isBegging()) this.headRotationCourse += (1.0F - this.headRotationCourse) * 0.4F;
        else this.headRotationCourse += (0.0F - this.headRotationCourse) * 0.4F;
        
        // Jaw motion handling
        if (this.openJawCounter > 0 && ++this.openJawCounter > 20)
        {
            this.openJawCounter = 0;
            setJawOpen(false);
        }
        this.prevJawOpenness = this.jawOpenness;
        if(isJawOpen()){ this.jawOpenness += (1.0F - this.jawOpenness) * 0.1F + 0.05F; }
        else{ this.jawOpenness += (-this.jawOpenness) * 0.1F - 0.05F; }
        this.jawOpenness = Math.max(0F, Math.min(1F, this.jawOpenness));
        
        if(!this.getLevel().isClientSide && this.isWet && !this.isShaking && !getNavigation().isInProgress() && this.onGround)
        {
        	resetShaking();
        	this.isShaking = true;
        	this.getLevel().broadcastEntityEvent(this, (byte)8);
        }
        
        // Tameability affected by interaction with goblins
        if(!this.getLevel().isClientSide && this.goblinTimer > 0 && --this.goblinTimer%Reference.Values.TICKS_PER_SECOND == 0)
        	if(!getLevel().getEntitiesOfClass(EntityGoblin.class, this.getBoundingBox().inflate(8, 4, 8)).isEmpty())
        		setGoblinSight(Reference.Values.TICKS_PER_DAY);
        
        // Eating held food
        if(!this.getLevel().isClientSide && this.isAlive() && this.getHealth() < this.getMaxHealth())
        {
        	++this.eatTicks;
        	ItemStack heldItem = this.getItemBySlot(EquipmentSlot.MAINHAND);
        	if(this.canEatItem(heldItem))
        		if(this.eatTicks > 600)
        		{
        			heal(heldItem.getItem().getFoodProperties(heldItem, this).getNutrition());
        			ItemStack heldItemUsed = heldItem.finishUsingItem(getLevel(), this);
        			if(!heldItemUsed.isEmpty())
        				this.setItemSlot(EquipmentSlot.MAINHAND, heldItemUsed);
        			this.eatTicks = 0;
        		}
        		else if(this.eatTicks > 560 && this.random.nextFloat() < 0.1F)
        		{
        			this.playSound(this.getEatingSound(heldItem), 1F, 1F);
        			this.getLevel().broadcastEntityEvent(this, (byte)45);
        		}
        }
    }
    
    public void setGoblinSight(int par1Int){ this.goblinTimer = par1Int; }
    
    public boolean canEatItem(ItemStack itemStackIn)
    {
    	return itemStackIn.getItem().getFoodProperties(itemStackIn, this) != null && this.getTarget() == null;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(byte id)
    {
    	switch(id)
    	{
	    	case 8:
	    		this.isShaking = true;
	    		this.timeShaking = 0F;
	    		this.prevTimeShaking = 0F;
	    		break;
	    	case 45:
	    		ItemStack heldItem = this.getItemBySlot(EquipmentSlot.MAINHAND);
	    		if(!heldItem.isEmpty())
	    			for(int i = 0; i < 8; ++i)
	    			{
	    				Vec3 pos = (new Vec3(((double)this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D)).xRot(-this.getXRot() * ((float)Math.PI / 180F)).yRot(-this.getYRot() * ((float)Math.PI / 180F));
	    				this.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, heldItem), this.getX() + this.getLookAngle().x / 2.0D, this.getY(), this.getZ() + this.getLookAngle().z / 2.0D, pos.x, pos.y + 0.05D, pos.z);
	    			}
	    		break;
	    	case 56:
	    		resetShaking();
	    		break;
    		default:
    			super.handleEntityEvent(id);
    			break;
    	}
    }
    
    public boolean isNoDespawnRequired(){ return true; }
    
    public Genetics getGenetics(){ return new Genetics(getEntityData().get(GENETICS).intValue()); }
    public void setGenetics(int genesIn){ getEntityData().set(GENETICS, genesIn); applyGeneticAI(); }
    public void setGenetics(Genetics genesIn){ setGenetics(genesIn.toVal()); }
    
    public abstract void getAggressiveBehaviours();
    public void addGeneticAI(int priority, Goal goalIn)
    {
    	this.aggressiveBehaviours.add(new Tuple<Goal,Integer>(goalIn,priority));
    }
    public void applyGeneticAI()
    {
    	if(this.aggressiveBehaviours == null || this.aggressiveBehaviours.isEmpty())
    	{
    		this.aggressiveBehaviours = new ArrayList<>();
    		getAggressiveBehaviours();
    	}
    	
    	// Clear aggressive behaviours
    	for(Tuple<Goal,Integer> behaviour : this.aggressiveBehaviours)
    		this.targetSelector.removeGoal(behaviour.getA());
    	
    	// Apply aggressive behaviours IF not passive
    	if(!getGenetics().gene(4))
	    	for(Tuple<Goal,Integer> behaviour : this.aggressiveBehaviours)
    			this.targetSelector.addGoal(behaviour.getB(), behaviour.getA());
    }
    
    public void setTarget(@Nullable LivingEntity target)
    {
    	if(!isTame() && target != null)
    	{
        	if(target.getType() == VOEntities.GOBLIN.get())
        		return;
        	
        	if(target.isAlive() && isOrderedToSit())
        		setOrderedToSit(false);
    	}
    	
    	super.setTarget(target);
    }
    
    protected void dropAllDeathLoot(DamageSource source)
    {
    	for(EquipmentSlot slot : EquipmentSlot.values())
    	{
    		ItemStack heldStack = getItemBySlot(slot);
    		if(!heldStack.isEmpty())
    		{
    			spawnAtLocation(heldStack);
    			setItemSlot(slot, ItemStack.EMPTY);
    		}
    	}
    	
    	super.dropAllDeathLoot(source);
    }
    
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag)
    {
    	setColor(getRandom().nextInt(3));
		return spawnDataIn;
    }
    
    /**
     * Holder class for managing a set of booleans representing a simple model of genetics.
     * @author Lying
     */
    public static class Genetics
    {
    	/** Chance of gene mutation during breeding */
    	private static final float MUTATION = 0.3F;
    	
    	public static final Genetics DEFAULT = new Genetics(224);
    	
    	private int value = 0;
    	
    	public Genetics(int byteIn)
    	{
    		this.value = Math.max(0, Math.min(255, byteIn));
    	}
    	
    	public int toVal(){ return value; }
    	
    	/**
    	 * Returns the value of the given gene.<br><br>
    	 * 0 - Floppy ear (left)<br>
    	 * 1 - Floppy ear (right)<br>
    	 * 2 - Short snout<br>
    	 * 3 - Lolling tongue<br>
    	 * 4 - Passive<br>
    	 * 5 - Spooked by loud noises<br>
    	 * 6 - Warg gene A<br>
    	 * 7 - Warg gene B
    	 */
    	public boolean gene(int n)
    	{
    		return DataHelper.Bytes.getBit((byte)value, n);
    	}
    	
    	/** Returns a random cross of the given genetics, with additional random mutation */
    	public static Genetics cross(Genetics genesA, Genetics genesB, RandomSource rand)
    	{
    		int val = 0;
    		for(int i=0; i<8; i++)
    		{
    			boolean geneA = genesA.gene(i);
    			boolean geneB = genesB.gene(i);
    			boolean gene = rand.nextBoolean() ? geneA : geneB;
    			
    			if(rand.nextFloat() < MUTATION)
    				gene = !gene;
    			
    			val = DataHelper.Bytes.setBit(val, i, gene);
    		}
    		
    		return new Genetics(val);
    	}
    	
    	/** Returns a random set of genes, suitable for a goblin-bred wolf */
    	public static Genetics random(RandomSource rand)
    	{
    		int val = 0;
    		for(int i=0; i<8; i++)
    			val = DataHelper.Bytes.setBit(val, i, rand.nextBoolean());
    		
    		return new Genetics(val);
    	}
    }
}
