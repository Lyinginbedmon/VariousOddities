package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityHeat extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "heat");
	
	private final float minDmg, maxDmg;
	private final boolean ignite;
	
	private AbilityHeat(float dmgMin, float dmgMax, boolean shouldIgnite)
	{
		super(REGISTRY_NAME);
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
				if(AbilityRegistry.hasAbility(attacker, REGISTRY_NAME) && AbilityRegistry.getAbilityByName(attacker, REGISTRY_NAME).canAbilityAffectEntity(victim, attacker))
					applyHeatTo(victim, (AbilityHeat)AbilityRegistry.getAbilityByName(attacker, REGISTRY_NAME), attacker.getRandom());
				
				if(AbilityRegistry.hasAbility(victim, REGISTRY_NAME) && AbilityRegistry.getAbilityByName(victim, REGISTRY_NAME).canAbilityAffectEntity(attacker, victim))
					applyHeatTo(attacker, (AbilityHeat)AbilityRegistry.getAbilityByName(victim, REGISTRY_NAME), victim.getRandom());
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
		public Builder(){ super(REGISTRY_NAME); }
		
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
