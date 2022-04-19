package com.lying.variousoddities.species.abilities;

import javax.annotation.Nullable;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

public class AbilityDrainHealth extends AbilityGaze
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "drain_health");

	public AbilityDrainHealth()
	{
		super(REGISTRY_NAME, 1D, Reference.Values.TICKS_PER_SECOND * 6);
	}
	
	public Type getType(){ return Type.ATTACK; }
	
	protected Nature getDefaultNature() { return Nature.EXTRAORDINARY; }
	
	public boolean isValidTarget(@Nullable LivingEntity living)
	{
		return living != null && EnumCreatureType.getCustomTypes(living).isLiving();
	}
	
	public boolean affectTarget(LivingEntity entity, LivingEntity owner)
	{
		if(entity.attackEntityFrom(DamageSource.GENERIC, 2F) && entity.addPotionEffect(new EffectInstance(VOPotions.HEALTH_DRAIN, Integer.MAX_VALUE, 1, true, false)))
		{
			owner.addPotionEffect(new EffectInstance(VOPotions.TEMP_HP, Integer.MAX_VALUE, 4, true, false));
			return true;
		}
		return false;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			AbilityDrainHealth drain = new AbilityDrainHealth();
			CompoundNBT nbt = drain.writeToNBT(new CompoundNBT());
			nbt.merge(compound);
			drain.readFromNBT(nbt);
			return drain;
		}
	}
}
