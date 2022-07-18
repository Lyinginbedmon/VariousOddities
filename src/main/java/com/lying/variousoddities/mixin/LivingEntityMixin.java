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
import com.lying.variousoddities.init.VOPotions;
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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
		LivingEntity living = (LivingEntity)(Object)this;
		LivingData livingData = LivingData.forEntity(living);
		if(livingData == null)
			return;
		
		livingData.tick(living);
		this.setAir(livingData.getAir());
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
	
	private boolean overridingAddPotionEffect = false;
	
	@Inject(method = "addPotionEffect(Lnet/minecraft/potion/EffectInstance;)Z", at = @At("HEAD"), cancellable = true)
	public void stackPotion(EffectInstance effectIn, final CallbackInfoReturnable<Boolean> ci)
	{
		Effect effect = effectIn.getPotion();
		if(!(effect instanceof IStackingPotion) || !activePotionsMap.containsKey(effect))
			return;
		
		EffectInstance existing = activePotionsMap.get(effect);
		if(existing != null && existing.getDuration() > 0 && !this.overridingAddPotionEffect)
		{
			LivingEntity entity = (LivingEntity)(Object)this;
			effectIn = ((IStackingPotion)effect).stackInstances(effectIn, existing, entity);
			this.overridingAddPotionEffect = true;
			ci.setReturnValue(true);
			ci.cancel();
			entity.addPotionEffect(effectIn);
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
		if(entity.getType() == EntityType.PLAYER && ((PlayerEntity)entity).isSpectator())
			return;
		
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(entity);
		if(abilityMap.containsKey(AbilityClimb.REGISTRY_NAME) && abilityMap.get(AbilityClimb.REGISTRY_NAME).isActive() && hasAdjacentClimbable(entity))
			ci.setReturnValue(true);
	}
	
	private boolean hasAdjacentClimbable(LivingEntity entity)
	{
		if(entity.collidedHorizontally)
			return true;
		
		AxisAlignedBB boundingBox = entity.getBoundingBox().grow(0.18D, 0D, 0.18D);
		int minX = (int)Math.floor(boundingBox.minX);
		int maxX = (int)Math.ceil(boundingBox.maxX);
		
		int minY = (int)boundingBox.minY;
		int maxY = (int)boundingBox.maxY;
		
		int minZ = (int)Math.floor(boundingBox.minZ);
		int maxZ = (int)Math.ceil(boundingBox.maxZ);
		
		World world = entity.getEntityWorld();
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
