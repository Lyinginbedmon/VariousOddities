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
import com.lying.variousoddities.init.VOMobEffects;
import com.lying.variousoddities.potion.IStackingPotion;
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

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

@Mixin(LivingEntity.class)
public class LivingEntityMixin extends EntityMixin
{
	@Shadow
	public Map<MobEffect, MobEffectInstance> activePotionsMap = Maps.newHashMap();
	
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
		if(!living.getLevel().isClientSide)
		{
			LivingData livingData = LivingData.forEntity(living);
			if(livingData == null)
				return;
			
			for(MobEffect visual : VOMobEffects.VISUALS.keySet())
			{
				int index = VOMobEffects.getVisualPotionIndex(visual);
				boolean active = living.hasEffect(visual);
				
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
		LivingEntity living = (LivingEntity)(Object)this;
		LivingData livingData = LivingData.forEntity(living);
		if(livingData == null)
			return;
		
		livingData.tick(living);
		this.setAir(livingData.getAir());
	}
	
	@Inject(method = "isPotionApplicable", at = @At("HEAD"), cancellable = true)
	public void isPotionApplicable(MobEffectInstance effectInstanceIn, final CallbackInfoReturnable<Boolean> ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		for(AbilityStatusImmunity statusImmunity : AbilityRegistry.getAbilitiesOfType(entity, AbilityStatusImmunity.class))
			if(statusImmunity.appliesToStatus(effectInstanceIn))
			{
				ci.setReturnValue(false);
				break;
			}
	}
	
	@Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
	public void hasEffect(MobEffect potionIn, final CallbackInfoReturnable<Boolean> ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		if(!activePotionsMap.containsKey(potionIn))
		{
			if(potionIn == MobEffects.NIGHT_VISION && AbilityDarkvision.isDarkvisionActive(entity))
					ci.setReturnValue(true);
			
			for(AbilityStatusEffect statusEffect : AbilityRegistry.getAbilitiesOfType(entity, AbilityStatusEffect.class))
				if(statusEffect.getEffect().getEffect() == potionIn)
					ci.setReturnValue(true);
		}
	}
	
	@Inject(method = "getActivePotionEffect", at = @At("HEAD"), cancellable = true)
	public void getActivePotion(MobEffect potionIn, final CallbackInfoReturnable<MobEffectInstance> ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		if(potionIn == MobEffects.NIGHT_VISION && AbilityDarkvision.isDarkvisionActive(entity))
		{
			MobEffectInstance effect = AbilityDarkvision.getEffect();
			if(!activePotionsMap.containsKey(potionIn) || effect.getAmplifier() > activePotionsMap.get(potionIn).getAmplifier())
				ci.setReturnValue(effect);
		}
		
		for(AbilityStatusEffect statusEffect : AbilityRegistry.getAbilitiesOfType(entity, AbilityStatusEffect.class))
		{
			MobEffectInstance effect = statusEffect.getEffect();
			if(effect != null && effect.getEffect() == potionIn)
				if(!activePotionsMap.containsKey(potionIn) || effect.getAmplifier() > activePotionsMap.get(potionIn).getAmplifier())
					ci.setReturnValue(new MobEffectInstance(potionIn, Integer.MAX_VALUE, effect.getAmplifier(), effect.isAmbient(), effect.isVisible()));
		}
	}
	
	private boolean overridingAddPotionEffect = false;
	
	@Inject(method = "addPotionEffect(Lnet/minecraft/potion/MobEffectInstance;)Z", at = @At("HEAD"), cancellable = true)
	public void stackPotion(MobEffectInstance effectIn, final CallbackInfoReturnable<Boolean> ci)
	{
		MobEffect effect = effectIn.getEffect();
		if(!(effect instanceof IStackingPotion) || !activePotionsMap.containsKey(effect))
			return;
		
		MobEffectInstance existing = activePotionsMap.get(effect);
		if(existing != null && existing.getDuration() > 0 && !this.overridingAddPotionEffect)
		{
			LivingEntity entity = (LivingEntity)(Object)this;
			effectIn = ((IStackingPotion)effect).stackInstances(effectIn, existing, entity);
			this.overridingAddPotionEffect = true;
			ci.setReturnValue(true);
			ci.cancel();
			entity.addEffect(effectIn);
		}
		else
			this.overridingAddPotionEffect = false;
	}
	
	@Inject(method = "attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
	public void attackPetrifiedFrom(DamageSource source, float amount, final CallbackInfoReturnable<Boolean> ci)
	{
		// NEVER prevent out-of-world damage, as it's used for cleanup and the /kill command
		if(source == DamageSource.OUT_OF_WORLD)
			return;
		
		LivingEntity entity = (LivingEntity)(Object)this;
		if(entity.getType() == EntityType.PLAYER && PlayerData.isPlayerSoulDetached((Player)entity))
		{
			ci.setReturnValue(false);
			ci.cancel();
		}
		else if(entity.hasEffect(VOMobEffects.PETRIFIED))
		{
			if(
					source == DamageSource.FALL || source == DamageSource.FALLING_BLOCK || 
					source == DamageSource.LAVA)
				return;
			
			Entity attacker = source.getEntity() == null ? source.getDirectEntity() : source.getEntity();
			if(attacker != null && attacker.getType() == EntityType.PLAYER)
			{
				Player player = (Player)source.getEntity();
				BlockState stone = Blocks.STONE.defaultBlockState();
				if(stone.canHarvestBlock(entity.getLevel(), entity.blockPosition(), player))
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
		if(entity.getType() == EntityType.PLAYER && PlayerData.isPlayerSoulDetached((Player)entity))
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
		if(entity.getType() == EntityType.PLAYER && ((Player)entity).isSpectator())
			return;
		
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(entity);
		if(abilityMap.containsKey(AbilityClimb.REGISTRY_NAME) && abilityMap.get(AbilityClimb.REGISTRY_NAME).isActive() && hasAdjacentClimbable(entity))
			ci.setReturnValue(true);
	}
	
	private boolean hasAdjacentClimbable(LivingEntity entity)
	{
		if(entity.horizontalCollision)
			return true;
		
		AABB boundingBox = entity.getBoundingBox().inflate(0.18D, 0D, 0.18D);
		int minX = (int)Math.floor(boundingBox.minX);
		int maxX = (int)Math.ceil(boundingBox.maxX);
		
		int minY = (int)boundingBox.minY;
		int maxY = (int)boundingBox.maxY;
		
		int minZ = (int)Math.floor(boundingBox.minZ);
		int maxZ = (int)Math.ceil(boundingBox.maxZ);
		
		Level world = entity.getLevel();
		for(int y=minY; y<maxY; y++)
			for(int x=minX; x<maxX; x++)
					for(int z=minZ; z<maxZ; z++)
						if(AbilityClimb.isClimbable(new BlockPos(x, y, z), world))
							return true;
		
		return false;
	}
	
	@Inject(method = "heal(F)V", at = @At("TAIL"))
	public void healBludgeoning(float healAmount, final CallbackInfo ci)
	{
		if(healAmount > 0)
		{
			LivingData data = LivingData.forEntity((LivingEntity)(Object)this);
			if(data != null && data.getBludgeoning() > 0)
				data.addBludgeoning(Math.min(0F, -healAmount));
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
	
	@Inject(method = "canAttack(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EntityPredicate;)Z", at = @At("HEAD"), cancellable = true)
	public void canTarget(LivingEntity living, EntityPredicate predicate, final CallbackInfoReturnable<Boolean> ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		LivingData data = LivingData.forEntity(entity);
		if(data.isTargetingHindered(living))
			ci.setReturnValue(false);
	}
}
