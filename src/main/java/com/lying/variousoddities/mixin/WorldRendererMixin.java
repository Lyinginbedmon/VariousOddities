package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.utility.VOBusClient;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
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
		if(VOBusClient.playerInWall() || VOBusClient.playerIsBlind())
		{
			renderSkyEnd(stack);
			ci.cancel();
		}
	}
	
	@Inject(method = "renderBlockLayer", at = @At("HEAD"), cancellable = true)
	private void renderBlockLayer(CallbackInfo ci)
	{
		if(VOBusClient.playerIsBlind())
			ci.cancel();
	}
	
	@Inject(method = "renderEntity(Lnet/minecraft/entity/Entity;DDDFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;)V", at = @At("HEAD"), cancellable = true)
	private void renderEntity(Entity entityIn, double x, double y, double z, float f, MatrixStack stack, IRenderTypeBuffer buffer, CallbackInfo ci)
	{
		PlayerEntity player = Minecraft.getInstance().player;
		if(VOBusClient.playerIsBlind())
			if(entityIn == player) return;
			else if(!Minecraft.getInstance().isEntityGlowing(entityIn))
				ci.cancel();
	}
	
	@Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
	private void renderClouds(CallbackInfo ci)
	{
		if(VOBusClient.playerIsBlind())
			ci.cancel();
	}
	
	@Inject(method = "renderStars", at = @At("HEAD"), cancellable = true)
	private void renderStars(CallbackInfo ci)
	{
		if(VOBusClient.playerIsBlind())
			ci.cancel();
	}
	
	@Inject(method = "renderRainSnow", at = @At("HEAD"), cancellable = true)
	private void renderRainSnow(CallbackInfo ci)
	{
		if(VOBusClient.playerIsBlind())
			ci.cancel();
	}
}
