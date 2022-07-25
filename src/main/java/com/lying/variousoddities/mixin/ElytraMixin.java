package com.lying.variousoddities.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityFlight;
import com.lying.variousoddities.species.abilities.AbilityRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public class ElytraMixin extends EntityMixin
{
	boolean isElytraFlying = false;
	
	@Shadow
	public boolean isElytraFlying(){ return false; }
	
	@Inject(method = "livingTick()V", at = @At("HEAD"))
	public void livingTickStart(final CallbackInfo ci)
	{
		this.isElytraFlying = isElytraFlying();
	}
	
	@Inject(method = "livingTick()V", at = @At("TAIL"))
	public void livingTickEnd(final CallbackInfo ci)
	{
		if(isElytraFlying && canElytraFly() && !isElytraFlying() && !isSneaking() && !isOnGround())
			setFlag(7, true);
	}
	
	private boolean canElytraFly()
	{
		LivingEntity living = (LivingEntity)(Object)this;
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(living);
		return abilityMap.containsKey(AbilityFlight.REGISTRY_NAME) && ((AbilityFlight)abilityMap.get(AbilityFlight.REGISTRY_NAME)).isActive();
	}
}
