package com.lying.variousoddities.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityClimb;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.AbilityStatusImmunity;
import com.lying.variousoddities.species.abilities.IPhasingAbility;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

@Mixin(LivingEntity.class)
public class LivingEntityMixin extends EntityMixin
{
	@Shadow
	public float getHealth(){ return 0F; }
	
	@Shadow
	public float getMaxHealth(){ return 0F; }
	
	@Shadow
	public boolean isElytraFlying(){ return false; }
	
	@Inject(method = "updatePotionEffects", at = @At("HEAD"))
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
		}
	}
	
	@Inject(method = "baseTick", at = @At("TAIL"))
	public void baseTick(CallbackInfo callbackInfo)
	{
		LivingData livingData = LivingData.forEntity((LivingEntity)(Object)this);
		if(livingData == null)
			return;
		
		livingData.tick((LivingEntity)(Object)this);
		if(livingData.overrideAir())
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
	
	@Inject(method = "attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
	public void attackPetrifiedFrom(DamageSource source, float amount, final CallbackInfoReturnable<Boolean> ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		if(entity.isPotionActive(VOPotions.PETRIFIED))
		{
			if(
					source == DamageSource.FALL || source == DamageSource.FALLING_BLOCK || 
					source == DamageSource.LAVA || source == DamageSource.OUT_OF_WORLD)
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
		if(IPhasingAbility.isPhasing((LivingEntity)(Object)this))
			ci.cancel();
		else if(entityIn instanceof LivingEntity)
			if(IPhasingAbility.isPhasing((LivingEntity)entityIn))
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
}
