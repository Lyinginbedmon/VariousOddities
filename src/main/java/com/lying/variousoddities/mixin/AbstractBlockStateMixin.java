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

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class AbstractBlockStateMixin
{
	@Inject(
			method = "getCollisionShape(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/shapes/ISelectionContext;)Lnet/minecraft/util/math/shapes/VoxelShape;",
			at = @At("HEAD"), 
			cancellable = true)
	private void getCollisionShape(BlockGetter worldIn, BlockPos pos, CollisionContext context, final CallbackInfoReturnable<VoxelShape> ci)
	{
		if(context instanceof EntityCollisionContext)
		{
			Entity entity = ((EntityCollisionContext)context).getEntity();
			if(entity != null && entity.isAlive() && entity instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entity;
				if(IPhasingAbility.isPhasing(living))
				{
					Collection<IPhasingAbility> phasings = AbilityRegistry.getAbilitiesOfType(living, IPhasingAbility.class);
					if(!phasings.isEmpty())
						phasings.forEach((ability) -> 
						{
							if(ability.canPhase(worldIn, pos, living))
								ci.setReturnValue(Shapes.empty());
						});
				}
				
				if(AbilityRegistry.hasAbility(living, AbilityWaterWalking.REGISTRY_NAME))
				{
					AbilityWaterWalking ability = (AbilityWaterWalking)AbilityRegistry.getAbilityByName(living, AbilityWaterWalking.REGISTRY_NAME);
					if(worldIn.getBlockState(pos).getFluidState() != null && ability.affectsFluid(worldIn.getBlockState(pos).getFluidState()))
						if(!(living.isCrouching() || living.isSwimming()) && pos.getY() < living.blockPosition().getY())
							ci.setReturnValue(Shapes.block());
				}
			}
		}
	}
	
	@Inject(
			method = "onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V", 
			at = @At("HEAD"),
			cancellable = true)
	public void onEntityCollision(Level worldIn, BlockPos pos, Entity entityIn, final CallbackInfo ci)
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
