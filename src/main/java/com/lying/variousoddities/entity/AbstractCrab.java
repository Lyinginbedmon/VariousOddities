package com.lying.variousoddities.entity;

import java.util.Random;

import javax.annotation.Nullable;

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
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class AbstractCrab extends EntityOddity
{
    public static final DataParameter<Boolean>	BARNACLES	= EntityDataManager.<Boolean>createKey(AbstractCrab.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean>	BIG_L		= EntityDataManager.<Boolean>createKey(AbstractCrab.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean>	BIG_R		= EntityDataManager.<Boolean>createKey(AbstractCrab.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer>	COLOR		= EntityDataManager.<Integer>createKey(AbstractCrab.class, DataSerializers.VARINT);
    public static final DataParameter<Boolean>	SCUTTLE		= EntityDataManager.<Boolean>createKey(AbstractCrab.class, DataSerializers.BOOLEAN);
    
    public static final DataParameter<Boolean>	PARTYING	= EntityDataManager.<Boolean>createKey(AbstractCrab.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer>	BUBBLES		= EntityDataManager.<Integer>createKey(AbstractCrab.class, DataSerializers.VARINT);
    
    private BlockPos jukeboxPos = null;
    
	protected AbstractCrab(EntityType<? extends AbstractCrab> type, World worldIn)
	{
		super(type, worldIn);
	    setPathPriority(PathNodeType.WATER, 0.0F);
	}
	
	protected void registerData()
	{
		super.registerData();
		getDataManager().register(BARNACLES, false);
		getDataManager().register(BIG_L, false);
		getDataManager().register(BIG_R, false);
		getDataManager().register(COLOR, 0);
		getDataManager().register(SCUTTLE, false);
		
		getDataManager().register(PARTYING, false);
		getDataManager().register(BUBBLES, 0);
	}
    
    public void registerGoals()
    {
    	super.registerGoals();
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
    
    public boolean hasBarnacles(){ return getDataManager().get(BARNACLES).booleanValue(); }
    public void setBarnacles(boolean par1Bool){ getDataManager().set(BARNACLES, par1Bool); }
    
    public boolean hasBigLeftClaw(){ return getDataManager().get(BIG_L).booleanValue(); }
    public void setBigLeftClaw(boolean par1Bool){ getDataManager().set(BIG_L, par1Bool); }
    
    public boolean hasBigRightClaw(){ return getDataManager().get(BIG_R).booleanValue(); }
    public void setBigRightClaw(boolean par1Bool){ getDataManager().set(BIG_R, par1Bool); }
    
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
	
	public static void startParty(IWorld world, BlockPos pos, boolean party)
	{
		for(AbstractCrab crab : world.getEntitiesWithinAABB(AbstractCrab.class, new AxisAlignedBB(pos).grow(6D)))
			if(crab.getAttackTarget() == null)
				crab.setPartying(pos);
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
				this.jukeboxPos.distanceSq(getPosition()) >= 16D ||
				getEntityWorld().getBlockState(this.jukeboxPos).getBlock() != Blocks.JUKEBOX || 
				getEntityWorld().getBlockState(this.jukeboxPos).get(JukeboxBlock.HAS_RECORD) == false)
					setPartying(null);
	}
	
    public void startBubbling()
    {
    	if(getBubbles() == 0 && this.rand.nextInt(4) == 0)
    		setBubbles(40 + this.rand.nextInt(20));
    }
    
    public int getBubbles(){ return getDataManager().get(BUBBLES).intValue(); }
    public void setBubbles(int par1Int){ getDataManager().set(BUBBLES, par1Int); }
    
    public void tick()
    {
    	super.tick();
    	if(getBubbles() > 0)
    		doBubbling();
    }
    
    public void doBubbling()
    {
    	setBubbles(getBubbles() - 1);
    	
    	float yaw = this.renderYawOffset + (shouldScuttle() ? 90F : 0F);
    	Vector3d forward = Vector3d.fromPitchYaw(0, yaw);
    	Vector3d left = Vector3d.fromPitchYaw(0, yaw - 90F);
    	double widthBase = getWidth() * 0.3D;
    	Vector3d pos = getPositionVec();
        for(int i=0; i<10; ++i)
        {
        	double xPos = pos.x + (forward.x * 0.8D) + (left.x * widthBase * (this.rand.nextDouble() - 0.5D));
        	double yPos = pos.y + (this.rand.nextDouble()) * (double)getHeight() + getHeight()*0.2D;
        	double zPos = pos.z + (forward.z * 0.8D) + (left.z * widthBase * (this.rand.nextDouble() - 0.5D));
        	
            getEntityWorld().addParticle(ParticleTypes.BUBBLE, xPos, yPos, zPos, 0D, 0D, 0D);
        }
    }
    
    protected SoundEvent getAmbientSound()
    {
    	startBubbling();
        return super.getAmbientSound();
    }
    
    protected float getSoundPitch()
    {
    	return (super.getSoundPitch() - 1.0F) * 0.1F;
    }
    
    public int getTalkInterval()
    {
    	return 200;
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
