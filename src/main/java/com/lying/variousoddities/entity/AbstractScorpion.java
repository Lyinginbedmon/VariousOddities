package com.lying.variousoddities.entity;

import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.lying.variousoddities.client.renderer.entity.EntityScorpionRenderer;
import com.lying.variousoddities.utility.DataHelper;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractScorpion extends EntityOddityAgeable
{
    private static final DataParameter<Integer> BREED	= EntityDataManager.<Integer>createKey(AbstractScorpion.class, DataSerializers.VARINT);
    private static final DataParameter<Byte>	BABIES	= EntityDataManager.<Byte>createKey(AbstractScorpion.class, DataSerializers.BYTE);
	
	protected AbstractScorpion(EntityType<? extends AbstractScorpion> type, World worldIn)
	{
		super(type, worldIn);
	}
	
	protected void registerData()
	{
		super.registerData();
		DataHelper.Booleans.registerBooleanByte(getDataManager(), BABIES, false);
		getDataManager().register(BREED, 0);
	}
	
	public CreatureAttribute getCreatureAttribute(){ return CreatureAttribute.ARTHROPOD; }
	
	protected void registerGoals()
	{
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
		
	    this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}
	
	public AgeableEntity func_241840_a(ServerWorld worldIn, AgeableEntity p_241840_2_)
	{
		return createBaby(worldIn);
	}
	
    public static boolean canSpawnAt(EntityType<?> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
    	return world.getLight(pos) <= 8 && world.getHeight(Heightmap.Type.WORLD_SURFACE, pos).getY() <= pos.getY();
    }
	
    public EnumScorpionType getScorpionType(){ return EnumScorpionType.values()[getBreed()]; }
    public int getBreed(){ return ((Integer)getDataManager().get(BREED)).intValue(); }
    public void setBreed(EnumScorpionType type){ setBreed(type.ordinal()); }
    public void setBreed(int par1Int){ getDataManager().set(BREED, Integer.valueOf(par1Int % 4)); }
    
    public boolean getBabies(){ return DataHelper.Booleans.getBooleanByte(getDataManager(), BABIES); }
    public void setBabies(boolean par1Bool){ DataHelper.Booleans.setBooleanByte(getDataManager(), par1Bool, BABIES); }
    public abstract AbstractScorpion createBaby(World worldIn);
    
    public void writeAdditional(CompoundNBT compound)
    {
    	super.writeAdditional(compound);
    	CompoundNBT display = new CompoundNBT();
    		display.putBoolean("Babies", getBabies());
    		display.putInt("Breed", getBreed());
    	compound.put("Display", display);
    }
    
    public void readAdditional(CompoundNBT compound)
    {
    	super.readAdditional(compound);
    	CompoundNBT display = compound.getCompound("Display");
    	setBabies(display.getBoolean("Babies"));
    	setBreed(display.getInt("Breed"));
    }
    
	public boolean attackEntityAsMob(Entity entityIn)
	{
		if(super.attackEntityAsMob(entityIn))
		{
	    	if(entityIn instanceof LivingEntity && !this.isChild() && this.getRNG().nextInt(3) == 0)
	    		this.getScorpionType().apply((LivingEntity)entityIn);
			
			return true;
		}
		return false;
	}
    
    /**
     * Called when the mob's health reaches 0.
     */
    public void onDeath(DamageSource cause)
    {
    	super.onDeath(cause);
    	if(!this.world.isRemote && this.getBabies() && !this.isChild())
    	{
    		for(int i=0; i<this.getRNG().nextInt(5); i++)
    		{
    			AbstractScorpion baby = createBaby(this.world);
    			baby.setGrowingAge(-2400 + this.getRNG().nextInt(50));
    			
    			Vector3d position = getPositionVec();
    			double posX = position.x + (this.getRNG().nextDouble() - 0.5D);
    			double posZ = position.z + (this.getRNG().nextDouble() - 0.5D);
    			baby.setPositionAndRotation(posX, position.y + 0.5F, posZ, 360.0F*this.getRNG().nextFloat(), 0F);
    			this.world.addEntity(baby);
    		}
    	}
    }
	
    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.ENTITY_SILVERFISH_AMBIENT;
    }
    
    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return SoundEvents.ENTITY_SILVERFISH_HURT;
    }
    
    protected SoundEvent getDeathSound()
    {
        return SoundEvents.ENTITY_SILVERFISH_DEATH;
    }
    
    @Nullable
    public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag)
    {
		Random rand = new Random(this.getUniqueID().getLeastSignificantBits());
		DataHelper.Booleans.setBooleanByte(getDataManager(), rand.nextInt(8) == 0, BABIES);
		getDataManager().set(BREED, rand.nextInt(4) == 0 ? 1 : 0);
		
		return spawnDataIn;
    }
	
	public enum EnumScorpionType
	{
		RED("red", new Predicate<LivingEntity>()
			{
				public boolean apply(LivingEntity input)
				{
					input.addPotionEffect(new EffectInstance(Effects.POISON, 280, 0));
					return input.getActivePotionEffect(Effects.POISON) != null;
				}
			}),
		BLACK("black", new Predicate<LivingEntity>()
		{
			public boolean apply(LivingEntity input)
			{
				input.addPotionEffect(new EffectInstance(Effects.WITHER, 280, 0));
				return input.getActivePotionEffect(Effects.WITHER) != null;
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
