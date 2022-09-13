package com.lying.variousoddities.entity.passive;

import java.util.Random;

import javax.annotation.Nullable;

import com.lying.variousoddities.api.entity.IDefaultSpecies;
import com.lying.variousoddities.api.entity.IFactionMob;
import com.lying.variousoddities.entity.EntityOddity;
import com.lying.variousoddities.entity.EntityOddityAgeable;
import com.lying.variousoddities.entity.ISettlementEntity;
import com.lying.variousoddities.entity.ai.EntityAIOperateRoom;
import com.lying.variousoddities.entity.ai.controller.ControllerKobold;
import com.lying.variousoddities.entity.ai.group.EntityGroup;
import com.lying.variousoddities.entity.ai.group.EntityGroupKobold;
import com.lying.variousoddities.entity.ai.group.GroupHandler;
import com.lying.variousoddities.init.VOSoundEvents;
import com.lying.variousoddities.species.SpeciesRegistry;
import com.lying.variousoddities.utility.DataHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class EntityKobold extends EntityOddityAgeable implements IFactionMob, ISettlementEntity, IDefaultSpecies
{
    public static final EntityDataAccessor<Byte>	HORNS		= SynchedEntityData.defineId(EntityKobold.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Byte>	SNOUT		= SynchedEntityData.defineId(EntityKobold.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Integer>	COLOR		= SynchedEntityData.defineId(EntityKobold.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Byte>	JAW_OPEN	= SynchedEntityData.defineId(EntityKobold.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Byte>	HAS_EGG		= SynchedEntityData.defineId(EntityKobold.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Byte>	GUARD		= SynchedEntityData.defineId(EntityKobold.class, EntityDataSerializers.BYTE);
    
    private int openJawCounter;
    private float jawOpenness;
    private float prevJawOpenness;
    
    private EntityAIOperateRoom operateRoomTask;
    
	public EntityKobold(EntityType<? extends EntityKobold> type, Level worldIn)
	{
		super(type, worldIn);
	}
	
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		Random rand = new Random(this.getUUID().getLeastSignificantBits());
		DataHelper.Booleans.registerBooleanByte(getEntityData(), HORNS, rand.nextBoolean());
		DataHelper.Booleans.registerBooleanByte(getEntityData(), SNOUT, rand.nextBoolean());
		getEntityData().define(COLOR, rand.nextInt(3));
		DataHelper.Booleans.registerBooleanByte(getEntityData(), JAW_OPEN, false);
		DataHelper.Booleans.registerBooleanByte(getEntityData(), HAS_EGG, false);
		DataHelper.Booleans.registerBooleanByte(getEntityData(), GUARD, false);
	}
	
	protected void registerGoals()
	{
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		
	    this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}
	
	/**
	 * Adds all EntityControllers to the list of controllers for this entity.<br>
	 * EntityControllers contain clusters of AI tasks to be executed under corresponding contexts.
	 */
    protected void addControllers()
    {
    	addController(new ControllerKobold.ControllerKoboldChild(2, this));
        addController(new ControllerKobold.ControllerKoboldAdult(3, this));
        addController(new ControllerKobold.ControllerKoboldGuardian(1, this));
    }
	
    public static AttributeSupplier.Builder createAttributes()
    {
        return EntityOddity.createAttributes();
    }
    
    public ResourceLocation defaultSpecies(){ return SpeciesRegistry.SPECIES_KOBOLD; }
    
    public boolean isNoDespawnRequired(){ return true; }
	
	public String getFactionName(){ return "kobold"; }
	
	public EntityAIOperateRoom getOperateRoomTask()
	{
		if(this.operateRoomTask == null)
			operateRoomTask = new EntityAIOperateRoom(this);
		
		return this.operateRoomTask;
	}
	
	protected void customServerAiStep()
	{
		super.customServerAiStep();
		// Cease the mate-dependent part of mating if we haven't completed it by dawn
		if(isInLove() && getLevel().getDayTime() < 15000){ setInLove(false); return; }
	}
	
	public boolean isInLove(){ return getEntityData().get(IN_LOVE).booleanValue(); }
	public void setInLove(boolean par1Bool){ getEntityData().set(IN_LOVE, par1Bool); }
	
	public void onMatingFinish()
	{
		setInLove(false);
		setAge(6000);
	}
    
    public void aiStep()
    {
    	super.aiStep();
        if (this.openJawCounter > 0 && ++this.openJawCounter > 60)
        {
            this.openJawCounter = 0;
            setJawOpen(false);
        }
        this.prevJawOpenness = this.jawOpenness;
        if(isJawOpen()){ this.jawOpenness += (1.0F - this.jawOpenness) * 0.1F + 0.05F; }
        else{ this.jawOpenness += (-this.jawOpenness) * 0.1F - 0.05F; }
        this.jawOpenness = Math.max(0F, Math.min(1F, this.jawOpenness));
    }
	
	public HumanoidArm getMainArm()
	{
		return (new Random(getUUID().getLeastSignificantBits())).nextBoolean() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
	}
	
	public AgeableMob getBreedOffspring(ServerLevel p_241840_1_, AgeableMob p_241840_2_)
	{
		return null;
	}
	
	public boolean isCarryingEgg(){ return DataHelper.Booleans.getBooleanByte(getEntityData(), HAS_EGG); }
	
	public void setCarryingEgg(boolean par1Bool){ DataHelper.Booleans.setBooleanByte(getEntityData(), par1Bool, HAS_EGG); }
	
	public boolean isHatcheryGuardian(){ return DataHelper.Booleans.getBooleanByte(getEntityData(), GUARD); }
	
	public void setHatcheryGuardian(boolean par1Bool)
	{
		DataHelper.Booleans.setBooleanByte(getEntityData(), par1Bool, GUARD);
		if(par1Bool)
		{
			if(getOffhandItem().isEmpty())
				setItemInHand(InteractionHand.OFF_HAND, Items.TORCH.getDefaultInstance());
		}
	}
	
	public int getColor(){ return getEntityData().get(COLOR).intValue(); }
	
	public float getGrowth(){ return 0.45F; }
	
    /**
     * Returns the progression of this mob towards adulthood.
     * Used to rescale the mob as it develops.
     * @return A float value between 0 (adult) and 1 (newborn)
     */
	public float getAgeProgress(){ return 0F; }
	
	public float getJawState(float partialTicks)
	{
        return this.prevJawOpenness + (this.jawOpenness - this.prevJawOpenness) * partialTicks;
	}
	
    public boolean isJawOpen(){ return DataHelper.Booleans.getBooleanByte(getEntityData(), JAW_OPEN); }
    public void setJawOpen(boolean par1Bool){ DataHelper.Booleans.setBooleanByte(getEntityData(), par1Bool, JAW_OPEN); }
    
    private void openJaw()
    {
        if (!this.level.isClientSide)
        {
            this.openJawCounter = 1;
            setJawOpen(true);
        }
    }

    protected SoundEvent getAmbientSound()
    {
    	openJaw();
        return VOSoundEvents.ENTITY_KOBOLD_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
    	openJaw();
        return VOSoundEvents.ENTITY_KOBOLD_HURT;
    }
    
    protected SoundEvent getDeathSound()
    {
    	openJaw();
        return VOSoundEvents.ENTITY_KOBOLD_DEATH;
    }
	
	public boolean getShortSnout(){ return DataHelper.Booleans.getBooleanByte(getEntityData(), SNOUT); }
	
	public boolean getHorns(){ return DataHelper.Booleans.getBooleanByte(getEntityData(), HORNS); }
	
	public void addAdditionalSaveData(CompoundTag compound)
	{
		super.addAdditionalSaveData(compound);
		CompoundTag displayData = new CompoundTag();
			displayData.putInt("Color", getColor());
			displayData.putBoolean("Horns", getHorns());
			displayData.putBoolean("Snout", getShortSnout());
		compound.put("Display", displayData);
		compound.putBoolean("InLove", isInLove());
		compound.putBoolean("HasEgg", isCarryingEgg());
		compound.putBoolean("IsGuard", isHatcheryGuardian());
	}
	
	public void readAdditionalSaveData(CompoundTag compound)
	{
		super.readAdditionalSaveData(compound);
		if(compound.contains("Display", 10))
		{
			CompoundTag displayData = compound.getCompound("Display");
			getEntityData().set(COLOR, displayData.getInt("Color"));
			DataHelper.Booleans.setBooleanByte(getEntityData(), displayData.getBoolean("Horns"), HORNS);
			DataHelper.Booleans.setBooleanByte(getEntityData(), displayData.getBoolean("Snout"), SNOUT);
		}
		setInLove(compound.getBoolean("InLove"));
		setCarryingEgg(compound.getBoolean("HasEgg"));
		DataHelper.Booleans.setBooleanByte(getEntityData(), compound.getBoolean("IsGuard"), GUARD);
	}
	
	public void setTarget(@Nullable LivingEntity entitylivingbaseIn)
	{
		super.setTarget(entitylivingbaseIn);
		if(entitylivingbaseIn != null)
		{
			EntityGroup group = GroupHandler.getEntityMemberGroup(this);
			if(group == null)
			{
				EntityGroup group2 = GroupHandler.getEntityTargetGroup(entitylivingbaseIn);
				if(group2 != null)
					group2.addMember(this);
				else
				{
					group = new EntityGroupKobold();
					group.addMember(this);
					group.addTarget(entitylivingbaseIn);
					GroupHandler.addGroup(group);
				}
			}
		}
	}
}
