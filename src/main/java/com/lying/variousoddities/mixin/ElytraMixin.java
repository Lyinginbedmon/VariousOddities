package com.lying.variousoddities.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityFlight;
import com.lying.variousoddities.species.abilities.AbilityRegistry;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@Mixin(ItemStack.class)
public class ElytraMixin
{
	@Inject(method = "canElytraFly(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	public void elytraFlyCheck(LivingEntity entity, final CallbackInfoReturnable<Boolean> ci)
	{
		if(canElytraFly(entity))
			ci.setReturnValue(true);
	}
	
	@Inject(method = "elytraFlightTick(Lnet/minecraft/entity/LivingEntity;I)Z", at = @At("HEAD"), cancellable = true)
	public void elytraFlyCheck2(LivingEntity entity, int flightTicks, final CallbackInfoReturnable<Boolean> ci)
	{
		if(canElytraFly(entity))
			ci.setReturnValue(true);
	}
	
	private boolean canElytraFly(LivingEntity entity)
	{
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(entity);
		return abilityMap.containsKey(AbilityFlight.REGISTRY_NAME) && ((AbilityFlight)abilityMap.get(AbilityFlight.REGISTRY_NAME)).active();
	}
}
