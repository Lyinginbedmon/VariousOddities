package com.lying.variousoddities.species.abilities;

import javax.annotation.Nonnull;

import com.lying.variousoddities.init.VOBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public interface IPhasingAbility
{
	/** Returns true if the given entity can pass through obstacles at the given position */
	public default boolean canPhase(BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull LivingEntity entity)
	{
		return entity != null && canPhaseThrough(worldIn, pos) && isPhaseable(worldIn, pos, entity);
	}
	
	public default boolean canPhaseThrough(BlockGetter worldIn, @Nonnull BlockPos pos)
	{
		if(pos.getY() <= 1)
			return false;
		
		// Deny for pre-defined blocks, such as unbreakable blocks and portals
		BlockState state = worldIn.getBlockState(pos);
		if(state.is(VOBlocks.UNPHASEABLE))
			return false;
		
		// Attempt to catch any of the above that haven't already been tagged in data
		if(state.getDestroySpeed(worldIn, pos) < 0F || state.getMaterial() == Material.PORTAL)
			return false;
		
		return true;
	}
	
	/** Returns true if the given entity can phase through the obstacles at the givne position */
	public boolean isPhaseable(BlockGetter worldIn, @Nonnull BlockPos pos, LivingEntity entity);
	
	/** Returns true if this ability prevents falling damage */
	public default boolean preventsFallDamage(Ability abilityIn){ return true; }
	
	/** Returns true if the given entity has any active phasing ability */
	public static boolean isPhasing(LivingEntity entity)
	{
		for(IPhasingAbility phasing : AbilityRegistry.getAbilitiesOfClass(entity, IPhasingAbility.class))
		{
			Ability ability = (Ability)phasing;
			if(ability.passive() || ((ActivatedAbility)ability).isActive())
				return true;
		};
		return false;
	}
}
