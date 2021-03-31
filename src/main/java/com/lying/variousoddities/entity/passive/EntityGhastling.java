package com.lying.variousoddities.entity.passive;

import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.EntityOddity;
import com.lying.variousoddities.entity.ai.MovementControllerGhastling;
import com.lying.variousoddities.entity.ai.passive.EntityAIGhastlingFireball;
import com.lying.variousoddities.entity.ai.passive.EntityAIGhastlingWander;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LandOnOwnersShoulderGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.OwnerHurtByTargetGoal;
import net.minecraft.entity.ai.goal.OwnerHurtTargetGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.GhastEntity;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityGhastling extends ShoulderRidingEntity implements IFlyingAnimal
{
	private static final DataParameter<Integer> EMOTION = EntityDataManager.<Integer>createKey(EntityGhastling.class, DataSerializers.VARINT);
	private static final Set<Item> TAME_ITEMS = Sets.newHashSet(Items.SUGAR, Items.COOKIE, Items.HONEYCOMB, Items.HONEY_BOTTLE, Items.SWEET_BERRIES);
	
	public EntityGhastling(EntityType<? extends EntityGhastling> type, World worldIn)
	{
		super(type, worldIn);
		this.moveController = new MovementControllerGhastling(this);
	    this.setPathPriority(PathNodeType.DANGER_FIRE, 0.0F);
	    this.setPathPriority(PathNodeType.DAMAGE_FIRE, 0.0F);
	}
	
	public void registerData()
	{
		super.registerData();
		getDataManager().register(EMOTION, 0);
	}
	
	public void registerGoals()
	{
	    this.goalSelector.addGoal(2, new SitGoal(this));
		this.goalSelector.addGoal(5, new EntityAIGhastlingFireball(this));
		this.goalSelector.addGoal(7, new LandOnOwnersShoulderGoal(this));
		this.goalSelector.addGoal(8, new EntityAIGhastlingWander(this, 0.1F));
		this.goalSelector.addGoal(10, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.addGoal(10, new LookRandomlyGoal(this));
		
		if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(VOEntities.GHASTLING))
		{
			this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
			this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
			this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
		}
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return EntityOddity.getAttributes()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 10D)
        		.createMutableAttribute(Attributes.MOVEMENT_SPEED, (double)0.05F)
        		.createMutableAttribute(Attributes.FLYING_SPEED, (double)0.25F);
    }
	
	public static boolean canSpawnAt(EntityType<EntityGhastling> type, IWorld world, SpawnReason reason, BlockPos pos, Random rand)
	{
		return reason == SpawnReason.SPAWNER || rand.nextInt(20) == 0 && canSpawnOn(type, world, reason, pos, rand);
	}
	
	public boolean isNoDespawnRequired(){ return this.isTamed() || super.isNoDespawnRequired(); }
	
	public SoundCategory getSoundCategory()
	{
		return SoundCategory.NEUTRAL;
	}
	
	public SoundEvent getAmbientSound(){ return SoundEvents.ENTITY_GHAST_AMBIENT; }
	public SoundEvent getHurtSound(DamageSource damageSourceIn){ return SoundEvents.ENTITY_GHAST_HURT; }
	public SoundEvent getDeathSound(){ return SoundEvents.ENTITY_GHAST_DEATH; }
	
	public float getSoundVolume(){ return 0.25F; }
	
	public boolean isChild(){ return true; }
	
	public boolean onLivingFall(float distance, float damageMultiplier){ return false; }
	protected void updateFallState(double y, boolean onGroundIn, BlockState state, BlockPos pos){ }
	
	public boolean isImmuneToFire(){ return true; }
	
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		if(source.isFireDamage())
			return false;
		return super.attackEntityFrom(source, amount);
	}
	
	public ActionResultType func_230254_b_(PlayerEntity player, Hand hand)
	{
		ItemStack heldStack = player.getHeldItem(hand);
		if(!this.isTamed())
		{
			if(TAME_ITEMS.contains(heldStack.getItem()))
			{
				if(!player.abilities.isCreativeMode)
					heldStack.shrink(1);
				
				if(!this.getEntityWorld().isRemote)
				{
					if(this.getRNG().nextInt(10) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player))
					{
						this.setTamedBy(player);
						this.getEntityWorld().setEntityState(this, (byte)7);
						this.setEmotion(Emotion.HAPPY);
					}
					else
						this.getEntityWorld().setEntityState(this, (byte)6);
				}
				return ActionResultType.func_233537_a_(this.getEntityWorld().isRemote);
			}
		}
		else if(this.isTamed())
		{
			if(getHealth() < getMaxHealth() && TAME_ITEMS.contains(heldStack.getItem()))
			{
				if(!player.abilities.isCreativeMode)
					heldStack.shrink(1);
				
				this.heal(1F + getRNG().nextFloat() * 3F);
				this.setEmotion(Emotion.HAPPY);
				this.getEntityWorld().setEntityState(this, (byte)7);
			}
			else if(this.isOwner(player))
			{
				if(!this.getEntityWorld().isRemote)
					this.func_233687_w_(!this.isSitting());
				return ActionResultType.func_233537_a_(this.getEntityWorld().isRemote);
			}
		}
		
		return super.func_230254_b_(player, hand);
	}
	
	public AgeableEntity func_241840_a(ServerWorld p_241840_1_, AgeableEntity p_241840_2_)
	{
		return null;
	}
	
	public boolean shouldAttackEntity(LivingEntity target, LivingEntity owner)
	{
		if(isTamed() && target == owner)
			return false;
		
		if(!(target instanceof CreeperEntity) && !(target instanceof GhastEntity))
			if(target instanceof WolfEntity)
			{
				WolfEntity wolfentity = (WolfEntity)target;
				return !wolfentity.isTamed() || wolfentity.getOwner() != owner;
			}
			else if(target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity)owner).canAttackPlayer((PlayerEntity)target))
				return false;
			else if (target instanceof AbstractHorseEntity && ((AbstractHorseEntity)target).isTame())
				return false;
			else
				return !(target instanceof TameableEntity) || !((TameableEntity)target).isTamed();
		return false;
	}
	
	public void livingTick()
	{
		super.livingTick();
		
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
				if(getRNG().nextInt(60) == 0)
					setEmotion(Emotion.NEUTRAL);
				break;
			case NEUTRAL:	// Main default emotion
				if(this.isSitting())
					setEmotion(Emotion.SLEEP);
				else if(getHealth() < getMaxHealth() * 0.3F)
					setEmotion(Emotion.SAD);
				else if(getHealth() > getMaxHealth() * 0.75F && getRNG().nextInt(600) == 0)
					setEmotion(Emotion.HAPPY);
				break;
			case SAD:	// Be sad when health is below 1/3rd of max
				if(getHealth() > getMaxHealth() * 0.3F)
					setEmotion(Emotion.NEUTRAL);
				break;
			case SLEEP:
				if(!isSitting())
					setEmotion(Emotion.NEUTRAL);
			default:
				break;
		}
	}
	
	public void travel(Vector3d travelVector)
	{
		if(this.isInWater())
		{
			this.moveRelative(0.02F, travelVector);
			this.move(MoverType.SELF, this.getMotion());
			this.setMotion(this.getMotion().scale((double)0.8F));
		}
		else if(this.isInLava())
		{
			this.moveRelative(0.02F, travelVector);
			this.move(MoverType.SELF, this.getMotion());
			this.setMotion(this.getMotion().scale(0.5D));
		}
		else
		{
			BlockPos ground = new BlockPos(this.getPosX(), this.getPosY() - 1.0D, this.getPosZ());
			float f = 0.91F;
			if (this.onGround)
			f = this.world.getBlockState(ground).getSlipperiness(this.world, ground, this) * 0.91F;
			
			float f1 = 0.16277137F / (f * f * f);
			f = 0.91F;
			if (this.onGround)
				f = this.world.getBlockState(ground).getSlipperiness(this.world, ground, this) * 0.91F;
			
			this.moveRelative(this.onGround ? 0.1F * f1 : 0.02F, travelVector);
			this.move(MoverType.SELF, this.getMotion());
			this.setMotion(this.getMotion().scale((double)f));
		}
		
		this.func_233629_a_(this, false);
	}
	
	public Emotion getEmotion(){ return Emotion.fromInt(getDataManager().get(EMOTION).intValue()); }
	public void setEmotion(Emotion emote)
	{
		getDataManager().set(EMOTION, emote.ordinal());
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
}
