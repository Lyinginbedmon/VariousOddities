package com.lying.variousoddities.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.types.EnumCreatureType.ActionSet;
import com.lying.variousoddities.world.savedata.TypesManager;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends LivingEntityMixin
{
	@Shadow
	public GameProfile getGameProfile(){ return null; }
	
	private UUID getPlayerUUID()
	{
		return PlayerEntity.getUUID(getGameProfile());
	}
	
	@Inject(method = "shouldHeal()Z", at = @At("HEAD"), cancellable = true)
	public void shouldHeal(final CallbackInfoReturnable<Boolean> ci)
	{
		TypesManager manager = TypesManager.get(world);
		ActionSet actions = ActionSet.fromTypes(manager.getMobTypes(world.getPlayerByUuid(getPlayerUUID())));
		if(!actions.regenerates())
			ci.setReturnValue(false);
	}
}
