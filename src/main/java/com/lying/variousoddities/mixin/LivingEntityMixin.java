package com.lying.variousoddities.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Maps;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.PlayerData.BodyCondition;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityClimb;
import com.lying.variousoddities.species.abilities.AbilityDarkvision;
import com.lying.variousoddities.species.abilities.AbilityHurtByEnv;
import com.lying.variousoddities.species.abilities.AbilityHurtByEnv.EnvType;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.AbilityStatusEffect;
import com.lying.variousoddities.species.abilities.AbilityStatusImmunity;
import com.lying.variousoddities.species.abilities.IPhasingAbility;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

@Mixin(LivingEntity.class)
public class LivingEntityMixin extends EntityMixin
{
	@Shadow
	public Map<Effect, EffectInstance> activePotionsMap = Maps.newHashMap();
	
	@Shadow
	public int idleTime = 0;
	
	@Shadow
	public float moveStrafing = 0F;
	
	@Shadow
	public float moveForward = 0F;
	
	@Shadow
	public boolean isJumping;
	
	@Shadow
	public float getHealth(){ return 0F; }
	
	@Shadow
	public float getMaxHealth(){ return 0F; }
	
	@Shadow
	public boolean isElytraFlying(){ return false; }
	
	@Inject(method = "updatePotionEffects()V", at = @At("HEAD"))
	public void updatePotionEffects(final CallbackInfo ci)
	{
		LivingEntity living = (LivingEntity)(Object)this;
		if(!living.getEntityWorld().isRemote)
		{
			LivingData livingData = LivingData.forEntity(living);
			if(livingData == null)
				return;
			
			for(Effect visual : VOPotions.VISUALS.keySet())
			{
				int index = VOPotions.getVisualPotionIndex(visual);
				boolean active = living.isPotionActive(visual);
				
				if(livingData.getVisualPotion(index) != active)
					livingData.setVisualPotion(index, active);
			}
			
			for(AbilityStatusEffect effectAbility : AbilityRegistry.getAbilitiesOfType(living, AbilityStatusEffect.class))
				effectAbility.tick(living);
		}
	}
	
	@Inject(method = "baseTick()V", at = @At("TAIL"))
	public void baseTick(final CallbackInfo ci)
	{
		LivingData livingData = LivingData.forEntity((LivingEntity)(Object)this);
		if(livingData == null)
			return;
		
		livingData.tick((LivingEntity)(Object)this);
		if(livingData.overrideAir())
			this.setAir(livingData.getAir());
	}
	
	@Inject(method = "livingTick()V", at = @At("HEAD"), cancellable = true)
	public void livingTick(final CallbackInfo ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		LivingData livingData = LivingData.forEntity(entity);
		if(livingData != null && livingData.isBeingPossessed())
		{
			if(!entity.isElytraFlying() && !entity.isSwimming() && isPoseClear(Pose.CROUCHING) && (entity.isSneaking() || !entity.isSleeping() && !isPoseClear(Pose.STANDING)))
				setPose(Pose.CROUCHING);
		}
	}
	
	@Inject(method = "isSleeping()V", at = @At("HEAD"), cancellable = true)
	public void isSleeping(final CallbackInfoReturnable<Boolean> ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		if(entity.getType() == EntityType.PLAYER)
		{
			PlayerEntity player = (PlayerEntity)entity;
			if(PlayerData.forPlayer(player).getBodyCondition() == BodyCondition.UNCONSCIOUS)
				ci.setReturnValue(true);
		}
	}
	
	@Inject(method = "isPotionApplicable", at = @At("HEAD"), cancellable = true)
	public void isPotionApplicable(EffectInstance effectInstanceIn, final CallbackInfoReturnable<Boolean> ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		for(AbilityStatusImmunity statusImmunity : AbilityRegistry.getAbilitiesOfType(entity, AbilityStatusImmunity.class))
			if(statusImmunity.appliesToStatus(effectInstanceIn))
			{
				ci.setReturnValue(false);
				break;
			}
	}
	
	@Inject(method = "isPotionActive", at = @At("HEAD"), cancellable = true)
	public void isPotionActive(Effect potionIn, final CallbackInfoReturnable<Boolean> ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		if(!activePotionsMap.containsKey(potionIn))
		{
			if(potionIn == Effects.NIGHT_VISION && AbilityDarkvision.isDarkvisionActive(entity))
					ci.setReturnValue(true);
			
			for(AbilityStatusEffect statusEffect : AbilityRegistry.getAbilitiesOfType(entity, AbilityStatusEffect.class))
				if(statusEffect.getEffect().getPotion() == potionIn)
					ci.setReturnValue(true);
		}
	}
	
