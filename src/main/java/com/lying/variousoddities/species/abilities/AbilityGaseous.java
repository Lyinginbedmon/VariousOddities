package com.lying.variousoddities.species.abilities;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class AbilityGaseous extends AbilityPhasing implements ICompoundAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "gaseous_form");
	
	public static final AbilityDamageReduction DAMAGE_REDUCTION = new AbilityDamageReduction(10, DamageType.MAGIC);
	
	public AbilityGaseous()
	{
		super(REGISTRY_NAME);
	}
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public List<Ability> getSubAbilities()
	{
		return Lists.newArrayList(new AbilityDamageReduction(DAMAGE_REDUCTION.getAmount(), DamageType.MAGIC));
	}
	
	public boolean ignoresNonMagicDamage(){ return false; }
	
	public boolean isPhaseable(IBlockReader worldIn, BlockPos pos, LivingEntity entity)
	{
		return false;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityGaseous();
		}
	}
}
