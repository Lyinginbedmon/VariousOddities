package com.lying.variousoddities.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.capabilities.Abilities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityFlight;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.EnumCreatureType.ActionSet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends LivingEntityMixin
{
	@Inject(method = "shouldHeal()Z", at = @At("HEAD"), cancellable = true)
	public void shouldHeal(final CallbackInfoReturnable<Boolean> ci)
	{
		PlayerEntity player = (PlayerEntity)(Object)this;
		LivingData data = LivingData.forEntity(player);
		if(data != null && data.checkingFoodRegen)
		{
			ActionSet actions = ActionSet.fromTypes(player, EnumCreatureType.getCreatureTypes(player));
			if(!actions.regenerates())
				ci.setReturnValue(false);
			
			data.checkingFoodRegen = false;
		}
	}
	
	@Inject(method = "tryToStartFallFlying()Z", at = @At("HEAD"), cancellable = true)
	public void startElytraFlying(final CallbackInfoReturnable<Boolean> ci)
	{
		PlayerEntity player = (PlayerEntity)(Object)this;
		Abilities abilities = LivingData.forEntity(player).getAbilities();
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(player);
		if(abilityMap.containsKey(AbilityFlight.REGISTRY_NAME) && ((AbilityFlight)abilityMap.get(AbilityFlight.REGISTRY_NAME)).active())
		{
			if(!player.isOnGround() && !player.isElytraFlying() && abilities.canAirJump)
			{
				player.startFallFlying();
				ci.setReturnValue(true);
			}
		}
	}
}
