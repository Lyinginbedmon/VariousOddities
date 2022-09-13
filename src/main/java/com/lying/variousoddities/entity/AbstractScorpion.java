package com.lying.variousoddities.entity;

import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.lying.variousoddities.client.renderer.entity.EntityScorpionRenderer;
import com.lying.variousoddities.utility.DataHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractScorpion extends EntityOddityAgeable
{
    private static final EntityDataAccessor<Integer> BREED	= SynchedEntityData.defineId(AbstractScorpion.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte>	BABIES	= SynchedEntityData.defineId(AbstractScorpion.class, EntityDataSerializers.BYTE);
	
	protected AbstractScorpion(EntityType<? extends AbstractScorpion> type, Level worldIn)
	{
		super(type, worldIn);
	}
	
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		DataHelper.Booleans.registerBooleanByte(getEntityData(), BABIES, false);
		getEntityData().define(BREED, 0);
	}
	
	public MobType getCreatureAttribute(){ return MobType.ARTHROPOD; }
	
	protected void registerGoals()
	{
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		
	    this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}
	
	public AgeableMob getBreedOffspring(ServerLevel worldIn, AgeableMob p_241840_2_)
	{
		return createBaby(worldIn);
	}
	
	public boolean checkSpawnRules(LevelAccessor world, MobSpawnType reason)
    {
    	return world.getLightEmission(blockPosition()) < 8 && world.getHeight(Heightmap.Types.WORLD_SURFACE, blockPosition().getX(), blockPosition().getZ()) <= blockPosition().getY() && super.checkSpawnRules(world, reason);
    }
	
    public EnumScorpionType getScorpionType(){ return EnumScorpionType.values()[getBreed()]; }
    public int getBreed(){ return ((Integer)getEntityData().get(BREED)).intValue(); }
    public void setBreed(EnumScorpionType type){ setBreed(type.ordinal()); }
    public void setBreed(int par1Int){ getEntityData().set(BREED, Integer.valueOf(par1Int % 4)); }
    
    public boolean getBabies(){ return DataHelper.Booleans.getBooleanByte(getEntityData(), BABIES); }
    public void setBabies(boolean par1Bool){ DataHelper.Booleans.setBooleanByte(getEntityData(), par1Bool, BABIES); }
    public abstract AbstractScorpion createBaby(Level worldIn);
    
    public void addAdditionalSaveData(CompoundTag compound)
    {
    	super.addAdditionalSaveData(compound);
    	CompoundTag display = new CompoundTag();
    		display.putBoolean("Babies", getBabies());
    		display.putInt("Breed", getBreed());
    	compound.put("Display", display);
    }
    
    public void readAdditionalSaveData(CompoundTag compound)
    {
    	super.readAdditionalSaveData(compound);
    	CompoundTag display = compound.getCompound("Display");
    	setBabies(display.getBoolean("Babies"));
    	setBreed(display.getInt("Breed"));
    }
    
	public boolean doHurtTarget(Entity entityIn)
	{
		if(super.doHurtTarget(entityIn))
		{
	    	if(entityIn instanceof LivingEntity && !this.isBaby() && this.getRandom().nextInt(3) == 0)
	    		this.getScorpionType().apply((LivingEntity)entityIn);
			
			return true;
		}
		return false;
	}
    
    /**
     * Called when the mob's health reaches 0.
     */
    public void die(DamageSource cause)
    {
    	super.die(cause);
    	if(!this.level.isClientSide && this.getBabies() && !this.isBaby())
    	{
    		for(int i=0; i<this.getRandom().nextInt(5); i++)
    		{
    			AbstractScorpion baby = createBaby(this.level);
    			baby.setAge(-2400 + this.getRandom().nextInt(50));
    			
    			Vec3 position = position();
    			double posX = position.x + (this.getRandom().nextDouble() - 0.5D);
    			double posZ = position.z + (this.getRandom().nextDouble() - 0.5D);
    			baby.absMoveTo(posX, position.y + 0.5F, posZ, 360.0F*this.getRandom().nextFloat(), 0F);
    			this.level.addFreshEntity(baby);
    		}
    	}
    }
	
    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.SILVERFISH_AMBIENT;
    }
    
    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return SoundEvents.SILVERFISH_HURT;
    }
    
    protected SoundEvent getDeathSound()
    {
        return SoundEvents.SILVERFISH_DEATH;
    }
    
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag)
    {
		Random rand = new Random(this.getUUID().getLeastSignificantBits());
		DataHelper.Booleans.setBooleanByte(getEntityData(), rand.nextInt(8) == 0, BABIES);
		getEntityData().set(BREED, rand.nextInt(4) == 0 ? 1 : 0);
		
		return spawnDataIn;
    }
	
	public enum EnumScorpionType
	{
		RED("red", new Predicate<LivingEntity>()
			{
				public boolean apply(LivingEntity input)
				{
					input.addEffect(new MobEffectInstance(MobEffects.POISON, 280, 0));
					return input.getEffect(MobEffects.POISON) != null;
				}
			}),
		BLACK("black", new Predicate<LivingEntity>()
		{
			public boolean apply(LivingEntity input)
			{
				input.addEffect(new MobEffectInstance(MobEffects.WITHER, 280, 0));
				return input.getEffect(MobEffects.WITHER) != null;
			}
		}),
		BASE("base", new Predicate<LivingEntity>()
		{
			public boolean apply(LivingEntity input)
			{
				return true;
			}
		});
		
		private final String name;
		private final Predicate<LivingEntity> attackEffect;
		
		private EnumScorpionType(String nameIn, Predicate<LivingEntity> effectIn)
		{
			name = nameIn;
			attackEffect = effectIn;
		}
		
		@OnlyIn(Dist.CLIENT)
		public ResourceLocation getTexture()
		{
			return new ResourceLocation(EntityScorpionRenderer.resourceBase+name+".png");
		}
		
		public boolean apply(LivingEntity input)
		{
			return attackEffect.apply(input);
		}
	}
}
