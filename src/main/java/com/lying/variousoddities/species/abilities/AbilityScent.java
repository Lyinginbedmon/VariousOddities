package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class AbilityScent extends ToggledAbility 
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "scent");
	
	private double range = 16D;
	
	public AbilityScent()
	{
		this(16D);
	}
	
	public AbilityScent(double rangeIn)
	{
		super(REGISTRY_NAME, Reference.Values.TICKS_PER_SECOND);
		this.range = rangeIn;
	}
	
	public int compare(Ability abilityIn)
	{
		AbilityScent scent = (AbilityScent)abilityIn;
		return scent.range < this.range ? 1 : scent.range > this.range ? -1 : 0;
	}
	
	public Type getType(){ return Type.UTILITY; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Component translatedName(){ return Component.translatable("ability."+Reference.ModInfo.MOD_ID+".scent", (int)range); }
	
	public boolean isInRange(Vec3 position, LivingEntity owner){ return owner.distanceToSqr(position) <= (range * range); }
	
	public double range(){ return this.range; }
	
	public static class Builder extends ToggledAbility.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public ToggledAbility createAbility(CompoundTag compound)
		{
			return new AbilityScent(compound.contains("Range", 6) ? compound.getDouble("Range") : 16D);
		}
	}
}
