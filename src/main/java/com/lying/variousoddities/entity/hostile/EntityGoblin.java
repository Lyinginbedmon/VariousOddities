package com.lying.variousoddities.entity.hostile;

import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.lying.variousoddities.api.entity.IDefaultSpecies;
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
import com.lying.variousoddities.species.SpeciesRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityGoblin extends EntityOddityAgeable implements IFactionMob, ISettlementEntity, IDefaultSpecies
{
    public static final EntityDataAccessor<Boolean> 	NOSE = SynchedEntityData.defineId(EntityGoblin.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> 	EARS = SynchedEntityData.defineId(EntityGoblin.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean>	VIOLENT = SynchedEntityData.defineId(EntityGoblin.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> 	COLOR = SynchedEntityData.defineId(EntityGoblin.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<CompoundTag>	CARRYING = SynchedEntityData.defineId(EntityGoblin.class, EntityDataSerializers.COMPOUND_TAG);
    public static final EntityDataAccessor<Optional<BlockPos>>	NEST = SynchedEntityData.defineId(EntityGoblin.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    
    private EntityAIOperateRoom operateRoomTask;
    
    private GoblinType goblinType = GoblinType.BASIC;
    
	public EntityGoblin(EntityType<? extends EntityGoblin> type, Level worldIn)
	{
		super(type, worldIn);
		this.goblinType = GoblinType.getRandomType(getRandom());
	}
	
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		Random random = new Random(this.getUUID().getLeastSignificantBits());
		getEntityData().define(NOSE, random.nextBoolean());
		getEntityData().define(EARS, random.nextBoolean());
		getEntityData().define(VIOLENT, random.nextInt(16) > 0);
		getEntityData().define(COLOR, random.nextInt(3));
		getEntityData().define(CARRYING, new CompoundTag());
		getEntityData().define(NEST, Optional.empty());
	}
	
	protected void registerGoals()
	{
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(7, new LookAtGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
	}
	
	protected void addControllers()
	{
    	addController(new ControllerGoblin.ControllerGoblinChild(1, this));
    	addController(new ControllerGoblin.ControllerGoblinWorgTamer(2, this));
    	addController(new ControllerGoblin.ControllerGoblinBasic(5, this));
	}

    public static AttributeSupplier.Builder createAttributes()
    {
        return EntityOddity.createAttributes();
    }
    
    public ResourceLocation defaultSpecies(){ return SpeciesRegistry.SPECIES_GOBLIN; }
	
	public EntityAIOperateRoom getOperateRoomTask()
	{
		if(this.operateRoomTask == null)
			operateRoomTask = new EntityAIOperateRoom(this);
		
		return this.operateRoomTask;
	}
	
	public String getFactionName(){ return "goblin"; }
    
    public boolean getNose(){ return getEntityData().get(NOSE).booleanValue(); }
    public void setNose(boolean par1Boolean){ getEntityData().set(NOSE, par1Boolean); }
    
    public boolean getEars(){ return getEntityData().get(EARS).booleanValue(); }
    public void setEars(boolean par1Boolean){ getEntityData().set(EARS, par1Boolean); }
    
    public boolean isViolent(){ return getEntityData().get(VIOLENT).booleanValue(); }
    public void setViolent(boolean par1Boolean){ getEntityData().set(VIOLENT, par1Boolean); }
    
    public int getColor(){ return ((Integer)getEntityData().get(COLOR)).intValue(); }
    public void setColor(int par1Int){ getEntityData().set(COLOR, Integer.valueOf(par1Int % 3)); }
	
	public GoblinType getGoblinType(){ return this.goblinType; }
	
	public float getGrowth(){ return 0.2F; }
	
	public boolean isInLove(){ return getEntityData().get(IN_LOVE).booleanValue(); }
	public void setInLove(boolean par1Bool){ getEntityData().set(IN_LOVE, par1Bool); }
	
	public boolean isCarrying(){ return !getEntityData().get(CARRYING).isEmpty(); }
	public EntityGoblin getOtherParent()
	{
		EntityGoblin child = VOEntities.GOBLIN.create(getLevel());
		child.readAdditional(getEntityData().get(CARRYING));
		return child;
	}
	public void setCarryingFrom(EntityGoblin parent)
	{
		if(parent == null)
			getEntityData().set(CARRYING, new CompoundTag());
		else
		{
			CompoundTag parentData = new CompoundTag();
			parent.writeAdditional(parentData);
			getEntityData().set(CARRYING, parentData);
		}
	}
	
	public BlockPos getNestSite(){ return getEntityData().get(NEST).orElse((BlockPos)null); }
	public void setNestSite(BlockPos pos){ getEntityData().set(NEST, Optional.of(pos)); }
	
    /**
     * Returns the progression of this mob towards adulthood.
     * Used to rescale the mob as it develops.
     * @return A float value between 0 (adult) and 1 (newborn)
     */
	public float getAgeProgress(){ return 0F; }
	
	public void writeAdditional(CompoundTag compound)
	{
		super.writeAdditional(compound);
		CompoundTag displayData = new CompoundTag();
			displayData.putInt("Color", getColor());
			displayData.putBoolean("Nose", getNose());
			displayData.putBoolean("Ears", getEars());
		compound.put("Display", displayData);
		compound.putBoolean("Violent", isViolent());
		compound.putInt("Type", getGoblinType().ordinal());
		compound.put("Mate", getEntityData().get(CARRYING));
		if(getNestSite() != null)
			compound.put("Nest", NbtUtils.writeBlockPos(getNestSite()));
	}
	
	public void readAdditionalSaveData(CompoundTag compound)
	{
		super.readAdditionalSaveData(compound);
		if(compound.contains("Display", 10))
		{
			CompoundTag displayData = compound.getCompound("Display");
			getEntityData().set(COLOR, displayData.getInt("Color"));
			setNose(displayData.getBoolean("Nose"));
			setEars(displayData.getBoolean("Ears"));
		}
		setViolent(compound.getBoolean("Violent"));
		this.goblinType = GoblinType.get(compound.getInt("Type"));
		getEntityData().set(CARRYING, compound.getCompound("Mate"));
		if(compound.contains("Nest", 10))
			setNestSite(NbtUtils.readBlockPos(compound.getCompound("Nest")));
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
	
    protected float getSoundVolume(){ return this.isBaby() ? super.getSoundVolume() * 0.3F : super.getSoundVolume(); }
    
    public void livingTick()
    {
    	/** If goblin has just become an adult OR is pregnant, set growing age to max */
    	boolean child = isBaby();
    	super.livingTick();
    	if(isCarrying() || isBaby() != child)
    		setAge(72000);
    	
    	if(isBaby())
    	{
    		if(isViolent() && getRandom().nextInt(Reference.Values.TICKS_PER_MINUTE) == 0)
    			setViolent(false);
    	}
    }
	
	public AgeableMob getBreedOffspring(ServerLevel worldIn, AgeableMob parent)
	{
		EntityGoblin child = VOEntities.GOBLIN.create(worldIn);
		
		if(parent.getType() == VOEntities.GOBLIN)
		{
			EntityGoblin partner = (EntityGoblin)parent;
			child.setColor(random.nextBoolean() ? partner.getColor() : getColor());
			child.setEars(random.nextBoolean() ? partner.getEars() : getEars());
			child.setNose(random.nextBoolean() ? partner.getNose() : getNose());
			
			child.setViolent(false);
		}
		
		return child;
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
					group = new EntityGroupGoblin();
					group.addMember(this);
					group.addTarget(entitylivingbaseIn);
					GroupHandler.addGroup(group);
				}
			}
		}
	}
	
	public boolean isNoDespawnRequired(){ return true; }
    
    @Nullable
    public ILivingEntityData onInitialSpawn(ServerLevel worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundTag dataTag)
    {
    	setAge(Reference.Values.TICKS_PER_DAY + getRandom().nextInt(Reference.Values.TICKS_PER_DAY * 2));
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
		
		public static GoblinType getRandomType(RandomSource random)
		{
			return BASIC;
//			List<GoblinType> seed = new ArrayList<GoblinType>();
//			for(GoblinType type : values()) for(int i=0; i<type.spawnWeight; i++) seed.add(type);
//			return seed.get(random.nextInt(seed.size()));
		}
	}
}
