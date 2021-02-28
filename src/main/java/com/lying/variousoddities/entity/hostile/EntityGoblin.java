package com.lying.variousoddities.entity.hostile;

import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.lying.variousoddities.api.entity.IFactionMob;
import com.lying.variousoddities.entity.EntityOddity;
import com.lying.variousoddities.entity.EntityOddityAgeable;
import com.lying.variousoddities.entity.ISettlementEntity;
import com.lying.variousoddities.entity.ai.EntityAIOperateRoom;
import com.lying.variousoddities.entity.ai.controller.ControllerGoblin;
import com.lying.variousoddities.entity.ai.group.EntityGroup;
import com.lying.variousoddities.entity.ai.group.EntityGroupGoblin;
import com.lying.variousoddities.entity.ai.group.GroupHandler;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.init.VOSoundEvents;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.utility.DataHelper;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntityGoblin extends EntityOddityAgeable implements IFactionMob, ISettlementEntity
{
    public static final DataParameter<Byte> 	NOSE = EntityDataManager.<Byte>createKey(EntityGoblin.class, DataSerializers.BYTE);
    public static final DataParameter<Byte> 	EARS = EntityDataManager.<Byte>createKey(EntityGoblin.class, DataSerializers.BYTE);
    public static final DataParameter<Integer> 	COLOR = EntityDataManager.<Integer>createKey(EntityGoblin.class, DataSerializers.VARINT);
    public static final DataParameter<CompoundNBT>	CARRYING = EntityDataManager.<CompoundNBT>createKey(EntityGoblin.class, DataSerializers.COMPOUND_NBT);
    public static final DataParameter<Optional<BlockPos>>	NEST = EntityDataManager.<Optional<BlockPos>>createKey(EntityGoblin.class, DataSerializers.OPTIONAL_BLOCK_POS);
    
    private EntityAIOperateRoom operateRoomTask;
    
    private GoblinType goblinType = GoblinType.BASIC;
    
	public EntityGoblin(EntityType<? extends EntityGoblin> type, World worldIn)
	{
		super(type, worldIn);
		this.goblinType = GoblinType.getRandomType(getRNG());
	}
	
	protected void registerData()
	{
		super.registerData();
		Random rand = new Random(this.getUniqueID().getLeastSignificantBits());
		DataHelper.Booleans.registerBooleanByte(getDataManager(), NOSE, rand.nextBoolean());
		DataHelper.Booleans.registerBooleanByte(getDataManager(), EARS, rand.nextBoolean());
		getDataManager().register(COLOR, rand.nextInt(3));
		getDataManager().register(CARRYING, new CompoundNBT());
		getDataManager().register(NEST, Optional.empty());
	}
	
	protected void registerGoals()
	{
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
	}
	
	protected void addControllers()
	{
    	addController(new ControllerGoblin.ControllerGoblinChild(1, this));
    	addController(new ControllerGoblin.ControllerGoblinWorgTamer(2, this));
    	addController(new ControllerGoblin.ControllerGoblinBasic(5, this));
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return EntityOddity.getAttributes()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 20.0D)
        		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 4.0D)
        		.createMutableAttribute(Attributes.ARMOR, 2.0D);
    }
    
    public static boolean canSpawnAt(EntityType<? extends CreatureEntity> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
	    return CreatureEntity.canSpawnOn(animal, world, reason, pos, random);
    }
	
	public EntityAIOperateRoom getOperateRoomTask()
	{
		if(this.operateRoomTask == null)
			operateRoomTask = new EntityAIOperateRoom(this);
		
		return this.operateRoomTask;
	}
	
	public String getFactionName(){ return "goblin"; }
    
    public boolean getNose(){ return DataHelper.Booleans.getBooleanByte(getDataManager(), NOSE); }
    public void setNose(boolean par1Boolean){ DataHelper.Booleans.setBooleanByte(getDataManager(), par1Boolean, NOSE); }
    
    public boolean getEars(){ return DataHelper.Booleans.getBooleanByte(getDataManager(), EARS); }
    public void setEars(boolean par1Boolean){ DataHelper.Booleans.setBooleanByte(getDataManager(), par1Boolean, EARS); }
    
    public int getColor(){ return ((Integer)getDataManager().get(COLOR)).intValue(); }
    public void setColor(int par1Int){ getDataManager().set(COLOR, Integer.valueOf(par1Int % 3)); }
	
	public GoblinType getGoblinType(){ return this.goblinType; }
	
	public float getGrowth(){ return 0.2F; }
	
	public boolean isInLove(){ return getDataManager().get(IN_LOVE).booleanValue(); }
	public void setInLove(boolean par1Bool){ getDataManager().set(IN_LOVE, par1Bool); }
	
	public boolean isCarrying(){ return !getDataManager().get(CARRYING).isEmpty(); }
	public EntityGoblin getOtherParent()
	{
		EntityGoblin child = VOEntities.GOBLIN.create(getEntityWorld());
		child.readAdditional(getDataManager().get(CARRYING));
		return child;
	}
	public void setCarryingFrom(EntityGoblin parent)
	{
		if(parent == null)
			getDataManager().set(CARRYING, new CompoundNBT());
		else
		{
			CompoundNBT parentData = new CompoundNBT();
			parent.writeAdditional(parentData);
			getDataManager().set(CARRYING, parentData);
		}
	}
	
	public BlockPos getNestSite(){ return getDataManager().get(NEST).orElse((BlockPos)null); }
	public void setNestSite(BlockPos pos){ getDataManager().set(NEST, Optional.of(pos)); }
	
    /**
     * Returns the progression of this mob towards adulthood.
     * Used to rescale the mob as it develops.
     * @return A float value between 0 (adult) and 1 (newborn)
     */
	public float getAgeProgress(){ return 0F; }
	
	public void writeAdditional(CompoundNBT compound)
	{
		super.writeAdditional(compound);
		CompoundNBT displayData = new CompoundNBT();
			displayData.putInt("Color", getColor());
			displayData.putBoolean("Nose", getNose());
			displayData.putBoolean("Ears", getEars());
		compound.put("Display", displayData);
		compound.putInt("Type", getGoblinType().ordinal());
		compound.put("Mate", getDataManager().get(CARRYING));
		if(getNestSite() != null)
			compound.put("Nest", NBTUtil.writeBlockPos(getNestSite()));
	}
	
	public void readAdditional(CompoundNBT compound)
	{
		super.readAdditional(compound);
		if(compound.contains("Display", 10))
		{
			CompoundNBT displayData = compound.getCompound("Display");
			getDataManager().set(COLOR, displayData.getInt("Color"));
			DataHelper.Booleans.setBooleanByte(getDataManager(), displayData.getBoolean("Nose"), NOSE);
			DataHelper.Booleans.setBooleanByte(getDataManager(), displayData.getBoolean("Ears"), EARS);
		}
		this.goblinType = GoblinType.get(compound.getInt("Type"));
		getDataManager().set(CARRYING, compound.getCompound("Mate"));
		if(compound.contains("Nest", 10))
			setNestSite(NBTUtil.readBlockPos(compound.getCompound("Nest")));
	}
	
    protected SoundEvent getAmbientSound()
    {
        return VOSoundEvents.ENTITY_GOBLIN_AMBIENT;
    }
    
    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return VOSoundEvents.ENTITY_GOBLIN_HURT;
    }
    
    protected SoundEvent getDeathSound()
    {
        return VOSoundEvents.ENTITY_GOBLIN_DEATH;
    }
	
    protected float getSoundVolume(){ return this.isChild() ? super.getSoundVolume() * 0.3F : super.getSoundVolume(); }
    
    public void livingTick()
    {
    	/** If goblin has just become an adult OR is pregnant, set growing age to max */
    	boolean child = isChild();
    	super.livingTick();
    	if(isCarrying() || isChild() != child)
    		setGrowingAge(72000);
    }
	
	public AgeableEntity func_241840_a(ServerWorld worldIn, AgeableEntity parent)
	{
		EntityGoblin child = VOEntities.GOBLIN.create(worldIn);
		
		child.setColor(getColor());
		child.setEars(getEars());
		child.setNose(getNose());
		if(parent != null && parent instanceof EntityGoblin)
		{
			EntityGoblin parentGoblin = (EntityGoblin)parent;
			if(getRNG().nextBoolean()) child.setColor(parentGoblin.getColor());
			if(getRNG().nextBoolean()) child.setEars(parentGoblin.getEars());
			if(getRNG().nextBoolean()) child.setNose(parentGoblin.getNose());
		}
		
		return child;
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
					group = new EntityGroupGoblin();
					group.addMember(this);
					group.addTarget(entitylivingbaseIn);
					GroupHandler.addGroup(group);
				}
			}
		}
	}
    
    @Nullable
    public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag)
    {
    	setGrowingAge(Reference.Values.TICKS_PER_DAY + getRNG().nextInt(Reference.Values.TICKS_PER_DAY * 2));
		return spawnDataIn;
    }
	
	public enum GoblinType
	{
		BASIC(70, 1),
		SHAMAN(1, 3),
		WORG_TAMER(29, 2);
		
		@SuppressWarnings("unused")
		private final int spawnWeight;
		public final int authority;
		
		private GoblinType(int weightIn, int authorityIn)
		{
			spawnWeight = weightIn;
			authority = authorityIn;
		}
		
		public static GoblinType get(int par1Int)
		{
			par1Int = Math.max(0, Math.min(values().length - 1, par1Int));
			return values()[par1Int];
		}
		
		public static GoblinType getRandomType(Random rand)
		{
			return BASIC;
//			List<GoblinType> seed = new ArrayList<GoblinType>();
//			for(GoblinType type : values()) for(int i=0; i<type.spawnWeight; i++) seed.add(type);
//			return seed.get(rand.nextInt(seed.size()));
		}
	}
}
