package com.lying.variousoddities.entity;

import java.util.Random;

import javax.annotation.Nullable;

import com.lying.variousoddities.entity.ai.passive.EntityAICrabRave;
import com.lying.variousoddities.utility.DataHelper;

import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class AbstractCrab extends EntityOddity
{
    public static final DataParameter<Byte>		BARNACLES	= EntityDataManager.<Byte>createKey(AbstractCrab.class, DataSerializers.BYTE);
    public static final DataParameter<Byte>		BIG_L		= EntityDataManager.<Byte>createKey(AbstractCrab.class, DataSerializers.BYTE);
    public static final DataParameter<Byte>		BIG_R		= EntityDataManager.<Byte>createKey(AbstractCrab.class, DataSerializers.BYTE);
    public static final DataParameter<Integer>	COLOR		= EntityDataManager.<Integer>createKey(AbstractCrab.class, DataSerializers.VARINT);
    public static final DataParameter<Boolean>	SCUTTLE		= EntityDataManager.<Boolean>createKey(AbstractCrab.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean>	PARTYING	= EntityDataManager.<Boolean>createKey(AbstractCrab.class, DataSerializers.BOOLEAN);
    
    private BlockPos jukeboxPos = null;
    
	protected AbstractCrab(EntityType<? extends AbstractCrab> type, World worldIn)
	{
		super(type, worldIn);
	    setPathPriority(PathNodeType.WATER, 0.0F);
	}
	
	protected void registerData()
	{
		super.registerData();
		DataHelper.Booleans.registerBooleanByte(getDataManager(), BARNACLES, false);
		DataHelper.Booleans.registerBooleanByte(getDataManager(), BIG_L, false);
		DataHelper.Booleans.registerBooleanByte(getDataManager(), BIG_R, false);
		getDataManager().register(COLOR, 0);
		getDataManager().register(SCUTTLE, false);
		
		getDataManager().register(PARTYING, false);
	}
    
    public void registerGoals()
    {
    	super.registerGoals();
        this.goalSelector.addGoal(2, new EntityAICrabRave(this));	// TODO Proper mixin for starting party from jukebox
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
		
	    this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }
	
    public static boolean canSpawnAt(EntityType<? extends MobEntity> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        return CreatureEntity.canSpawnOn(animal, world, reason, pos, random);
    }
    
    public CreatureAttribute getCreatureAttribute(){ return CreatureAttribute.ARTHROPOD; }

    public boolean canBreatheUnderwater()
    {
    	return true;
    }
    
    public boolean hasBarnacles(){ return DataHelper.Booleans.getBooleanByte(getDataManager(), BARNACLES); }
    public void setBarnacles(boolean par1Bool){ DataHelper.Booleans.setBooleanByte(getDataManager(), par1Bool, BARNACLES); }
    
    public boolean hasBigLeftClaw(){ return DataHelper.Booleans.getBooleanByte(getDataManager(), BIG_L); }
    public void setBigLeftClaw(boolean par1Bool){ DataHelper.Booleans.setBooleanByte(getDataManager(), par1Bool, BIG_L); }
    
    public boolean hasBigRightClaw(){ return DataHelper.Booleans.getBooleanByte(getDataManager(), BIG_R); }
    public void setBigRightClaw(boolean par1Bool){ DataHelper.Booleans.setBooleanByte(getDataManager(), par1Bool, BIG_R); }
    
    public int getColor(){ return getDataManager().get(COLOR).intValue(); }
    public void setColor(int par1Int){ getDataManager().set(COLOR, MathHelper.clamp(par1Int, 0, 2)); }
    
    public boolean shouldScuttle()
    {
    	Vector3d motion = getMotion();
    	double motionLength = Math.sqrt((motion.x * motion.x) + (motion.z * motion.z));
    	return getDataManager().get(SCUTTLE) && motionLength > 0.01D;
    }
    public void setScuttle(boolean par1Bool){ getDataManager().set(SCUTTLE, par1Bool); }
	
	public void writeAdditional(CompoundNBT compound)
	{
		super.writeAdditional(compound);
		CompoundNBT displayData = new CompoundNBT();
			displayData.putInt("Color", getColor());
			displayData.putBoolean("Barnacles", hasBarnacles());
			displayData.putBoolean("BigLeft", hasBigLeftClaw());
			displayData.putBoolean("BigRight", hasBigRightClaw());
			displayData.putBoolean("Scuttle", shouldScuttle());
		compound.put("Display", displayData);
	}
	
	public void readAdditional(CompoundNBT compound)
	{
		super.readAdditional(compound);
		if(compound.contains("Display", 10))
		{
			CompoundNBT displayData = compound.getCompound("Display");
			getDataManager().set(COLOR, displayData.getInt("Color"));
			setBarnacles(displayData.getBoolean("Barnacles"));
			setBigLeftClaw(displayData.getBoolean("BigLeft"));
			setBigRightClaw(displayData.getBoolean("BigRight"));
			setScuttle(displayData.getBoolean("Scuttles"));
		}
	}
	
	public boolean isPartying(){ return getDataManager().get(PARTYING).booleanValue(); }
	public void setPartying(BlockPos jukeboxPos)
	{
		this.jukeboxPos = jukeboxPos;
		getDataManager().set(PARTYING, jukeboxPos != null);
	}
	
	public void updateAITasks()
	{
		super.updateAITasks();
		if(isPartying())
			if(
				getAttackTarget() != null || 
				this.jukeboxPos == null || 
				getEntityWorld().getBlockState(this.jukeboxPos).getBlock() != Blocks.JUKEBOX || 
				getEntityWorld().getBlockState(this.jukeboxPos).get(JukeboxBlock.HAS_RECORD) == false)
					setPartying(null);
	}
    
    @Nullable
    public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag)
    {
    	setBarnacles(getRNG().nextBoolean());
    	setBigLeftClaw(getRNG().nextBoolean());
    	setBigRightClaw(getRNG().nextBoolean());
    	setColor(getRNG().nextInt(3));
    	setScuttle(getRNG().nextBoolean());
		return spawnDataIn;
    }
}
