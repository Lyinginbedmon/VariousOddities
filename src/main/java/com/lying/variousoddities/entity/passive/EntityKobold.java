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

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntityKobold extends EntityOddityAgeable implements IFactionMob, ISettlementEntity, IDefaultSpecies
{
    public static final DataParameter<Byte>		HORNS		= EntityDataManager.<Byte>createKey(EntityKobold.class, DataSerializers.BYTE);
    public static final DataParameter<Byte>		SNOUT		= EntityDataManager.<Byte>createKey(EntityKobold.class, DataSerializers.BYTE);
    public static final DataParameter<Integer>	COLOR		= EntityDataManager.<Integer>createKey(EntityKobold.class, DataSerializers.VARINT);
    public static final DataParameter<Byte>		JAW_OPEN	= EntityDataManager.<Byte>createKey(EntityKobold.class, DataSerializers.BYTE);
    public static final DataParameter<Byte>		HAS_EGG		= EntityDataManager.<Byte>createKey(EntityKobold.class, DataSerializers.BYTE);
    public static final DataParameter<Byte>		GUARD		= EntityDataManager.<Byte>createKey(EntityKobold.class, DataSerializers.BYTE);
    
    private int openJawCounter;
    private float jawOpenness;
    private float prevJawOpenness;
    
    private EntityAIOperateRoom operateRoomTask;
    
	public EntityKobold(EntityType<? extends EntityKobold> type, World worldIn)
	{
		super(type, worldIn);
	}
	
	protected void registerData()
	{
		super.registerData();
		Random rand = new Random(this.getUniqueID().getLeastSignificantBits());
		DataHelper.Booleans.registerBooleanByte(getDataManager(), HORNS, rand.nextBoolean());
		DataHelper.Booleans.registerBooleanByte(getDataManager(), SNOUT, rand.nextBoolean());
		getDataManager().register(COLOR, rand.nextInt(3));
		DataHelper.Booleans.registerBooleanByte(getDataManager(), JAW_OPEN, false);
		DataHelper.Booleans.registerBooleanByte(getDataManager(), HAS_EGG, false);
		DataHelper.Booleans.registerBooleanByte(getDataManager(), GUARD, false);
	}
	
	protected void registerGoals()
	{
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
		
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
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return EntityOddity.getAttributes();
    }
    
    public ResourceLocation defaultSpecies(){ return SpeciesRegistry.SPECIES_KOBOLD; }
    
    public static boolean canSpawnAt(EntityType<?> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
	    BlockPos blockpos = pos.down();
	    return reason == SpawnReason.SPAWNER || world.getBlockState(blockpos).canEntitySpawn(world, blockpos, animal);
    }
    
    public boolean isNoDespawnRequired(){ return true; }
	
	public String getFactionName(){ return "kobold"; }
	
	public EntityAIOperateRoom getOperateRoomTask()
	{
		if(this.operateRoomTask == null)
			operateRoomTask = new EntityAIOperateRoom(this);
		
		return this.operateRoomTask;
	}
	
	protected void updateAITasks()
	{
		super.updateAITasks();
		// Cease the mate-dependent part of mating if we haven't completed it by dawn
		if(isInLove() && getEntityWorld().getDayTime() < 15000){ setInLove(false); return; }
	}
	
	public boolean isInLove(){ return getDataManager().get(IN_LOVE).booleanValue(); }
	public void setInLove(boolean par1Bool){ getDataManager().set(IN_LOVE, par1Bool); }
	
	public void onMatingFinish()
	{
		setInLove(false);
		setGrowingAge(6000);
	}
    
    public void livingTick()
    {
    	super.livingTick();
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
	
	public HandSide getPrimaryHand()
	{
		return (new Random(getUniqueID().getLeastSignificantBits())).nextBoolean() ? HandSide.LEFT : HandSide.RIGHT;
	}
	
	public AgeableEntity func_241840_a(ServerWorld p_241840_1_, AgeableEntity p_241840_2_)
	{
		return null;
	}
	
	public boolean isCarryingEgg(){ return DataHelper.Booleans.getBooleanByte(getDataManager(), HAS_EGG); }
	
	public void setCarryingEgg(boolean par1Bool){ DataHelper.Booleans.setBooleanByte(getDataManager(), par1Bool, HAS_EGG); }
	
	public boolean isHatcheryGuardian(){ return DataHelper.Booleans.getBooleanByte(getDataManager(), GUARD); }
	
	public void setHatcheryGuardian(boolean par1Bool)
	{
		DataHelper.Booleans.setBooleanByte(getDataManager(), par1Bool, GUARD);
		if(par1Bool)
		{
			if(getHeldItemOffhand().isEmpty())
				setHeldItem(Hand.OFF_HAND, Items.TORCH.getDefaultInstance());
		}
	}
	
	public int getColor(){ return getDataManager().get(COLOR).intValue(); }
	
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
	
    public boolean isJawOpen(){ return DataHelper.Booleans.getBooleanByte(getDataManager(), JAW_OPEN); }
    public void setJawOpen(boolean par1Bool){ DataHelper.Booleans.setBooleanByte(getDataManager(), par1Bool, JAW_OPEN); }
    
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
	
	public boolean getShortSnout(){ return DataHelper.Booleans.getBooleanByte(getDataManager(), SNOUT); }
	
	public boolean getHorns(){ return DataHelper.Booleans.getBooleanByte(getDataManager(), HORNS); }
	
	public void writeAdditional(CompoundNBT compound)
	{
		super.writeAdditional(compound);
		CompoundNBT displayData = new CompoundNBT();
			displayData.putInt("Color", getColor());
			displayData.putBoolean("Horns", getHorns());
			displayData.putBoolean("Snout", getShortSnout());
		compound.put("Display", displayData);
		compound.putBoolean("InLove", isInLove());
		compound.putBoolean("HasEgg", isCarryingEgg());
		compound.putBoolean("IsGuard", isHatcheryGuardian());
	}
	
	public void readAdditional(CompoundNBT compound)
	{
		super.readAdditional(compound);
		if(compound.contains("Display", 10))
		{
			CompoundNBT displayData = compound.getCompound("Display");
			getDataManager().set(COLOR, displayData.getInt("Color"));
			DataHelper.Booleans.setBooleanByte(getDataManager(), displayData.getBoolean("Horns"), HORNS);
			DataHelper.Booleans.setBooleanByte(getDataManager(), displayData.getBoolean("Snout"), SNOUT);
		}
		setInLove(compound.getBoolean("InLove"));
		setCarryingEgg(compound.getBoolean("HasEgg"));
		DataHelper.Booleans.setBooleanByte(getDataManager(), compound.getBoolean("IsGuard"), GUARD);
	}
	
	public void setAttackTarget(@Nullable LivingEntity entitylivingbaseIn)
	{
		super.setAttackTarget(entitylivingbaseIn);
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
