package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.capabilities.LivingData;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

@Mixin(TargetGoal.class)
public class TargetGoalMixin
{
	@Shadow
	protected Mob mob;
	
	@Shadow
	protected LivingEntity targetMob;
	
	@Inject(method = "canAttack(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;)Z", at = @At("RETURN"), cancellable = true)
	public void canAttack(LivingEntity living, TargetingConditions predicate, final CallbackInfoReturnable<Boolean> ci)
	{
		LivingData mobData = LivingData.forEntity(mob);
		if(mobData != null && living != null && mobData.isTargetingHindered(living))
			ci.setReturnValue(false);
	}
	
	@Inject(method = "canContinueToUse()Z", at = @At("RETURN"), cancellable = true)
	public void canContinueToUse(final CallbackInfoReturnable<Boolean> ci)
	{
		LivingData mobData = LivingData.forEntity(mob);
		if(mobData != null && targetMob != null && mobData.isTargetingHindered(targetMob))
			ci.setReturnValue(false);
	}
}
