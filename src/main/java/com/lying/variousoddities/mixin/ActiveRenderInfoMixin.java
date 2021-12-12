package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.capabilities.PlayerData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.LivingEntity;

@Mixin(ActiveRenderInfo.class)
public class ActiveRenderInfoMixin
{
	@Shadow
	private float height;
	@Shadow
	private float previousHeight;
	
	@Inject(method = "interpolateHeight()V", at = @At("HEAD"), cancellable = true)
	public void possessedEyeHeight(final CallbackInfo ci)
	{
		PlayerData data = PlayerData.forPlayer(Minecraft.getInstance().player);
		if(data != null && data.isPossessing())
		{
			LivingEntity possessed = data.getPossessed();
			if(possessed == null)
				return;
			
			ci.cancel();
			
			this.previousHeight = this.height;
			this.height += (possessed.getEyeHeight() - this.height) * 0.5F;
		}
	}
}
