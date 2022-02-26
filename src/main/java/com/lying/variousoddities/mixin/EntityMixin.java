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

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

@Mixin(Entity.class)
public class EntityMixin
{
	@Shadow public World world;
	
	@Shadow
	public final double getPosX(){ return 0D; }
	
	@Shadow
	public final double getPosY(){ return 0D; }
	
	@Shadow
	public final double getPosZ(){ return 0D; }
	
	@Shadow
	public EntityDataManager getDataManager(){ return null; }
	
	@Shadow
	public int getMaxAir(){ return 0; }
	
	@Shadow
	public void setAir(int airIn){ }
	
	@Shadow
	public boolean isSprinting(){ return false; }
	
	@Shadow
	public boolean isSneaking(){ return false; }
	
	@Shadow
	public boolean isOnGround(){ return false; }
	
	@Shadow
	public void setFlag(int flag, boolean set){ }
	
	@Shadow
	public void setSneaking(boolean keyDownIn){ }
	
	@Shadow
	public void setPose(Pose pose){ }
	
	@Shadow
	public boolean isPoseClear(Pose poseIn){ return false; }
	
	@Shadow
	public void setRotation(float strafe, float forward){ }
	
	@Shadow
	public BlockPos getPositionUnderneath(){ return BlockPos.ZERO; }
	
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
			for(IPhasingAbility phasing : AbilityRegistry.getAbilitiesOfType((LivingEntity)ent, IPhasingAbility.class))
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
		if(ent instanceof LivingEntity && AbilityRegistry.hasAbility((LivingEntity)ent, AbilityHoldBreath.REGISTRY_NAME))
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
	
	@Inject(method = "getBoundingBox(Lnet/minecraft/entity/Pose;)Lnet/minecraft/util/math/AxisAlignedBB;", at = @At("TAIL"), cancellable = true)
	public void getSize(Pose poseIn, final CallbackInfoReturnable<AxisAlignedBB> ci)
	{
		Entity ent = (Entity)(Object)this;
		if(ent.getType() == EntityType.PLAYER && AbilityRegistry.hasAbility((PlayerEntity)ent, AbilitySize.REGISTRY_NAME))
		{
			AbilitySize size = (AbilitySize)AbilityRegistry.getAbilityByName((PlayerEntity)ent, AbilitySize.REGISTRY_NAME);
			if(size == null) return;
			
			AxisAlignedBB baseSize = ci.getReturnValue();
			float scale = size.getScale();
			
			double posX = ent.getPosX();
			double posY = ent.getPosY();
			double posZ = ent.getPosZ();
			
			double lenX = baseSize.getXSize() * scale * 0.5D;
			double lenY = baseSize.getYSize() * scale;
			double lenZ = baseSize.getZSize() * scale * 0.5D;
			
			AxisAlignedBB trueSize = new AxisAlignedBB(
					posX - lenX, posY, posZ - lenZ,
					posX + lenX, posY + lenY, posZ + lenZ
					);
			
			ci.setReturnValue(trueSize);
		}
	}
}
