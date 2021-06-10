package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class AbilityEtherealness extends AbilityPhasing
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "etherealness");
	
	public AbilityEtherealness()
	{
		super(REGISTRY_NAME);
	}
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public boolean ignoresNonMagicDamage(){ return true; }
	
	public boolean isPhaseable(IBlockReader worldIn, BlockPos pos, LivingEntity entity)
	{
		return entity.getPosition().getY() <= pos.getY() || entity.isSneaking();
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityEtherealness();
		}
	}
}