	@Inject(method = "getActivePotionEffect", at = @At("HEAD"), cancellable = true)
	public void getActivePotion(Effect potionIn, final CallbackInfoReturnable<EffectInstance> ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		if(potionIn == Effects.NIGHT_VISION && AbilityDarkvision.isDarkvisionActive(entity))
		{
			EffectInstance effect = AbilityDarkvision.getEffect();
			if(!activePotionsMap.containsKey(potionIn) || effect.getAmplifier() > activePotionsMap.get(potionIn).getAmplifier())
				ci.setReturnValue(effect);
		}
		
		for(AbilityStatusEffect statusEffect : AbilityRegistry.getAbilitiesOfType(entity, AbilityStatusEffect.class))
		{
			EffectInstance effect = statusEffect.getEffect();
			if(effect != null && effect.getPotion() == potionIn)
				if(!activePotionsMap.containsKey(potionIn) || effect.getAmplifier() > activePotionsMap.get(potionIn).getAmplifier())
					ci.setReturnValue(new EffectInstance(potionIn, Integer.MAX_VALUE, effect.getAmplifier(), effect.isAmbient(), effect.doesShowParticles()));
		}
	}
	
	@Inject(method = "attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
	public void attackPetrifiedFrom(DamageSource source, float amount, final CallbackInfoReturnable<Boolean> ci)
	{
		// NEVER prevent out-of-world damage, as it's used for cleanup and the /kill command
		if(source == DamageSource.OUT_OF_WORLD)
			return;
		
		LivingEntity entity = (LivingEntity)(Object)this;
		if(entity.getType() == EntityType.PLAYER && PlayerData.isPlayerSoulDetached((PlayerEntity)entity))
		{
			ci.setReturnValue(false);
			ci.cancel();
		}
		else if(entity.isPotionActive(VOPotions.PETRIFIED))
		{
			if(
					source == DamageSource.FALL || source == DamageSource.FALLING_BLOCK || 
					source == DamageSource.LAVA)
				return;
			
			Entity attacker = source.getTrueSource() == null ? source.getImmediateSource() : source.getTrueSource();
			if(attacker != null && attacker.getType() == EntityType.PLAYER)
			{
				PlayerEntity player = (PlayerEntity)source.getTrueSource();
				BlockState stone = Blocks.STONE.getDefaultState();
				if(stone.canHarvestBlock(entity.getEntityWorld(), entity.getPosition(), player))
					return;
			}
			
			ci.setReturnValue(false);
			ci.cancel();
		}
	}
	
	@Inject(method = "isHandActive()Z", at = @At("HEAD"), cancellable = true)
	public void isHandActive(final CallbackInfoReturnable<Boolean> ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		if(entity.getType() == EntityType.PLAYER && PlayerData.isPlayerSoulDetached((PlayerEntity)entity))
			ci.setReturnValue(false);
	}
	
	@Inject(method = "isEntityUndead()Z", at = @At("HEAD"), cancellable = true)
	public void isEntityUndead(final CallbackInfoReturnable<Boolean> ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		if(EnumCreatureType.getTypes(entity).isUndead())
			ci.setReturnValue(true);
	}
	
	@Inject(method = "isEntityInsideOpaqueBlock", at = @At("HEAD"), cancellable = true)
	public void isEntityInsideOpaqueBlock(final CallbackInfoReturnable<Boolean> ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		if(IPhasingAbility.isPhasing(entity))
			ci.setReturnValue(false);
	}
	
	@Inject(method = "applyEntityCollision(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
	public void applyEntityCollision(Entity entityIn, final CallbackInfo ci)
	{
		LivingEntity thisEnt = (LivingEntity)(Object)this;
		if(IPhasingAbility.isPhasing(thisEnt) || PlayerData.isPlayerSoulDetached(thisEnt))
			ci.cancel();
		else if(entityIn instanceof LivingEntity)
			if(IPhasingAbility.isPhasing((LivingEntity)entityIn) || PlayerData.isPlayerSoulDetached((LivingEntity)entityIn))
				ci.cancel();
	}
	
	@Inject(method = "isOnLadder()Z", at = @At("HEAD"), cancellable = true)
	public void onClimbWall(final CallbackInfoReturnable<Boolean> ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(entity);
		if(abilityMap.containsKey(AbilityClimb.REGISTRY_NAME) && abilityMap.get(AbilityClimb.REGISTRY_NAME).isActive())
			if(entity.collidedHorizontally)
				ci.setReturnValue(true);
	}
	
	@Inject(method = "heal(F)V", at = @At("TAIL"))
	public void healBludgeoning(float healAmount, final CallbackInfo ci)
	{
		if(healAmount > 0)
		{
			LivingData data = LivingData.forEntity((LivingEntity)(Object)this);
			if(data != null && data.getBludgeoning() > 0)
				data.setBludgeoning(Math.max(0F, data.getBludgeoning() - healAmount));
		}
	}
	
	@Inject(method = "isWaterSensitive()Z", at = @At("TAIL"), cancellable = true)
	public void isWaterSensitive(final CallbackInfoReturnable<Boolean> ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(entity);
		if(abilityMap.containsKey(AbilityHurtByEnv.REGISTRY_NAME) && ((AbilityHurtByEnv)abilityMap.get(AbilityHurtByEnv.REGISTRY_NAME)).getEnvType() == EnvType.WATER)
			ci.setReturnValue(true);
	}
	
	/** True if the current call to vanilla travel function is being performed by code in this class */
	private boolean callingSuper = false;
	
	@Inject(method = "travel(Lnet/minecraft/util/math/vector/Vector3d;)V", at = @At("HEAD"), cancellable = true)
	public void travel(Vector3d travelVector, final CallbackInfo ci)
	{
		if(callingSuper)
		{
			callingSuper = false;
			return;
		}
		
		if(!this.isAlive())
			return;
		
		LivingEntity living = (LivingEntity)(Object)this;
		if(!(living instanceof MobEntity))
			return;
		
		MobEntity mob = (MobEntity)living;
		LivingData data = LivingData.forEntity(mob);
		if(data == null || !data.isBeingPossessed())
			return;
		
		LivingEntity possessor = data.getPossessor();
		if(possessor != null)
		{
			ci.cancel();
			mob.rotationYaw = possessor.rotationYaw;
			mob.prevRotationYaw = mob.rotationYaw;
			mob.rotationPitch = possessor.rotationPitch * 0.5F;
			setRotation(mob.rotationYaw, mob.rotationPitch);
			mob.renderYawOffset = mob.rotationYaw;
			mob.rotationYawHead = mob.renderYawOffset;
			float strafe = possessor.moveStrafing;
			float forward = possessor.moveForward;
			
			// TODO Jump handling
//			if(mob.isOnGround() && this.jumpPower == 0.0F && this.isRearing() && !this.allowStandSliding)
//			{
//				strafe = 0.0F;
//				forward = 0.0F;
//			}
//			
//			if(this.jumpPower > 0.0F && !this.isHorseJumping() && this.onGround)
//			{
//				double d0 = this.getHorseJumpStrength() * (double)this.jumpPower * (double)this.getJumpFactor();
//				double d1;
//				if (mob.isPotionActive(Effects.JUMP_BOOST))
//					d1 = d0 + (double)((float)(mob.getActivePotionEffect(Effects.JUMP_BOOST).getAmplifier() + 1) * 0.1F);
//				else
//					d1 = d0;
//				
//				Vector3d vector3d = mob.getMotion();
//				mob.setMotion(vector3d.x, d1, vector3d.z);
//				mob.isAirBorne = true;
//				net.minecraftforge.common.ForgeHooks.onLivingJump(mob);
//				if (forward > 0.0F)
//				{
//					float f2 = MathHelper.sin(mob.rotationYaw * ((float)Math.PI / 180F));
//					float f3 = MathHelper.cos(mob.rotationYaw * ((float)Math.PI / 180F));
//					mob.setMotion(mob.getMotion().add((double)(-0.4F * f2 * this.jumpPower), 0.0D, (double)(0.4F * f3 * this.jumpPower)));
//				}
//			}
//			
//			this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
			mob.setAIMoveSpeed((float)mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
			callingSuper = true;
			mob.travel(new Vector3d((double)strafe, travelVector.y, (double)forward));
			
			mob.func_233629_a_(mob, false);
		}
	}
}
