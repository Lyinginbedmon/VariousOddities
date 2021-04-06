package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.types.EnumCreatureType;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
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
	@Shadow
	public abstract Block getBlock();
	
	@SuppressWarnings("deprecation")
	@Inject(
			method = "getCollisionShape(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/shapes/ISelectionContext;)Lnet/minecraft/util/shape/VoxelShape;", 
			at = @At("HEAD"), 
			cancellable = true)
	private void getCollisionShape(IBlockReader worldIn, BlockPos pos, ISelectionContext context, CallbackInfoReturnable<VoxelShape> ci)
	{
		System.out.println("Getting collision shape");
		VoxelShape actualShape = getBlock().getCollisionShape(worldIn.getBlockState(pos), worldIn, pos);
		if(!actualShape.isEmpty() && context instanceof EntitySelectionContext)
		{
			System.out.println("Shape not empty");
			Entity entity = ((EntitySelectionContext)context).getEntity();
			if(entity != null && entity instanceof LivingEntity)
			{
				System.out.println("Modifying shape for "+entity.getName().getUnformattedComponentText());
				TypesManager manager = TypesManager.get(entity.getEntityWorld());
				if(manager.isMobOfType((LivingEntity)entity, EnumCreatureType.INCORPOREAL))
				{
					System.out.println("Rendering block passable");
					if(pos.getY() > 1)
						ci.setReturnValue(VoxelShapes.empty());
				}
			}
		}
	}
	
	@Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
	private void onEntityCollision(World world, BlockPos pos, Entity entity, CallbackInfo ci)
	{
		if(entity instanceof LivingEntity)
		{
			TypesManager manager = TypesManager.get(world);
			if(manager.isMobOfType((LivingEntity)entity, EnumCreatureType.INCORPOREAL))
				ci.cancel();
		}
	}
}
