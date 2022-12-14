package com.lying.variousoddities.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.capabilities.Abilities;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityFlight;
import com.lying.variousoddities.species.abilities.AbilityRegistry;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(LocalPlayer.class)
public class ElytraMixinClient extends PlayerEntityMixin
{
	boolean isElytraFlying = false;
	
	@Inject(method = "livingTick()V", at = @At("HEAD"))
	public void livingTickStart(final CallbackInfo ci)
	{
		this.isElytraFlying = isFallFlying();
	}
	
	@Inject(method = "livingTick()V", at = @At("TAIL"))
	public void livingTickEnd(final CallbackInfo ci)
	{
		LocalPlayer living = (LocalPlayer)(Object)this;
		if(isElytraFlying && canElytraFly() && !isFallFlying() && !isDiscrete() && !isOnGround())
			living.connection.send(new ServerboundPlayerCommandPacket(living, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
	}
	
	private boolean canElytraFly()
	{
		LocalPlayer living = (LocalPlayer)(Object)this;
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(living);
		ResourceLocation flightKey = AbilityRegistry.getClassRegistryKey(AbilityFlight.class).location();
		return abilityMap.containsKey(flightKey) && abilityMap.get(flightKey).isActive() && Abilities.canBonusJump(living);
	}
}
