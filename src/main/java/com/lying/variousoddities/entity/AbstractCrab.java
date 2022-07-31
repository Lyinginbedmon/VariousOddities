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
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
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
    public static final DataParameter<Boolean>	BARNACLES	= EntityDataManager.<Boolean>createKey(AbstractCrab.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean>	BIG_L		= EntityDataManager.<Boolean>createKey(AbstractCrab.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean>	BIG_R		= EntityDataManager.<Boolean>createKey(AbstractCrab.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer>	COLOR		= EntityDataManager.<Integer>createKey(AbstractCrab.class, DataSerializers.VARINT);
    public static final DataParameter<Boolean>	SCUTTLE		= EntityDataManager.<Boolean>createKey(AbstractCrab.class, DataSerializers.BOOLEAN);
    
    public static final DataParameter<Boolean>	PARTYING	= EntityDataManager.<Boolean>createKey(AbstractCrab.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer>	BUBBLES		= EntityDataManager.<Integer>createKey(AbstractCrab.class, DataSerializers.VARINT);
    
    private BlockPos jukeboxPos = null;
    
	protected AbstractCrab(EntityType<? extends AbstractCrab> type, Level worldIn)
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
    
    public boolean hasBarnacles(){ return getDataManager().get(BARNACLES).booleanValue(); }
    public void setBarnacles(boolean par1Bool){ getDataManager().set(BARNACLES, par1Bool); }
    
    public boolean hasBigLeftClaw(){ return getDataManager().get(BIG_L).booleanValue(); }
    public void setBigLeftClaw(boolean par1Bool){ getDataManager().set(BIG_L, par1Bool); }
    
    public boolean hasBigRightClaw(){ return getDataManager().get(BIG_R).booleanValue(); }
    public void setBigRightClaw(boolean par1Bool){ getDataManager().set(BIG_R, par1Bool); }
    
    public int getColor(){ return getDataManager().get(COLOR).intValue(); }
    public void setColor(int par1Int){ getDataManager().set(COLOR, Mth.clamp(par1Int, 0, 2)); }
    
    public boolean shouldScuttle()
    {
    	Vec3 motion = getMotion();
    	double motionLength = Math.sqrt((motion.x * motion.x) + (motion.z * motion.z));
    	return getDataManager().get(SCUTTLE) && motionLength > 0.01D;
    }
    public void setScuttle(boolean par1Bool){ getDataManager().set(SCUTTLE, par1Bool); }
	
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
			getDataManager().set(COLOR, displayData.getInt("Color"));
			setBarnacles(displayData.getBoolean("Barnacles"));
			setBigLeftClaw(displayData.getBoolean("BigLeft"));
			setBigRightClaw(displayData.getBoolean("BigRight"));
			setScuttle(displayData.getBoolean("Scuttles"));
		}
	}
	
	public static void startParty(Level world, BlockPos pos, boolean party)
	{
		for(AbstractCrab crab : world.getEntitiesWithinAABB(AbstractCrab.class, new AABB(pos).inflate(6D)))
			if(crab.getTarget() == null)
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
				getTarget() != null || 
				this.jukeboxPos == null || 
				this.jukeboxPos.distanceSq(getPosition()) >= 16D ||
				getEntityWorld().getBlockState(this.jukeboxPos).getBlock() != Blocks.JUKEBOX || 
				getEntityWorld().getBlockState(this.jukeboxPos).get(JukeboxBlock.HAS_RECORD) == false)
					setPartying(null);
	}
	
    public void startBubbling()
    {
    	if(getBubbles() == 0 && this.random.nextInt(4) == 0)
    		setBubbles(40 + this.random.nextInt(20));
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
    	Vec3 forward = Vec3.fromPitchYaw(0, yaw);
    	Vec3 left = Vec3.fromPitchYaw(0, yaw - 90F);
    	double widthBase = getWidth() * 0.3D;
    	Vec3 pos = getPositionVec();
        for(int i=0; i<10; ++i)
        {
        	double xPos = pos.x + (forward.x * 0.8D) + (left.x * widthBase * (this.random.nextDouble() - 0.5D));
        	double yPos = pos.y + (this.random.nextDouble()) * (double)getHeight() + getHeight()*0.2D;
        	double zPos = pos.z + (forward.z * 0.8D) + (left.z * widthBase * (this.random.nextDouble() - 0.5D));
        	
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
