package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.capabilities.LivingData;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

@Mixin(TargetGoal.class)
public class TargetGoalMixin
{
	@Shadow
	protected Mob goalOwner;
	
	@Shadow
	protected LivingEntity target;
	
	@Inject(method = "isSuitableTarget(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EntityPredicate;)Z", at = @At("RETURN"), cancellable = true)
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
