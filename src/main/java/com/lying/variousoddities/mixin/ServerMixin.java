package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.entity.AbstractCrab;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

@Mixin(ServerWorld.class)
public class ServerMixin
{
	@Inject(method = "playEvent", at = @At("HEAD"))
	private void crabDance(PlayerEntity player, int type, BlockPos pos, int vars, CallbackInfo callbackInfo)
	{
		System.out.println("Initiating crustacean dance event");
		if(type == 1010)
			for(AbstractCrab crab : player.getEntityWorld().getEntitiesWithinAABB(AbstractCrab.class, new AxisAlignedBB(pos).grow(3D)))
				if(crab.getAttackTarget() == null)
					crab.setPartying(pos);
	}
}
