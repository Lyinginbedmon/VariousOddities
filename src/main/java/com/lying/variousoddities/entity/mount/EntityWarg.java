package com.lying.variousoddities.entity.mount;

import javax.annotation.Nullable;

import com.lying.variousoddities.entity.AbstractGoblinWolf;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.IRideable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntityWarg extends AbstractGoblinWolf implements IRideable, IJumpingMount
{
	private static final DataParameter<Boolean> REARING	= EntityDataManager.<Boolean>createKey(EntityWarg.class, DataSerializers.BOOLEAN);
	
	private float jumpPower = 0F;
	private boolean allowStandSliding;
	
	private boolean wargJumping;
	private int jumpRearingCounter;
	private float rearingAmount;
	@SuppressWarnings("unused")
	private float prevRearingAmount;
	
	public EntityWarg(EntityType<? extends EntityWarg> type, World worldIn)
	{
		super(type, worldIn);
	    this.stepHeight = 1.0F;
	}
	
	protected void registerData()
	{
		super.registerData();
		getDataManager().register(REARING, false);
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return MobEntity.func_233666_p_()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 40.0D)
        		.createMutableAttribute(Attributes.ARMOR, 7.0D)
        		.createMutableAttribute(Attributes.MOVEMENT_SPEED, (double)0.3002F)
        		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 12.0D);
    }
	
	public void getAggressiveBehaviours()
	{
		this.addGeneticAI(3, new NearestAttackableTargetGoal<CowEntity>(this, CowEntity.class, true));
		this.addGeneticAI(3, new NearestAttackableTargetGoal<PigEntity>(this, PigEntity.class, true));
		this.addGeneticAI(3, new NearestAttackableTargetGoal<LlamaEntity>(this, LlamaEntity.class, true));
		this.addGeneticAI(3, new NearestAttackableTargetGoal<SheepEntity>(this, SheepEntity.class, true));
	}
	
	public AgeableEntity func_241840_a(ServerWorld arg0, AgeableEntity arg1)
	{
		return null;
	}
	
	public boolean isJumping()
	{
		return this.wargJumping;
	}
	
	public void setJumping(boolean jumping)
	{
		this.wargJumping = jumping;
	}
	
	public boolean isRearing()
	{
		return getDataManager().get(REARING).booleanValue();
	}
	
	public void setRearing(boolean rearing)
	{
		getDataManager().set(REARING, rearing);
	}
	
	private void makeWargRear()
	{
		if(canPassengerSteer() || this.isServerWorld())
		{
			this.jumpRearingCounter = 1;
			this.setRearing(true);
		}
	}
	
	public boolean boost()
	{
		return false;
	}
	
	public void travelTowards(Vector3d travelVec)
	{
		super.travel(travelVec);
	}
	
	public boolean isRiderControlling()
	{
		if(getControllingPassenger() != null)
		{
			LivingEntity rider = (LivingEntity)getControllingPassenger();
			return Math.abs(rider.moveForward) > 0F || Math.abs(rider.moveStrafing) > 0F || this.jumpPower > 0F;
		}
		return false;
	}
	
	public void travel(Vector3d travelVector)
	{
		if(!this.isAlive())
			return;
		
		if(isBeingRidden() && canBeSteered())
		{
			LivingEntity entity = (LivingEntity)getControllingPassenger();
			
			// TODO Determine values to base motion on in lieu of direct rider control
			float forward = 0F;
			float strafe = 0F;
			
			if(isRiderControlling())
			{
				forward = entity.moveForward;
				strafe = entity.moveStrafing * 0.5F;
			}
			
			this.rotationYaw = entity.rotationYaw;
			this.prevRotationYaw = this.rotationYaw;
			this.rotationPitch = entity.rotationPitch * 0.5F;
			this.setRotation(this.rotationYaw, this.rotationPitch);
			this.renderYawOffset = this.rotationYaw;
			this.rotationYawHead = this.renderYawOffset;
			
			if(this.onGround && this.jumpPower == 0F && this.isRearing() && !this.allowStandSliding)
				strafe = forward = 0F;
			
			if(this.jumpPower > 0F && !this.isJumping() && this.onGround)
			{
				double jump = this.jumpPower * this.getJumpFactor();
				double boost;
				if(isPotionActive(Effects.JUMP_BOOST))
					boost = jump + (double)((float)(getActivePotionEffect(Effects.JUMP_BOOST).getAmplifier() + 1) * 0.1F);
				else
					boost = jump;
				
				Vector3d motion = this.getMotion();
				this.setMotion(motion.x, boost, motion.z);
				this.setJumping(true);
				this.isAirBorne = true;
				net.minecraftforge.common.ForgeHooks.onLivingJump(this);
				if(forward > 0F || !isRiderControlling())
				{
					float sine = MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F));
					float cosine = MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F));
					this.setMotion(this.getMotion().add(-0.4F * sine * this.jumpPower, 0D, 0.4F * cosine * this.jumpPower));
				}
				
				this.jumpPower = 0F;
			}
			
			this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
			if(canPassengerSteer())
			{
				this.setAIMoveSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
				super.travel(new Vector3d(strafe, travelVector.y, forward));
			}
			
			if(this.onGround)
			{
				this.jumpPower = 0F;
				this.setJumping(false);
			}
			this.func_233629_a_(this, false);
			return;
		}
		
		this.jumpMovementFactor = 0.02F;
		super.travel(travelVector);
	}
	
	public float getMountedSpeed()
	{
		return (float)getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.225F;
	}
	
	public ActionResultType func_230254_b_(PlayerEntity player, Hand hand)
	{
		if(!isBeingRidden() && !player.isSecondaryUseActive())
		{
			if(!this.getEntityWorld().isRemote)
				player.startRiding(this);
			return ActionResultType.func_233537_a_(this.getEntityWorld().isRemote);
		}
		
		return super.func_230254_b_(player, hand);
	}
	
	@Nullable
	public Entity getControllingPassenger()
	{
		return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
	}
	
	public boolean canBeSteered()
	{
		return getControllingPassenger() != null && getControllingPassenger().isAlive();
	}
	
	public void tick()
	{
		super.tick();
		
		if((canPassengerSteer() || this.isServerWorld()) && this.jumpRearingCounter > 0 && ++this.jumpRearingCounter > 20)
		{
			this.jumpRearingCounter = 0;
			this.setRearing(false);
		}
		
		this.prevRearingAmount = this.rearingAmount;
		if(this.isRearing())
		{
			this.rearingAmount += (1F - this.rearingAmount) * 0.4F + 0.05F;
			if(this.rearingAmount > 1F)
				this.rearingAmount = 1F;
		}
		else
		{
			this.allowStandSliding = false;
			this.rearingAmount += (0.8F * this.rearingAmount * this.rearingAmount * this.rearingAmount - this.rearingAmount) * 0.6F - 0.5F;
			if(this.rearingAmount < 0F)
				this.rearingAmount = 0F;
		}
	}
	
	public void setJumpPower(int jumpPowerIn)
	{
		if(this.canPassengerSteer())
		{
			if(jumpPowerIn < 0)
				jumpPowerIn = 0;
			else
			{
				this.allowStandSliding = true;
				this.makeWargRear();
			}
			
			if(jumpPowerIn >= 90)
				this.jumpPower = 1F;
			else
				this.jumpPower = 0.4F + 0.4F * (float)jumpPowerIn / 90F;
		}
	}
	
	public boolean canJump()
	{
		return canPassengerSteer();
	}
	
	public void handleStartJump(int jumpPower)
	{
		this.allowStandSliding = true;
		this.makeWargRear();
	}
	
	public void handleStopJump()
	{
		
	}
}
