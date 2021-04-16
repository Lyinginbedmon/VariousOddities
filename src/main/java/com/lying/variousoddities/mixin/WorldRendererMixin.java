package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.utility.VOBusClient;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(WorldRenderer.class)
public class WorldRendererMixin
{
	@Shadow
	private void renderSkyEnd(MatrixStack stack){ }
	
	@Inject(method = "renderSky(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", at = @At("HEAD"), cancellable = true)
	private void renderSky(MatrixStack stack, float partialTicks, CallbackInfo ci)
	{
		if(VOBusClient.playerInWall())
		{
			renderSkyEnd(stack);
			ci.cancel();
		}
	}
}
