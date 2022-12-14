package com.lying.variousoddities.species.abilities;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.condition.Conditions;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.AbilityGaze.AbilityGazeControl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;

public class AbilityFearAura extends AbilityGazeControl
{
	public AbilityFearAura()
	{
		super(Conditions.AFRAID, 9D, Reference.Values.TICKS_PER_MINUTE);
	}
	
	protected Nature getDefaultNature() { return Nature.SUPERNATURAL; }
	
	public List<LivingEntity> getValidTargets(LivingEntity entity)
	{
		List<LivingEntity> targets = entity.getLevel().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(range, 4D, range), this::canAffect);
		List<LivingEntity> realTargets = Lists.newArrayList();
		for(LivingEntity target : targets)
			if(target != entity && !entity.hasPassenger(target) && !target.hasPassenger(entity) && isValidTarget(target, entity))
				realTargets.add(target);
		return realTargets;
	}
	
	public boolean canTrigger(LivingEntity entity)
	{
		return AbilityRegistry.hasAbilityOfMapName(entity, getMapName()) && !LivingData.forEntity(entity).getAbilities().isAbilityOnCooldown(getMapName()) && !getValidTargets(entity).isEmpty();
	}
	
	public void trigger(LivingEntity entity, Dist side)
	{
		List<LivingEntity> targets = getValidTargets(entity);
		if(targets.isEmpty())
			return;
		
		switch(side)
		{
			case CLIENT:
				break;
			default:
				boolean success = false;
				for(LivingEntity target : targets)
					if(affectTarget(target, entity))
						success = true;
				if(success)
					putOnCooldown(entity);
				break;
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			AbilityFearAura fear = new AbilityFearAura();
			CompoundTag nbt = fear.writeToNBT(new CompoundTag());
			nbt.merge(compound);
			fear.readFromNBT(nbt);
			return fear;
		}
	}
}
