package com.lying.variousoddities.entity.passive;

import java.util.Set;

import com.google.common.collect.Sets;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.EntityOddity;
import com.lying.variousoddities.entity.ai.MovementControllerGhastling;
import com.lying.variousoddities.entity.ai.passive.EntityAIGhastlingFireball;
import com.lying.variousoddities.entity.ai.passive.EntityAIGhastlingWander;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LandOnOwnersShoulderGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityGhastling extends ShoulderRidingEntity implements FlyingAnimal
{
	private static final EntityDataAccessor<Integer> EMOTION = SynchedEntityData.defineId(EntityGhastling.class, EntityDataSerializers.INT);
	private static final Set<Item> TAME_ITEMS = Sets.newHashSet(Items.SUGAR, Items.COOKIE, Items.HONEYCOMB, Items.HONEY_BOTTLE, Items.SWEET_BERRIES);
	
	public EntityGhastling(EntityType<? extends EntityGhastling> type, Level worldIn)
	{
		super(type, worldIn);
		this.moveControl = new MovementControllerGhastling(this);
	    setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
	    setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
	}
	
	public void defineSynchedData()
	{
		super.defineSynchedData();
		getEntityData().define(EMOTION, 0);
	}
	
	public void registerGoals()
	{
	    this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
		this.goalSelector.addGoal(5, new EntityAIGhastlingFireball(this));
		this.goalSelector.addGoal(7, new LandOnOwnersShoulderGoal(this));
		this.goalSelector.addGoal(8, new EntityAIGhastlingWander(this, 0.1F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
		
		if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(VOEntities.GHASTLING.get()))
		{
			this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
			this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
			this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
		}
	}

    public static AttributeSupplier.Builder createAttributes()
    {
        return EntityOddity.createAttributes()
        		.add(Attributes.MAX_HEALTH, 10D)
        		.add(Attributes.MOVEMENT_SPEED, (double)0.05F)
        		.add(Attributes.FLYING_SPEED, (double)0.25F);
    }
	
    public boolean checkSpawnRules(LevelAccessor world, MobSpawnType reason)
	{
		return random.nextInt(20) == 0 && super.checkSpawnRules(world, reason);
	}
	
	public boolean isPersistenceRequired(){ return this.isTame() || super.isPersistenceRequired(); }
	
	public SoundSource getSoundCategory()
	{
		return SoundSource.NEUTRAL;
	}
	
	public SoundEvent getAmbientSound(){ return SoundEvents.GHAST_AMBIENT; }
	public SoundEvent getHurtSound(DamageSource damageSourceIn){ return SoundEvents.GHAST_HURT; }
	public SoundEvent getDeathSound(){ return SoundEvents.GHAST_DEATH; }
	
	public float getSoundVolume(){ return 0.25F; }
	
	public boolean isChild(){ return true; }
	
	public boolean onLivingFall(float distance, float damageMultiplier){ return false; }
	protected void updateFallState(double y, boolean onGroundIn, BlockState state, BlockPos pos){ }
	
	public boolean isImmuneToFire(){ return true; }
	
	public boolean hurt(DamageSource source, float amount)
	{
		if(source.isFire() || source.isExplosion())
			return false;
		return super.hurt(source, amount);
	}
	
	public InteractionResult mobInteract(Player player, InteractionHand hand)
	{
		ItemStack heldStack = player.getItemInHand(hand);
		if(!this.isTame())
		{
			if(TAME_ITEMS.contains(heldStack.getItem()))
			{
				if(!player.isCreative())
					heldStack.shrink(1);
				
				if(!this.getLevel().isClientSide)
				{
					if(this.getRandom().nextInt(10) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player))
					{
						this.tame(player);
						this.getLevel().broadcastEntityEvent(this, (byte)7);
						this.setEmotion(Emotion.HAPPY);
					}
					else
						this.getLevel().broadcastEntityEvent(this, (byte)6);
				}
				return InteractionResult.sidedSuccess(this.getLevel().isClientSide);
			}
		}
		else if(this.isTame())
		{
			if(getHealth() < getMaxHealth() && TAME_ITEMS.contains(heldStack.getItem()))
			{
				if(!player.isCreative())
					heldStack.shrink(1);
				
				this.heal(1F + getRandom().nextFloat() * 3F);
				this.setEmotion(Emotion.HAPPY);
				this.getLevel().broadcastEntityEvent(this, (byte)7);
			}
			else if(this.isOwnedBy(player))
			{
				if(!this.getLevel().isClientSide)
					this.setOrderedToSit(!this.isOrderedToSit());
				return InteractionResult.sidedSuccess(this.getLevel().isClientSide);
			}
		}
		
		return super.mobInteract(player, hand);
	}
	
	public AgeableMob getBreedOffspring(ServerLevel p_241840_1_, AgeableMob p_241840_2_)
	{
		return null;
	}
	
	public boolean shouldAttackEntity(LivingEntity target, LivingEntity owner)
	{
		if(isTame() && target == owner)
			return false;
		
		if(!(target instanceof Creeper) && !(target instanceof Ghast))
			if(target instanceof Wolf)
			{
				Wolf wolfentity = (Wolf)target;
				return !wolfentity.isTame() || wolfentity.getOwner() != owner;
			}
			else if(target instanceof Player && owner instanceof Player && !((Player)owner).canAttack((Player)target))
				return false;
			else if (target instanceof AbstractHorse && ((AbstractHorse)target).isTamed())
				return false;
			else
				return !(target instanceof TamableAnimal) || !((TamableAnimal)target).isTame();
		return false;
	}
	
	public void aiStep()
	{
		super.aiStep();
		
		if(getEmotion() == Emotion.SLEEP)
		{
			if(onGround)
				setPose(Pose.SLEEPING);
			else
				setPose(Pose.STANDING);
		}
		else
			setPose(getEmotion().pose());
		
		switch(getEmotion())
		{
			case HAPPY:	// Be happy for roughly 3 seconds for each happiness-inducing event
				if(getRandom().nextInt(60) == 0)
					setEmotion(Emotion.NEUTRAL);
				break;
			case NEUTRAL:	// Main default emotion
				if(this.isOrderedToSit())
					setEmotion(Emotion.SLEEP);
				else if(getHealth() < getMaxHealth() * 0.3F)
					setEmotion(Emotion.SAD);
				else if(getHealth() > getMaxHealth() * 0.75F && getRandom().nextInt(600) == 0)
					setEmotion(Emotion.HAPPY);
				break;
			case SAD:	// Be sad when health is below 1/3rd of max
				if(getHealth() > getMaxHealth() * 0.3F)
					setEmotion(Emotion.NEUTRAL);
				break;
			case SLEEP:
				if(!isOrderedToSit())
					setEmotion(Emotion.NEUTRAL);
			default:
				break;
		}
	}
	
	public void travel(Vec3 travelVector)
	{
		if(this.isInWater())
		{
			this.moveRelative(0.02F, travelVector);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale((double)0.8F));
		}
		else if(this.isInLava())
		{
			this.moveRelative(0.02F, travelVector);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
		}
		else
		{
			BlockPos ground = new BlockPos(this.getX(), this.getY() - 1.0D, this.getZ());
			float f = 0.91F;
			if (this.onGround)
			f = this.level.getBlockState(ground).getFriction(this.level, ground, this) * 0.91F;
			
			float f1 = 0.16277137F / (f * f * f);
			f = 0.91F;
			if (this.onGround)
				f = this.level.getBlockState(ground).getFriction(this.level, ground, this) * 0.91F;
			
			this.moveRelative(this.onGround ? 0.1F * f1 : 0.02F, travelVector);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale((double)f));
		}
		
		this.calculateEntityAnimation(this, false);
	}
	
	public Emotion getEmotion(){ return Emotion.fromInt(getEntityData().get(EMOTION).intValue()); }
	public void setEmotion(Emotion emote)
	{
		getEntityData().set(EMOTION, emote.ordinal());
		setPose(emote.pose());
	}
	
	public static enum Emotion
	{
		NEUTRAL,
		HAPPY,
		SAD,
		SLEEP,
		ANGRY,
		ANGRY2;
		
		public static Emotion fromInt(int par1Int)
		{
			return values()[Math.abs(par1Int) % values().length];
		}
		
		public Pose pose(){ return this == SLEEP ? Pose.SLEEPING : Pose.STANDING; }
		
		@OnlyIn(Dist.CLIENT)
		public ResourceLocation texture(){ return new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/ghastling/ghastling_"+name().toLowerCase()+".png"); }
	}
	
	public boolean isFlying() { return true; }
}
