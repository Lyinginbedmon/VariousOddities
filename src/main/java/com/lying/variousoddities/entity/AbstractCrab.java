package com.lying.variousoddities.entity;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AbstractCrab extends EntityOddity
{
    public static final EntityDataAccessor<Boolean>	BARNACLES	= SynchedEntityData.defineId(AbstractCrab.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean>	BIG_L		= SynchedEntityData.defineId(AbstractCrab.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean>	BIG_R		= SynchedEntityData.defineId(AbstractCrab.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer>	COLOR		= SynchedEntityData.defineId(AbstractCrab.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean>	SCUTTLE		= SynchedEntityData.defineId(AbstractCrab.class, EntityDataSerializers.BOOLEAN);
    
    public static final EntityDataAccessor<Boolean>	PARTYING	= SynchedEntityData.defineId(AbstractCrab.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer>	BUBBLES		= SynchedEntityData.defineId(AbstractCrab.class, EntityDataSerializers.INT);
    
    private BlockPos jukeboxPos = null;
    
	protected AbstractCrab(EntityType<? extends AbstractCrab> type, Level worldIn)
	{
		super(type, worldIn);
	    setPathPriority(PathNodeType.WATER, 0.0F);
	}
	
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		getEntityData().define(BARNACLES, false);
		getEntityData().define(BIG_L, false);
		getEntityData().define(BIG_R, false);
		getEntityData().define(COLOR, 0);
		getEntityData().define(SCUTTLE, false);
		
		getEntityData().define(PARTYING, false);
		getEntityData().define(BUBBLES, 0);
	}
    
    public void registerGoals()
    {
    	super.registerGoals();
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(7, new LookAtGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
		
	    this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }
    
    public MobType getCreatureAttribute(){ return MobType.ARTHROPOD; }

    public boolean canBreatheUnderwater()
    {
    	return true;
    }
    
    public boolean hasBarnacles(){ return getEntityData().get(BARNACLES).booleanValue(); }
    public void setBarnacles(boolean par1Bool){ getEntityData().set(BARNACLES, par1Bool); }
    
    public boolean hasBigLeftClaw(){ return getEntityData().get(BIG_L).booleanValue(); }
    public void setBigLeftClaw(boolean par1Bool){ getEntityData().set(BIG_L, par1Bool); }
    
    public boolean hasBigRightClaw(){ return getEntityData().get(BIG_R).booleanValue(); }
    public void setBigRightClaw(boolean par1Bool){ getEntityData().set(BIG_R, par1Bool); }
    
    public int getColor(){ return getEntityData().get(COLOR).intValue(); }
    public void setColor(int par1Int){ getEntityData().set(COLOR, Mth.clamp(par1Int, 0, 2)); }
    
    public boolean shouldScuttle()
    {
    	Vec3 motion = getMotion();
    	double motionLength = Math.sqrt((motion.x * motion.x) + (motion.z * motion.z));
    	return getEntityData().get(SCUTTLE) && motionLength > 0.01D;
    }
    public void setScuttle(boolean par1Bool){ getEntityData().set(SCUTTLE, par1Bool); }
	
	public void writeAdditional(CompoundTag compound)
	{
		super.writeAdditional(compound);
		CompoundTag displayData = new CompoundTag();
			displayData.putInt("Color", getColor());
			displayData.putBoolean("Barnacles", hasBarnacles());
			displayData.putBoolean("BigLeft", hasBigLeftClaw());
			displayData.putBoolean("BigRight", hasBigRightClaw());
			displayData.putBoolean("Scuttle", shouldScuttle());
		compound.put("Display", displayData);
	}
	
	public void readAdditional(CompoundTag compound)
	{
		super.readAdditional(compound);
		if(compound.contains("Display", 10))
		{
			CompoundTag displayData = compound.getCompound("Display");
			getEntityData().set(COLOR, displayData.getInt("Color"));
			setBarnacles(displayData.getBoolean("Barnacles"));
			setBigLeftClaw(displayData.getBoolean("BigLeft"));
			setBigRightClaw(displayData.getBoolean("BigRight"));
			setScuttle(displayData.getBoolean("Scuttles"));
		}
	}
	
	public static void startParty(Level world, BlockPos pos, boolean party)
	{
		for(AbstractCrab crab : world.getEntitiesOfClass(AbstractCrab.class, new AABB(pos).inflate(6D)))
			if(crab.getTarget() == null)
				crab.setPartying(pos);
	}
	
	public boolean isPartying(){ return getEntityData().get(PARTYING).booleanValue(); }
	public void setPartying(BlockPos jukeboxPos)
	{
		this.jukeboxPos = jukeboxPos;
		getEntityData().set(PARTYING, jukeboxPos != null);
	}
	
	public void updateAITasks()
	{
		super.updateAITasks();
		if(isPartying())
			if(
				getTarget() != null || 
				this.jukeboxPos == null || 
				this.jukeboxPos.distSqr(blockPosition()) >= 16D ||
				getLevel().getBlockState(this.jukeboxPos).getBlock() != Blocks.JUKEBOX || 
				getLevel().getBlockState(this.jukeboxPos).getValue(JukeboxBlock.HAS_RECORD) == false)
					setPartying(null);
	}
	
    public void startBubbling()
    {
    	if(getBubbles() == 0 && this.random.nextInt(4) == 0)
    		setBubbles(40 + this.random.nextInt(20));
    }
    
    public int getBubbles(){ return getEntityData().get(BUBBLES).intValue(); }
    public void setBubbles(int par1Int){ getEntityData().set(BUBBLES, par1Int); }
    
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
    	Vec3 forward = Vec3.fromPitchYaw(0, yaw);
    	Vec3 left = Vec3.fromPitchYaw(0, yaw - 90F);
    	double widthBase = getBbWidth() * 0.3D;
    	Vec3 pos = position();
        for(int i=0; i<10; ++i)
        {
        	double xPos = pos.x + (forward.x * 0.8D) + (left.x * widthBase * (this.random.nextDouble() - 0.5D));
        	double yPos = pos.y + (this.random.nextDouble()) * (double)getBbHeight() + getBbHeight()*0.2D;
        	double zPos = pos.z + (forward.z * 0.8D) + (left.z * widthBase * (this.random.nextDouble() - 0.5D));
        	
            getLevel().addParticle(ParticleTypes.BUBBLE, xPos, yPos, zPos, 0D, 0D, 0D);
        }
    }
    
    protected SoundEvent getAmbientSound()
    {
    	startBubbling();
        return super.getAmbientSound();
    }
    
    public float getVoicePitch()
    {
    	return (super.getVoicePitch() - 1.0F) * 0.1F;
    }
    
    public int getTalkInterval()
    {
    	return 200;
    }
    
    @Nullable
    public ILivingEntityData onInitialSpawn(ServerLevel worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundTag dataTag)
    {
    	setBarnacles(getRNG().nextBoolean());
    	setBigLeftClaw(getRNG().nextBoolean());
    	setBigRightClaw(getRNG().nextBoolean());
    	setColor(getRNG().nextInt(3));
    	setScuttle(getRNG().nextBoolean());
		return spawnDataIn;
    }
}
