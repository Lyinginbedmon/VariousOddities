package com.lying.variousoddities.species.abilities;

import javax.annotation.Nullable;

import com.lying.variousoddities.init.VOMobEffects;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class AbilityDrainHealth extends AbilityGaze
{
	public AbilityDrainHealth()
	{
		super(1D, Reference.Values.TICKS_PER_SECOND * 6);
		this.needsLooking = false;
	}
	
	public Type getType(){ return Type.ATTACK; }
	
	protected Nature getDefaultNature() { return Nature.EXTRAORDINARY; }
	
	public boolean isValidTarget(@Nullable LivingEntity living, @Nullable LivingEntity owner)
	{
		return living != null && EnumCreatureType.getCustomTypes(living).isLiving() && canAbilityAffectEntity(living, owner);
	}
	
	public boolean affectTarget(LivingEntity entity, LivingEntity owner)
	{
		if(entity.hurt(DamageSource.GENERIC, 2F) && entity.addEffect(new MobEffectInstance(VOMobEffects.HEALTH_DRAIN.get(), Integer.MAX_VALUE, 1, true, false)))
		{
			owner.addEffect(new MobEffectInstance(VOMobEffects.TEMP_HP.get(), Integer.MAX_VALUE, 4, true, false));
			return true;
		}
		return false;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			AbilityDrainHealth drain = new AbilityDrainHealth();
			CompoundTag nbt = drain.writeToNBT(new CompoundTag());
			nbt.merge(compound);
			drain.readFromNBT(nbt);
			return drain;
		}
	}
}
