package com.lying.variousoddities.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityFlight;
import com.lying.variousoddities.species.abilities.AbilityRegistry;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(ClientPlayerEntity.class)
public class ElytraMixinClient extends PlayerEntityMixin
{
	boolean isElytraFlying = false;
	
	@Inject(method = "livingTick()V", at = @At("HEAD"))
	public void livingTickStart(final CallbackInfo ci)
	{
		this.isElytraFlying = isElytraFlying();
	}
	
	@Inject(method = "livingTick()V", at = @At("TAIL"))
	public void livingTickEnd(final CallbackInfo ci)
	{
		ClientPlayerEntity living = (ClientPlayerEntity)(Object)this;
		if(isElytraFlying && canElytraFly() && !isElytraFlying() && !isSneaking() && !isOnGround())
			living.connection.sendPacket(new CEntityActionPacket(living, CEntityActionPacket.Action.START_FALL_FLYING));
	}
	
	private boolean canElytraFly()
	{
		ClientPlayerEntity living = (ClientPlayerEntity)(Object)this;
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(living);
		return abilityMap.containsKey(AbilityFlight.REGISTRY_NAME) && ((AbilityFlight)abilityMap.get(AbilityFlight.REGISTRY_NAME)).active();
	}
}
