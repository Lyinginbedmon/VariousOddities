package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.client.special.BlindRender;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(BlockEntityRenderDispatcher.class)
public class TileEntityRenderDispatcherMixin
{
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void render(final CallbackInfo ci)
	{
		if(BlindRender.playerIsBlind())
			ci.cancel();
	}
}
