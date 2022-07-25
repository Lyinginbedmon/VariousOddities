package com.lying.variousoddities.species.abilities;

import javax.annotation.Nonnull;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AbilityIncorporeality extends AbilityPhasing
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "incorporeality");
	
	public AbilityIncorporeality(){ super(REGISTRY_NAME); }
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public Type getType(){ return Ability.Type.UTILITY; }
	
	public boolean ignoresNonMagicDamage(){ return true; }
	
	public boolean isPhaseable(BlockGetter worldIn, @Nonnull BlockPos pos, LivingEntity entity)
	{
		BlockState state = worldIn.getBlockState(pos);
		
		// Phasing or not is irrelevant here, but prevents pressure plates etc. from firing
		VoxelShape collision = state.getCollisionShape(worldIn, pos);
		if(collision == null || collision.isEmpty())
			return true;
		
		// Only allow phasing if any open side exists around the target block
		return (entity.blockPosition().getY() <= pos.getY() || entity.isCrouching()) && hasOpenSide(worldIn, pos);
	}
	
	private boolean hasOpenSide(BlockGetter worldIn, @Nonnull BlockPos pos)
	{
		for(Direction direction : Direction.values())
		{
			BlockPos offset = pos.relative(direction);
			BlockState stateAtOffset = worldIn.getBlockState(offset);
			if(
				!stateAtOffset.isFaceSturdy(worldIn, pos, direction) ||
				stateAtOffset.getCollisionShape(worldIn, offset).isEmpty() ||
				!stateAtOffset.getFluidState().isEmpty())
				return true;
		}
		return false;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityIncorporeality();
		}
	}
}
