package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.capabilities.LivingData;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

@Mixin(FoodData.class)
public class FoodDataMixin
{
	/**
	 * Passthrough to inform PlayerEntityMixin to modify shouldHeal for non-regenerating players without affecting other calls to shouldHeal.<br>
	 * FoodStats is the only call to it in vanilla, but other mods (such as Vampirism) may reference it.<br>
	 */
	@Inject(method = "tick(Lnet/minecraft/entity/player/PlayerEntity;)V", at = @At("INVOKE"))
	public void tick(Player player, CallbackInfo ci)
	{
		LivingData data = LivingData.forEntity(player);
		if(data != null)
			data.checkingFoodRegen = true;
	}
}
