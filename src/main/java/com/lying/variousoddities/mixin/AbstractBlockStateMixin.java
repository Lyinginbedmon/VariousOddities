package com.lying.variousoddities.mixin;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.AbilityWaterWalking;
import com.lying.variousoddities.species.abilities.IPhasingAbility;

import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.EntitySelectionContext;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin
{
	@Inject(
			method = "getCollisionShape(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/shapes/ISelectionContext;)Lnet/minecraft/util/math/shapes/VoxelShape;",
			at = @At("HEAD"), 
			cancellable = true)
	private void getCollisionShape(IBlockReader worldIn, BlockPos pos, ISelectionContext context, final CallbackInfoReturnable<VoxelShape> ci)
	{
		Entity entity = context.getEntity();
		if(context instanceof EntitySelectionContext && entity != null && entity.isAlive() && entity instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)entity;
			if(IPhasingAbility.isPhasing(living))
			{
				Collection<IPhasingAbility> phasings = AbilityRegistry.getAbilitiesOfType(living, IPhasingAbility.class);
				if(!phasings.isEmpty())
					phasings.forEach((ability) -> 
					{
						if(ability.canPhase(worldIn, pos, living))
							ci.setReturnValue(VoxelShapes.empty());
					});
			}
			
			if(AbilityRegistry.hasAbility(living, AbilityWaterWalking.REGISTRY_NAME))
			{
				AbilityWaterWalking ability = (AbilityWaterWalking)AbilityRegistry.getAbilityByName(living, AbilityWaterWalking.REGISTRY_NAME);
				if(worldIn.getBlockState(pos).getFluidState() != null && ability.affectsFluid(worldIn.getBlockState(pos).getFluidState()))
					if(!(living.isSneaking() || living.isSwimming()) && pos.getY() < living.getPosition().getY())
						ci.setReturnValue(VoxelShapes.fullCube());
			}
		}
	}
	
	@Inject(
			method = "onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V", 
			at = @At("HEAD"),
			cancellable = true)
	public void onEntityCollision(World worldIn, BlockPos pos, Entity entityIn, final CallbackInfo ci)
	{
		if(entityIn instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)entityIn;
			if(!IPhasingAbility.isPhasing(living))
				return;
			
			AbilityRegistry.getAbilitiesOfType(living, IPhasingAbility.class).forEach((ability) -> 
			{
				if(ability.canPhase(worldIn, pos, living)) ci.cancel();
			});
		}
	}
}
