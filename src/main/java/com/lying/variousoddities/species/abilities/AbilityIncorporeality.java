package com.lying.variousoddities.species.abilities;

import javax.annotation.Nonnull;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class AbilityIncorporeality extends AbilityPhasing
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "incorporeality");
	
	public AbilityIncorporeality(){ super(REGISTRY_NAME); }
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public Type getType(){ return Ability.Type.UTILITY; }
	
	public boolean ignoresNonMagicDamage(){ return true; }
	
	public boolean isPhaseable(IBlockReader worldIn, @Nonnull BlockPos pos, LivingEntity entity)
	{
		BlockState state = worldIn.getBlockState(pos);
		
		// Phasing or not is irrelevant here, but prevents pressure plates etc. from firing
		VoxelShape collision = state.getCollisionShape(worldIn, pos);
		if(collision == null || collision == VoxelShapes.empty())
			return true;
		
		// Only allow phasing if any open side exists around the target block
		return (entity.getPosition().getY() <= pos.getY() || entity.isSneaking()) && hasOpenSide(worldIn, pos);
	}
	
	private boolean hasOpenSide(IBlockReader worldIn, @Nonnull BlockPos pos)
	{
		for(Direction direction : Direction.values())
		{
			BlockPos offset = pos.offset(direction);
			BlockState stateAtOffset = worldIn.getBlockState(offset);
			if(
				!stateAtOffset.isSolidSide(worldIn, pos, direction) ||
				stateAtOffset.getCollisionShape(worldIn, offset) == VoxelShapes.empty() ||
				stateAtOffset.getFluidState().getFluid() != Fluids.EMPTY)
				return true;
		}
		return false;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityIncorporeality();
		}
	}
}
