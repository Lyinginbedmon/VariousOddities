package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AbilityClimb extends AbilityMoveMode
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "climb");
	
	public AbilityClimb()
	{
		super(REGISTRY_NAME);
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Component translatedName(){ return Component.translatable("ability.varodd.climb."+(isActive() ? "active" : "inactive")); }
	
	/**
	 * Returns true if the given block is not air, is solid, and does not contain a liquid
	 */
	public static boolean isClimbable(BlockPos pos, Level world)
	{
		BlockState state = world.getBlockState(pos);
		return !world.isEmptyBlock(pos) && state.isCollisionShapeFullBlock(world, pos) && state.getFluidState().isEmpty();
	}
	
	public static class Builder extends ToggledAbility.Builder
	{
		public Builder()
		{
			super(REGISTRY_NAME);
		}
		
		public ToggledAbility createAbility(CompoundTag compound)
		{
			return new AbilityClimb();
		}
	}
}
