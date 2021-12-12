package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin extends PlayerEntityMixin
{
	@Inject(method = "setEntityActionState(FFZZ)V", at = @At("HEAD"), cancellable = true)
	public void setEntityActionState(float strafe, float forward, boolean jumping, boolean sneaking, final CallbackInfo ci)
	{
		if(isPossessing())
		{
			if (strafe >= -1.0F && strafe <= 1.0F)
				this.moveStrafing = strafe;
			
			if (forward >= -1.0F && forward <= 1.0F)
				this.moveForward = forward;
			
			this.isJumping = jumping;
			this.setSneaking(sneaking);
		}
	}
}
