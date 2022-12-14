package com.lying.variousoddities.species.abilities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;

public class AbilityEtherealness extends AbilityPhasing
{
	public AbilityEtherealness()
	{
		super();
	}
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public boolean ignoresNonMagicDamage(){ return true; }
	
	public boolean isPhaseable(BlockGetter worldIn, BlockPos pos, LivingEntity entity)
	{
		return entity.blockPosition().getY() <= pos.getY() || entity.isCrouching();
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityEtherealness();
		}
	}
}
