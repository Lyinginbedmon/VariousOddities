package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AbilityTremorsense extends AbilityVision
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "tremorsense");
	
	public AbilityTremorsense(double rangeIn)
	{
		super(REGISTRY_NAME, rangeIn);
	}
	
	public AbilityTremorsense(double rangeIn, double rangeMinIn)
	{
		this(rangeIn);
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public ITextComponent translatedName(){ return new TranslationTextComponent("ability."+Reference.ModInfo.MOD_ID+".tremorsense", (int)range); }
	
	public boolean testEntity(Entity entity, LivingEntity player)
	{
		if(entity.isOnGround())
			return true;
		else if(EnumCreatureType.getTypes(player).includesType(EnumCreatureType.AQUATIC))
			return entity.areEyesInFluid(FluidTags.WATER) || entity.getEntityWorld().getBlockState(entity.getPosition()).getFluidState().isTagged(FluidTags.WATER);
		return false;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			double range = compound.contains("Max", 6) ? compound.getDouble("Max") : 16;
			return new AbilityTremorsense(range, compound.getDouble("Min"));
		}
	}
}
