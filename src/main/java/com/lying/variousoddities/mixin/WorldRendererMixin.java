package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.client.special.BlindRender;
import com.lying.variousoddities.utility.VOBusClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
		if(VOBusClient.playerInWall() || BlindRender.playerIsBlind())
		{
			renderSkyEnd(stack);
			ci.cancel();
		}
	}
	
	@Inject(method = "renderBlockLayer", at = @At("HEAD"), cancellable = true)
	private void renderBlockLayer(CallbackInfo ci)
	{
		if(BlindRender.playerIsBlind())
			ci.cancel();
	}
	
	@Inject(method = "renderEntity(Lnet/minecraft/entity/Entity;DDDFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;)V", at = @At("HEAD"), cancellable = true)
	private void renderEntity(Entity entityIn, double x, double y, double z, float f, MatrixStack stack, IRenderTypeBuffer buffer, CallbackInfo ci)
	{
		Player player = Minecraft.getInstance().player;
		if(BlindRender.playerIsBlind())
			if(entityIn == player) return;
			else if(!Minecraft.getInstance().isEntityGlowing(entityIn))
				ci.cancel();
	}
	
	@Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
	private void renderClouds(CallbackInfo ci)
	{
		if(BlindRender.playerIsBlind())
			ci.cancel();
	}
	
	@Inject(method = "renderStars", at = @At("HEAD"), cancellable = true)
	private void renderStars(CallbackInfo ci)
	{
		if(BlindRender.playerIsBlind())
			ci.cancel();
	}
	
	@Inject(method = "renderRainSnow", at = @At("HEAD"), cancellable = true)
	private void renderRainSnow(CallbackInfo ci)
	{
		if(BlindRender.playerIsBlind())
			ci.cancel();
	}
}
