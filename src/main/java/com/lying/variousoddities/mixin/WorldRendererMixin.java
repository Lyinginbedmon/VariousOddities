package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.client.special.BlindRender;
import com.lying.variousoddities.utility.VOBusClient;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(LevelRenderer.class)
public class WorldRendererMixin
{
	@Shadow
	private void renderEndSky(PoseStack stack){ }
	
	@Inject(method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/math/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
	private void renderSky(PoseStack stack, Matrix4f matrix, float partialTicks, Camera camera, boolean client, Runnable runnable, CallbackInfo ci)
	{
		if(VOBusClient.playerInWall() || BlindRender.playerIsBlind())
		{
			renderEndSky(stack);
			ci.cancel();
		}
	}
	
	@Inject(method = "renderChunkLayer", at = @At("HEAD"), cancellable = true)
	private void renderChunkLayer(CallbackInfo ci)
	{
		if(BlindRender.playerIsBlind())
			ci.cancel();
	}
	
	@Inject(method = "renderEntity(Lnet/minecraft/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At("HEAD"), cancellable = true)
	private void renderEntity(Entity entityIn, double x, double y, double z, float f, PoseStack stack, MultiBufferSource buffer, CallbackInfo ci)
	{
		Player player = Minecraft.getInstance().player;
		if(BlindRender.playerIsBlind())
			if(entityIn == player) return;
			else if(!Minecraft.getInstance().shouldEntityAppearGlowing(entityIn))
				ci.cancel();
	}
	
	@Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
	private void renderClouds(CallbackInfo ci)
	{
		if(BlindRender.playerIsBlind())
			ci.cancel();
	}
	
	@Inject(method = "drawStars(Lcom/mojang/blaze3d/vertex/BufferBuilder;)Lcom/mojang/blaze3d/vertex/BufferBuilder/RenderedBuffer;", at = @At("HEAD"), cancellable = true)
	private void drawStars(CallbackInfoReturnable<BufferBuilder.RenderedBuffer> ci)
	{
		if(BlindRender.playerIsBlind())
			ci.cancel();
	}
	
	@Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true)
	private void renderSnowAndRain(CallbackInfo ci)
	{
		if(BlindRender.playerIsBlind())
			ci.cancel();
	}
}
