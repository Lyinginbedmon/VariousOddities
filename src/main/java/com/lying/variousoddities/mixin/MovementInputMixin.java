package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.init.VOPotions;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(MovementInputFromOptions.class)
public class MovementInputMixin
{
	@Inject(method = "tickMovement(Z)V", at = @At("HEAD"), cancellable = true)
	public void dazedPreventMovement(boolean forceDown, final CallbackInfo ci)
	{
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null)
		{
			EffectInstance dazed = player.getActivePotionEffect(VOPotions.DAZED);
			if(dazed != null && dazed.getDuration() > 0)
			{
				MovementInput input = (MovementInput)(Object)this;
				input.forwardKeyDown = false;
				input.backKeyDown = false;
				input.leftKeyDown = false;
				input.rightKeyDown = false;
				input.moveForward = 0F;
				input.moveStrafe = 0F;
				input.jump = false;
				input.sneaking = false;
				
				ci.cancel();
			}
		}
	}
}
