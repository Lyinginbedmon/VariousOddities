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

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CEntityActionPacket.Action;
import net.minecraft.util.ResourceLocation;

@Mixin(ServerPlayNetHandler.class)
public class ServerPlayNetHandlerMixin
{
	@Shadow
	public ServerPlayerEntity player;
	
	@Inject(method = "processEntityAction(Lnet/minecraft/network/play/client/CEntityActionPacket;)V", at = @At("HEAD"), cancellable = true)
	public void processEntityAction(CEntityActionPacket packetIn, final CallbackInfo ci)
	{
		if(packetIn.getAction() == Action.START_FALL_FLYING && player.isElytraFlying() && !player.isOnGround() && canElytraFly())
			ci.cancel();
	}
	
	private boolean canElytraFly()
	{
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(player);
		return abilityMap.containsKey(AbilityFlight.REGISTRY_NAME) && abilityMap.get(AbilityFlight.REGISTRY_NAME).isActive();
	}
}
