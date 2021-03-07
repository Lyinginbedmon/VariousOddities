package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.entity.AbstractCrab;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

@Mixin(ServerWorld.class)
public class ServerWorldMixin
{
	@Inject(method = "playEvent", at = @At("HEAD"))
	private void crabDance(PlayerEntity player, int type, BlockPos pos, int vars, CallbackInfo callbackInfo)
	{
		if(type == 1010)
			AbstractCrab.startParty((ServerWorld)(Object)this, pos, vars != 0);
	}
}
