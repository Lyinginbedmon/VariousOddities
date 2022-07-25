package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class AbilityEtherealness extends AbilityPhasing
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "etherealness");
	
	public AbilityEtherealness()
	{
		super(REGISTRY_NAME);
	}
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public boolean ignoresNonMagicDamage(){ return true; }
	
	public boolean isPhaseable(Level worldIn, BlockPos pos, LivingEntity entity)
	{
		return entity.blockPosition().getY() <= pos.getY() || entity.isCrouching();
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityEtherealness();
		}
	}
}
