package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.client.special.BlindRender;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(EntityRendererManager.class)
public class EntityRendererManagerMixin
{
	@Inject(method = "getPackedLight(Lnet/minecraft/entity/Entity;F)I", at = @At("HEAD"), cancellable = true)
	public <E extends Entity> void getPackedLight(E entityIn, float partialTicks, final CallbackInfoReturnable<Integer> ci)
	{
		Player player = Minecraft.getInstance().player;
		if(player != null && BlindRender.playerIsBlind() && entityIn == player)
			ci.setReturnValue(8);
	}
}
