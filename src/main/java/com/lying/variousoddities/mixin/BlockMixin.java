package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.species.abilities.AbilityPhasing;
import com.lying.variousoddities.species.abilities.AbilityRegistry;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(Block.class)
public class BlockMixin
{
	@Inject(method = "onFallenUpon(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;F)V", at = @At("HEAD"), cancellable = true)
	public void incorporealFallOn(World worldIn, BlockPos pos, Entity entityIn, float fallDistance, final CallbackInfo ci)
	{
		if(entityIn instanceof LivingEntity)
			if(AbilityRegistry.hasAbility((LivingEntity)entityIn, AbilityPhasing.class))
				ci.cancel();
	}
	
	@Inject(method = "onEntityWalk(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
	public void incorporealWalkOn(World worldIn, BlockPos pos, Entity entityIn, final CallbackInfo ci)
	{
		if(entityIn instanceof LivingEntity)
			if(AbilityRegistry.hasAbility((LivingEntity)entityIn, AbilityPhasing.class))
				ci.cancel();
	}
}
