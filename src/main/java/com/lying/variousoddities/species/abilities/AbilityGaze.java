package com.lying.variousoddities.species.abilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.condition.Condition;
import com.lying.variousoddities.condition.ConditionInstance;
import com.lying.variousoddities.condition.Conditions;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.utility.VOHelper;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;

public abstract class AbilityGaze extends ActivatedAbility
{
	protected double range = 9D;
	protected boolean needsLooking = true;
	
	protected AbilityGaze(ResourceLocation registryName, double rangeIn, int cooldownIn)
	{
		super(registryName, cooldownIn);
		this.range = rangeIn;
	}
	
	public Type getType() { return Type.ATTACK; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		super.writeToNBT(compound);
		if(this.range > 0)
			compound.putDouble("Range", this.range);
		
		if(!this.needsLooking)
			compound.putBoolean("NeedsLooking", this.needsLooking);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		super.readFromNBT(compound);
		if(compound.contains("Range"))
			this.range = compound.getDouble("Range");
		
		if(compound.contains("NeedsLooking"))
			this.needsLooking = compound.getBoolean("NeedsLooking");
	}
	
	public boolean isValidTarget(@Nullable LivingEntity living, @Nonnull LivingEntity owner)
	{
		if(living != null && !owner.isInvisible() && canAbilityAffectEntity(living, owner))
		{
			// Ignore players not currently functioning normally (to prevent detached spirits or unconscious minds being affected)
			if(living.getType() == EntityType.PLAYER && !PlayerData.isPlayerNormalFunction(living))
				return false;
			
			if(!AbilityBlind.isMobBlind(living) && canAffect(living))
			{
				if(!needsLooking)
					return true;
				
				// Is target look vector close enough to ability owner?
				Vector3d lookVec = living.getLook(1.0F).normalize();
				Vector3d eyeVec = living.getEyePosition(1F);
				AxisAlignedBB box = owner.getBoundingBox();
				
				Vector3d lookEnd = eyeVec.add(lookVec.scale(range));
				return box.intersects(Math.min(eyeVec.x, lookEnd.x), Math.min(eyeVec.y, lookEnd.y), Math.min(eyeVec.z, lookEnd.z), Math.max(eyeVec.x, lookEnd.x), Math.max(eyeVec.y, lookEnd.y), Math.max(eyeVec.z, lookEnd.z));
			}
		}
		return false;
	}
	
	private LivingEntity getLookTarget(LivingEntity entity)
	{
		return range > 0 ? VOHelper.getEntityLookTarget(entity, range) : VOHelper.getEntityLookTarget(entity);
	}
	
	public boolean canTrigger(LivingEntity entity)
	{
		return super.canTrigger(entity) && isValidTarget(getLookTarget(entity), entity);
	}
	
	public void trigger(LivingEntity entity, Dist side)
	{
		LivingEntity victim = getLookTarget(entity);
		if(!isValidTarget(victim, entity))
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
		private final Condition condition;
		private int durationMin = Reference.Values.TICKS_PER_MINUTE * 2;
		private int durationMax = durationMin;
		
		public AbilityGazeControl(ResourceLocation registryName, Condition conditionIn, double rangeIn, int cooldownIn)
		{
			super(registryName, rangeIn, cooldownIn);
			this.condition = conditionIn;
		}
		
		public CompoundNBT writeToNBT(CompoundNBT compound)
		{
			super.writeToNBT(compound);
			if(isDurationVariable())
			{
				compound.putInt("DurationMin", Math.min(durationMin, durationMax));
				compound.putInt("DurationMax", Math.max(durationMin, durationMax));
			}
			else
				compound.putInt("Duration", durationMin);
			return compound;
		}
		
		public void readFromNBT(CompoundNBT compound)
		{
			super.readFromNBT(compound);
			if(compound.contains("Duration"))
				this.durationMin = this.durationMax = compound.getInt("Duration");
			else if(compound.contains("DurationMin"))
			{
				this.durationMin = compound.getInt("DurationMin");
				this.durationMax = compound.getInt("DurationMax");
			}
		}
		
		public boolean canAffect(LivingEntity entity)
		{
			return condition.canAffect(entity);
		}
		
		public boolean affectTarget(LivingEntity entity, LivingEntity owner)
		{
			LivingData data = LivingData.forEntity(entity);
			if(data.hasCondition(condition, owner))
				return false;
			else
			{
				int duration = isDurationVariable() ? owner.getRNG().nextInt(this.durationMin, this.durationMax) : this.durationMax;
				data.addCondition(new ConditionInstance(condition, duration, owner.getUniqueID()));
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
		
		protected boolean isDurationVariable() { return this.durationMax != this.durationMin; }
	}
	
	public static class Petrify extends AbilityGaze
	{
		public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "petrifying_gaze");
		
		public Petrify()
		{
			super(REGISTRY_NAME, 9D, Reference.Values.TICKS_PER_SECOND * 30);
		}
		
		protected Nature getDefaultNature() { return Nature.SUPERNATURAL; }
		
		public boolean affectTarget(LivingEntity entity, LivingEntity owner)
		{
			return entity.addPotionEffect(new EffectInstance(VOPotions.PETRIFYING, Reference.Values.TICKS_PER_SECOND * 10, 4));
		}
		
		public static class Builder extends Ability.Builder
		{
			public Builder(){ super(REGISTRY_NAME); }
			
			public Ability create(CompoundNBT compound)
			{
				Petrify petrify = new Petrify();
				CompoundNBT nbt = petrify.writeToNBT(new CompoundNBT());
				nbt.merge(compound);
				petrify.readFromNBT(nbt);
				return petrify;
			}
		}
	}
	
	public static class Charm extends AbilityGazeControl
	{
		public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "charming_gaze");
		
		public Charm()
		{
			super(REGISTRY_NAME, Conditions.CHARMED, 9D, Reference.Values.TICKS_PER_SECOND * 10);
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
			super(REGISTRY_NAME, Conditions.DOMINATED, 9D, Reference.Values.TICKS_PER_MINUTE);
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
