package com.lying.variousoddities.species.abilities;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.condition.Conditions;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.AbilityGaze.AbilityGazeControl;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;

public class AbilityFearAura extends AbilityGazeControl
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "fear_aura");
	
	public AbilityFearAura()
	{
		super(REGISTRY_NAME, Conditions.AFRAID, 9D, Reference.Values.TICKS_PER_MINUTE);
	}
	
	protected Nature getDefaultNature() { return Nature.SUPERNATURAL; }
	
	public List<LivingEntity> getValidTargets(LivingEntity entity)
	{
		List<LivingEntity> targets = entity.getEntityWorld().getEntitiesWithinAABB(LivingEntity.class, entity.getBoundingBox().grow(range, 4D, range), this::canAffect);
		List<LivingEntity> realTargets = Lists.newArrayList();
		for(LivingEntity target : targets)
			if(target != entity && !entity.isRidingOrBeingRiddenBy(target) && isValidTarget(target, entity))
				realTargets.add(target);
		return realTargets;
	}
	
	public boolean canTrigger(LivingEntity entity)
	{
		return AbilityRegistry.hasAbility(entity, getMapName()) && !LivingData.forEntity(entity).getAbilities().isAbilityOnCooldown(getMapName()) && !getValidTargets(entity).isEmpty();
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
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			AbilityFearAura fear = new AbilityFearAura();
			CompoundNBT nbt = fear.writeToNBT(new CompoundNBT());
			nbt.merge(compound);
			fear.readFromNBT(nbt);
			return fear;
		}
	}
}
