package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.capabilities.LivingData;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.TargetGoal;

@Mixin(TargetGoal.class)
public class TargetGoalMixin
{
	@Shadow
	protected MobEntity goalOwner;
	
	@Shadow
	protected LivingEntity target;
	
	@Inject(method = "isSuitableTarget(Lnet/minecraft/entity/LivingEntity;Ljava/lang/Class;Lnet/minecraft/entity/EntityPredicate;)Z", at = @At("RETURN"), cancellable = true)
	public void isSuitableTarget(LivingEntity living, EntityPredicate predicate, final CallbackInfoReturnable<Boolean> ci)
	{
		LivingData mobData = LivingData.forEntity(goalOwner);
		if(mobData != null && living != null && mobData.isTargetingHindered(living))
			ci.setReturnValue(false);
	}
	
	@Inject(method = "shouldContinueExecuting()Z", at = @At("RETURN"), cancellable = true)
	public void shouldContinueExecuting(final CallbackInfoReturnable<Boolean> ci)
	{
		LivingData mobData = LivingData.forEntity(goalOwner);
		if(mobData != null && target != null && mobData.isTargetingHindered(target))
			ci.setReturnValue(false);
	}
}
