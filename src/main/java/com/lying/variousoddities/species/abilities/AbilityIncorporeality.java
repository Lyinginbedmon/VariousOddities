package com.lying.variousoddities.species.abilities;

import javax.annotation.Nonnull;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
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
	
	public Type getType(){ return Ability.Type.UTILITY; }
	
	public boolean ignoresNonMagicDamage(){ return true; }
	
	protected boolean isPhaseable(IBlockReader worldIn, @Nonnull BlockPos pos, LivingEntity entity)
	{
		BlockState state = worldIn.getBlockState(pos);
		
		// Attempt to catch any of the above that haven't already been tagged in data
		if(state.getBlockHardness(worldIn, pos) < 0F || state.getMaterial() == Material.PORTAL)
			return false;
		
		// Phasing or not is irrelevant here, but prevents pressure plates etc. from firing
		VoxelShape collision = state.getCollisionShape(worldIn, pos);
		if(collision == null || collision == VoxelShapes.empty())
			return true;
		
		// Lastly, only allow phasing if any open side exists around the target block
		double blockTop = pos.getY() + collision.getBoundingBox().maxY;
		if((entity.getPosY() + 0.5D) <= blockTop)
			for(Direction direction : Direction.values())
			{
				BlockPos offset = pos.offset(direction);
				BlockState stateAtOffset = worldIn.getBlockState(offset);
				if(stateAtOffset.getCollisionShape(worldIn, offset) == VoxelShapes.empty() || stateAtOffset.getFluidState().getFluid() != Fluids.EMPTY)
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
