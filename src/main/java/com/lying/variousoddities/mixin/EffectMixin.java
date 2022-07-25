package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VODamageSource;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

@Mixin(MobEffect.class)
public class EffectMixin
{
	@Inject(method = "performEffect", at = @At("HEAD"), cancellable = true)
	public void performEffect(LivingEntity living, int amplifier, final CallbackInfo ci)
	{
		LivingData data = LivingData.forEntity(living);
		MobEffect effect = (MobEffect)(Object)this;
		if(effect == MobEffects.POISON)
		{
			if(living.getHealth() > 1.0F)
				living.hurt(VODamageSource.POISON, 1.0F);
			ci.cancel();
		}
		else if(effect == MobEffects.REGENERATION && data != null && data.getBludgeoning() > 0F && !living.getLevel().isClientSide)
			data.addBludgeoning(-1F);
	}
}
