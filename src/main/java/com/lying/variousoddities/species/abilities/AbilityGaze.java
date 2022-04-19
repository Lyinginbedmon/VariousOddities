package com.lying.variousoddities.species.abilities;

import javax.annotation.Nullable;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.LivingData.MindControl;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.utility.VOHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;

public abstract class AbilityGaze extends ActivatedAbility
{
	private final double range;
	
	protected AbilityGaze(ResourceLocation registryName, double rangeIn, int cooldownIn)
	{
		super(registryName, cooldownIn);
		this.range = rangeIn;
	}
	
	public Type getType() { return Type.ATTACK; }
	
	public boolean isValidTarget(@Nullable LivingEntity living)
	{
		return living != null && !AbilityBlind.isMobBlind(living) && canAffect(living);
	}
	
	private LivingEntity getLookTarget(LivingEntity entity)
	{
		return range > 0 ? VOHelper.getEntityLookTarget(entity, range) : VOHelper.getEntityLookTarget(entity);
	}
	
	public boolean canTrigger(LivingEntity entity)
	{
		return super.canTrigger(entity) && isValidTarget(getLookTarget(entity));
	}
	
	public void trigger(LivingEntity entity, Dist side)
	{
		LivingEntity victim = getLookTarget(entity);
		if(!isValidTarget(victim))
			return;
		
		switch(side)
		{
			case CLIENT:
				break;
			default:
				if(affectTarget(victim, entity))
					putOnCooldown(entity);
				break;
		}
	}
	
	public abstract boolean affectTarget(LivingEntity entity, LivingEntity owner);
	
	public boolean canAffect(LivingEntity entity) { return true; }
	
	public static abstract class AbilityGazeControl extends AbilityGaze
	{
		private final MindControl type;
		
		public AbilityGazeControl(ResourceLocation registryName, MindControl typeIn, double rangeIn, int cooldownIn)
		{
			super(registryName, rangeIn, cooldownIn);
			this.type = typeIn;
		}
		
		public boolean canAffect(LivingEntity entity)
		{
			return AbilityResistanceSpell.canSpellAffectMob(entity, type.getSchool(), type.getDescriptor());
		}
		
		public boolean affectTarget(LivingEntity entity, LivingEntity owner)
		{
			LivingData data = LivingData.forEntity(entity);
			if(data.isMindControlledBy(owner, type))
				return false;
			else
			{
				data.setMindControlled(owner, Reference.Values.TICKS_PER_MINUTE * 2, type);	// FIXME Return to 7 minute duration after dev
				if(entity instanceof MobEntity)
				{
					MobEntity mob = (MobEntity)entity;
					if(mob.getAttackTarget() == owner)
						mob.setAttackTarget((LivingEntity)null);
					
					if(mob.getRevengeTarget() == owner)
						mob.setRevengeTarget((LivingEntity)null);
				}
			}
			
			return true;
		}
	}
	
	public static class Charm extends AbilityGazeControl
	{
		public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "charming_gaze");
		
		public Charm()
		{
			super(REGISTRY_NAME, MindControl.CHARMED, 9D, Reference.Values.TICKS_PER_SECOND * 10);
		}
		
		protected Nature getDefaultNature() { return Nature.SPELL_LIKE; }
		
		public static class Builder extends Ability.Builder
		{
			public Builder(){ super(REGISTRY_NAME); }
			
			public Ability create(CompoundNBT compound)
			{
				Charm charm = new Charm();
				CompoundNBT nbt = charm.writeToNBT(new CompoundNBT());
				nbt.merge(compound);
				charm.readFromNBT(nbt);
				return charm;
			}
		}
	}
	
	public static class Dominate extends AbilityGazeControl
	{
		public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "dominating_gaze");
		
		public Dominate()
		{
			super(REGISTRY_NAME, MindControl.DOMINATED, 9D, Reference.Values.TICKS_PER_SECOND * 10);
		}
		
		protected Nature getDefaultNature() { return Nature.SPELL_LIKE; }
		
		public static class Builder extends Ability.Builder
		{
			public Builder(){ super(REGISTRY_NAME); }
			
			public Ability create(CompoundNBT compound)
			{
				Dominate dominate = new Dominate();
				CompoundNBT nbt = dominate.writeToNBT(new CompoundNBT());
				nbt.merge(compound);
				dominate.readFromNBT(nbt);
				return dominate;
			}
		}
	}
}
