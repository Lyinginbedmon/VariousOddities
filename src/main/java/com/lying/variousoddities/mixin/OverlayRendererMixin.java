package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.species.abilities.IPhasingAbility;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(ScreenEffectRenderer.class)
public class OverlayRendererMixin
{
	@Inject(method = "renderTexture(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"), cancellable = true)
	private static void renderBlockOverlay(TextureAtlasSprite spriteIn, PoseStack stackIn, CallbackInfo ci)
	{
		Player living = Minecraft.getInstance().player;
		if(IPhasingAbility.isPhasing(living))
			ci.cancel();
//		Collection<IPhasingAbility> phasings = AbilityRegistry.getAbilitiesOfType(living, IPhasingAbility.class);
//		phasings.forEach((ability) -> { if(ability.canPhase(minecraftIn.world, null, living)) ci.cancel(); });
	}
}
