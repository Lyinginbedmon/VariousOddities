package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityHoldBreath;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.AbilitySize;
import com.lying.variousoddities.species.abilities.IPhasingAbility;
import com.mojang.math.Vector3d;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Entity.class)
public class EntityMixin
{
	@Shadow public Level level;
	
	@Shadow
	public final double getX(){ return 0D; }
	
	@Shadow
	public final double getY(){ return 0D; }
	
	@Shadow
	public final double getZ(){ return 0D; }
	
	@Shadow
	public SynchedEntityData getEntityData(){ return null; }
	
	@Shadow
	public int getMaxAirSupply(){ return 0; }
	
	@Shadow
	public void setAirSupply(int airIn){ }
	
	@Shadow
	public boolean isSprinting(){ return false; }
	
	@Shadow
	public boolean isDiscrete(){ return false; }
	
	@Shadow
	public boolean isOnGround(){ return false; }
	
	@Shadow
	public void setSharedFlag(int flag, boolean set){ }
	
	@Shadow
	public void setShiftKeyDown(boolean keyDownIn){ }
	
	@Shadow
	public void setPose(Pose pose){ }
	
	@Shadow
	public boolean canEnterPose(Pose poseIn){ return false; }
	
	@Shadow
	public void setRot(float strafe, float forward){ }
	
	@Shadow
	public BlockPos getBlockPosBelowThatAffectsMyMovement(){ return BlockPos.ZERO; }
	
	@Shadow
	public boolean isAlive(){ return true; }
	
	@Inject(method = "pushOutOfBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;<init>(DDD)V"), cancellable = true)
	public void incorporealPushOutOfBlock(double x, double y, double z, final CallbackInfo ci)
	{
		Entity ent = (Entity)(Object)this;
		if(ent instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)ent;
			if(!IPhasingAbility.isPhasing(living))
				ci.cancel();
		}
	}
	
	@Inject(method = "updateFallState", at = @At("HEAD"))
	public void incorporealFall(final CallbackInfo ci)
	{
		Entity ent = (Entity)(Object)this;
		if(ent instanceof LivingEntity && IPhasingAbility.isPhasing((LivingEntity)ent))
			for(IPhasingAbility phasing : AbilityRegistry.getAbilitiesOfClass((LivingEntity)ent, IPhasingAbility.class))
				if(phasing.preventsFallDamage((Ability)phasing))
				{
					Ability ability = (Ability)phasing;
					if(ability.passive() || ability.isActive())
					{
						ent.fallDistance = 0F;
						return;
					}
				}
	}
	
	@Inject(method = "isSteppingCarefully()Z", at = @At("HEAD"), cancellable = true)
	public void incorporealStepCarefully(final CallbackInfoReturnable<Boolean> ci)
	{
		Entity ent = (Entity)(Object)this;
		if(ent instanceof LivingEntity && ((IPhasingAbility.isPhasing((LivingEntity)ent) && !isSprinting()) || !PlayerData.isPlayerNormalFunction((LivingEntity)ent)))
			ci.setReturnValue(true);
	}
	
	@Inject(method = "getMaxAir()I", at = @At("HEAD"), cancellable = true)
	public void getMaxAirReptile(final CallbackInfoReturnable<Integer> ci)
	{
		Entity ent = (Entity)(Object)this;
		if(ent instanceof LivingEntity && AbilityRegistry.hasAbilityOfMapName((LivingEntity)ent, AbilityRegistry.getClassRegistryKey(AbilityHoldBreath.class).location()))
			ci.setReturnValue(600);
	}
	
	@Inject(method = "setMotionMultiplier(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/vector/Vector3d;)V", at = @At("HEAD"), cancellable = true)
	public void cancelWebSlowdown(BlockState state, Vector3d multiplier, final CallbackInfo ci)
	{
		Entity ent = (Entity)(Object)this;
		if(ent instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)ent;
			if(IPhasingAbility.isPhasing(living) || !PlayerData.isPlayerNormalFunction(living))
				ci.cancel();
		}
	}
	
	@Inject(method = "getDimensions(Lnet/minecraft/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;", at = @At("TAIL"), cancellable = true)
	public void getDimensions(Pose poseIn, final CallbackInfoReturnable<EntityDimensions> ci)
	{
		Entity ent = (Entity)(Object)this;
		ResourceLocation sizeKey = AbilityRegistry.getClassRegistryKey(AbilitySize.class).location();
		if(ent.getType() == EntityType.PLAYER && AbilityRegistry.hasAbilityOfMapName((Player)ent, sizeKey))
		{
			AbilitySize size = (AbilitySize)AbilityRegistry.getAbilityByMapName((Player)ent, sizeKey);
			if(size == null) return;
			
			EntityDimensions baseSize = ci.getReturnValue();
			float scale = size.getScale();
			
			float lenX = baseSize.width * scale * 0.5F;
			float lenY = baseSize.height * scale;
			
			ci.setReturnValue(EntityDimensions.scalable(lenX, lenY));
		}
	}
}
