package com.lying.variousoddities.species.abilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.init.VOBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public abstract class AbilityPhasing extends Ability
{
	protected AbilityPhasing(ResourceLocation registryNameIn)
	{
		super(registryNameIn);
	}
	
	public Type getType(){ return Type.UTILITY; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::ignoreNonMagicDamage);
	}
	
	public void ignoreNonMagicDamage(LivingHurtEvent event)
	{
		LivingEntity living = event.getEntityLiving();
		if(!DamageType.getDamageTypes(event.getSource()).contains(DamageType.MAGIC))
			AbilityRegistry.getAbilitiesOfType(living, AbilityPhasing.class).forEach((ability) -> { if(ability.ignoresNonMagicDamage()) event.setCanceled(true); });
	}
	
	/** Returns true if the given entity can pass through obstacles at the given position */
	public boolean canPhase(IBlockReader worldIn, @Nullable BlockPos pos, @Nonnull LivingEntity entity)
	{
		return entity != null && (pos == null ? true : canPhaseThrough(worldIn, pos) && isPhaseable(worldIn, pos, entity));
	}
	
	public boolean canPhaseThrough(IBlockReader worldIn, @Nonnull BlockPos pos)
	{
		if(pos.getY() <= 1)
			return false;
		
		// Deny for pre-defined blocks, such as unbreakable blocks and portals
		BlockState state = worldIn.getBlockState(pos);
		if(VOBlocks.UNPHASEABLE.contains(state.getBlock()))
			return false;
		
		return true;
	}
	
	/** Returns true if the given entity can phase through the obstacles at the givne position */
	protected abstract boolean isPhaseable(IBlockReader worldIn, @Nonnull BlockPos pos, LivingEntity entity);
	
	public abstract boolean ignoresNonMagicDamage();
}
