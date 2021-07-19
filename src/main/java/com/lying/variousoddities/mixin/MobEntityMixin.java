package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.init.VOPotions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;

@Mixin(MobEntity.class)
public class MobEntityMixin extends LivingEntityMixin 
{
	@Inject(method = "playAmbientSound()V", at = @At("HEAD"), cancellable = true)
	public void playAmbientSound(final CallbackInfo ci)
	{
		MobEntity entity = (MobEntity)(Object)this;
		if(entity.isPotionActive(VOPotions.SILENCED))
			ci.cancel();
	}
	
	@Inject(method = "playHurtSound", at = @At("HEAD"), cancellable = true)
	public void playHurtSound(final CallbackInfo ci)
	{
		MobEntity entity = (MobEntity)(Object)this;
		if(entity.isPotionActive(VOPotions.SILENCED))
			ci.cancel();
	}
	
	@Inject(method = "updateAITasks", at = @At("HEAD"), cancellable = true)
	public void isMobParalysed(final CallbackInfo ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		if(VOPotions.isParalysed(entity))
			ci.cancel();
	}
}
