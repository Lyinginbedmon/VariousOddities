package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.init.VODamageSource;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;

@Mixin(Effect.class)
public class EffectMixin
{
	@Inject(method = "performEffect", at = @At("HEAD"), cancellable = true)
	public void performEffect(LivingEntity living, int amplifier, final CallbackInfo ci)
	{
		Effect effect = (Effect)(Object)this;
		if(effect == Effects.POISON)
		{
			if(living.getHealth() > 1.0F)
				living.attackEntityFrom(VODamageSource.POISON, 1.0F);
			ci.cancel();
		}
	}
}
