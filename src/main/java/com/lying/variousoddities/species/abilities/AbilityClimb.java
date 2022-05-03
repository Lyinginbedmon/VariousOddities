package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class AbilityClimb extends AbilityMoveMode
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "climb");
	
	public AbilityClimb()
	{
		super(REGISTRY_NAME);
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public ITextComponent translatedName(){ return new TranslationTextComponent("ability.varodd.climb."+(isActive() ? "active" : "inactive")); }
	
	/**
	 * Returns true if the given block is not air, is solid, and does not contain a liquid
	 */
	public static boolean isClimbable(BlockPos pos, World world)
	{
		BlockState state = world.getBlockState(pos);
		return !state.getBlock().isAir(state, world, pos) && state.isSolid() && state.getFluidState().isEmpty();
	}
	
	public static class Builder extends ToggledAbility.Builder
	{
		public Builder()
		{
			super(REGISTRY_NAME);
		}
		
		public ToggledAbility createAbility(CompoundNBT compound)
		{
			return new AbilityClimb();
		}
	}
}
