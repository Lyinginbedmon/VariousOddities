package com.lying.variousoddities.species.abilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityHeat extends Ability
{
	private final float minDmg, maxDmg;
	private final boolean ignite;
	
	private AbilityHeat(float dmgMin, float dmgMax, boolean shouldIgnite)
	{
		super();
		minDmg = dmgMin;
		maxDmg = dmgMax;
		ignite = shouldIgnite;
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Ability.Type.ATTACK; }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.putFloat("DmgMin", this.minDmg);
		compound.putFloat("DmgMax", this.maxDmg);
		compound.putBoolean("Ignite", this.ignite);
		return compound;
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::onLivingAttack);
	}
	
	public void onLivingAttack(LivingAttackEvent event)
	{
		DamageSource source = event.getSource();
		LivingEntity victim = event.getEntity();
		if(source.getDirectEntity() != null && source.getDirectEntity() instanceof LivingEntity && source.getDirectEntity().isAlive())
		{
			LivingEntity attacker = (LivingEntity)source.getDirectEntity();
			if(attacker != null)
			{
				ResourceLocation heatKey = getRegistryName();
				AbilityHeat attackerHeat = (AbilityHeat)AbilityRegistry.getAbilityByMapName(attacker, heatKey);
				if(attackerHeat != null && attackerHeat.canAbilityAffectEntity(victim, attacker))
					applyHeatTo(victim, (AbilityHeat)AbilityRegistry.getAbilityByMapName(attacker, getRegistryName()), attacker.getRandom());
				
				AbilityHeat victimHeat = (AbilityHeat)AbilityRegistry.getAbilityByMapName(attacker, heatKey);
				if(victimHeat != null && victimHeat.canAbilityAffectEntity(attacker, victim))
					applyHeatTo(attacker, (AbilityHeat)AbilityRegistry.getAbilityByMapName(victim, getRegistryName()), victim.getRandom());
			}
		}
	}
	
	private void applyHeatTo(LivingEntity target, AbilityHeat ability, RandomSource rand)
	{
		target.hurt(DamageSource.IN_FIRE, ability.minDmg + rand.nextFloat() * (ability.maxDmg - ability.minDmg));
		if(ability.ignite && rand.nextInt(6) == 0)
			target.setSecondsOnFire(5);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			float dmgA = compound.contains("DmgMin", 5) ? compound.getFloat("DmgMin") : 0F;
			float dmgB = compound.contains("DmgMax", 5) ? compound.getFloat("DmgMax") : 2F;
			boolean shouldIgnite = compound.contains("Ignite") ? compound.getBoolean("Ignite") : false;
			
			float min = Math.min(dmgA, dmgB);
			float max = Math.max(dmgA, dmgB);
			
			return new AbilityHeat(min, max, shouldIgnite);
		}
	}
}
