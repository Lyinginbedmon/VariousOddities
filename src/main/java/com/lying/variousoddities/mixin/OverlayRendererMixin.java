package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.species.abilities.IPhasingAbility;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OverlayRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(OverlayRenderer.class)
public class OverlayRendererMixin
{
	@Inject(method = "renderTexture(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lcom/mojang/blaze3d/matrix/MatrixStack;)V", at = @At("HEAD"), cancellable = true)
	private static void renderBlockOverlay(Minecraft minecraftIn, TextureAtlasSprite spriteIn, MatrixStack stackIn, CallbackInfo ci)
	{
		PlayerEntity living = minecraftIn.player;
		if(IPhasingAbility.isPhasing(living))
			ci.cancel();
//		Collection<IPhasingAbility> phasings = AbilityRegistry.getAbilitiesOfType(living, IPhasingAbility.class);
//		phasings.forEach((ability) -> { if(ability.canPhase(minecraftIn.world, null, living)) ci.cancel(); });
	}
}
