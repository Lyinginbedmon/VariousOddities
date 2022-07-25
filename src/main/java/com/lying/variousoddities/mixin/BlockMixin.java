package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.species.abilities.IPhasingAbility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

@Mixin(Block.class)
public class BlockMixin
{
	@Inject(method = "onFallenUpon(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;F)V", at = @At("HEAD"), cancellable = true)
	public void incorporealFallOn(Level worldIn, BlockPos pos, Entity entityIn, float fallDistance, final CallbackInfo ci)
	{
		if(entityIn instanceof LivingEntity)
			if(IPhasingAbility.isPhasing((LivingEntity)entityIn))
				ci.cancel();
	}
	
	@Inject(method = "onEntityWalk(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
	public void incorporealWalkOn(Level worldIn, BlockPos pos, Entity entityIn, final CallbackInfo ci)
	{
		if(entityIn instanceof LivingEntity)
			if(IPhasingAbility.isPhasing((LivingEntity)entityIn))
				ci.cancel();
	}
}
