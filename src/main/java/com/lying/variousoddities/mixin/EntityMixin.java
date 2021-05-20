package com.lying.variousoddities.mixin;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.species.abilities.AbilityHoldBreath;
import com.lying.variousoddities.species.abilities.AbilityPhasing;
import com.lying.variousoddities.species.abilities.AbilityRegistry;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

@Mixin(Entity.class)
public class EntityMixin extends CapabilityProviderMixin
{
	@Shadow public World world;
	
	@Shadow
	public final double getPosX()
	{
		return 0D;
	}
	
	@Shadow
	public final double getPosY()
	{
		return 0D;
	}
	
	@Shadow
	public final double getPosZ()
	{
		return 0D;
	}
	
	@Shadow
	public EntityDataManager getDataManager()
	{
		return null;
	}
	
	@Shadow
	public int getMaxAir(){ return 0; }
	
	@Shadow
	public void setAir(int airIn){ }
	
	@Shadow
	public boolean isSprinting()
	{
		return false;
	}
	
	@Shadow
	public boolean isSneaking()
	{
		return false;
	}
	
	@Shadow
	public boolean isOnGround()
	{
		return false;
	}
	
	@Shadow
	public void setFlag(int flag, boolean set){ }
	
	@Inject(method = "pushOutOfBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;<init>(DDD)V"), cancellable = true)
	public void incorporealPushOutOfBlock(double x, double y, double z, final CallbackInfo ci)
	{
		Entity ent = (Entity)(Object)this;
		if(ent instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)ent;
			Collection<AbilityPhasing> phasings = AbilityRegistry.getAbilitiesOfType(living, AbilityPhasing.class);
			phasings.forEach((ability) -> { if(ability.canPhase(world, null, living)) ci.cancel(); });
		}
	}
	
	@Inject(method = "updateFallState", at = @At("HEAD"))
	public void incorporealFall(final CallbackInfo ci)
	{
		Entity ent = (Entity)(Object)this;
		if(ent instanceof LivingEntity && AbilityRegistry.hasAbility((LivingEntity)ent, AbilityPhasing.class))
			ent.fallDistance = 0F;
	}
	
	@Inject(method = "isSteppingCarefully()Z", at = @At("HEAD"), cancellable = true)
	public void incorporealStepCarefully(final CallbackInfoReturnable<Boolean> ci)
	{
		Entity ent = (Entity)(Object)this;
		if(ent instanceof LivingEntity && AbilityRegistry.hasAbility((LivingEntity)ent, AbilityPhasing.class) && !isSprinting())
			ci.setReturnValue(true);
	}
	
	@Inject(method = "getMaxAir()I", at = @At("HEAD"), cancellable = true)
	public void getMaxAirReptile(final CallbackInfoReturnable<Integer> ci)
	{
		Entity ent = (Entity)(Object)this;
		if(ent instanceof LivingEntity && AbilityRegistry.hasAbility((LivingEntity)ent, AbilityHoldBreath.REGISTRY_NAME))
			ci.setReturnValue(ci.getReturnValueI() * 2);
	}
	
	@Inject(method = "setMotionMultiplier(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/vector/Vector3d;)V", at = @At("HEAD"), cancellable = true)
	public void cancelWebSlowdown(BlockState state, Vector3d multiplier, final CallbackInfo ci)
	{
		Entity ent = (Entity)(Object)this;
		if(ent instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)ent;
			if(AbilityRegistry.hasAbility(living, AbilityPhasing.class))
				ci.cancel();
		}
	}
}
