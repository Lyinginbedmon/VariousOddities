package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class AbilityTremorsense extends AbilityVision
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "tremorsense");
	
	private AbilityTremorsense()
	{
		this(0D);
	}
	
	public AbilityTremorsense(double rangeIn)
	{
		super(REGISTRY_NAME, rangeIn);
	}
	
	public AbilityTremorsense(double rangeIn, double rangeMinIn)
	{
		this(rangeIn);
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Component translatedName(){ return Component.translatable("ability."+Reference.ModInfo.MOD_ID+".tremorsense", (int)range); }
	
	@SuppressWarnings("deprecation")
	public boolean testEntity(Entity entity, LivingEntity player)
	{
		if(entity.isOnGround())
			return true;
		else if(EnumCreatureType.getTypes(player).includesType(EnumCreatureType.AQUATIC))
			return entity.isEyeInFluid(FluidTags.WATER) || entity.getLevel().getBlockState(entity.blockPosition()).getFluidState().is(FluidTags.WATER);
		return false;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundTag compound)
		{
			Ability ability = new AbilityTremorsense();
			ability.readFromNBT(compound);
			return ability;
		}
	}
}
