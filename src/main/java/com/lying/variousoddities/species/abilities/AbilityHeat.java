package com.lying.variousoddities.species.abilities;

import java.util.Random;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
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
	
	public Type getType(){ return Ability.Type.ATTACK; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
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
		LivingEntity victim = event.getEntityLiving();
		if(source.getImmediateSource() != null && source.getImmediateSource() instanceof LivingEntity && source.getImmediateSource().isAlive())
		{
			LivingEntity attacker = (LivingEntity)source.getImmediateSource();
			if(attacker != null)
			{
				if(AbilityRegistry.hasAbility(attacker, REGISTRY_NAME))
					applyHeatTo(victim, (AbilityHeat)AbilityRegistry.getAbilityByName(attacker, REGISTRY_NAME), attacker.getRNG());
				
				if(AbilityRegistry.hasAbility(victim, REGISTRY_NAME))
					applyHeatTo(attacker, (AbilityHeat)AbilityRegistry.getAbilityByName(victim, REGISTRY_NAME), victim.getRNG());
			}
		}
	}
	
	private void applyHeatTo(LivingEntity target, AbilityHeat ability, Random rand)
	{
		target.attackEntityFrom(DamageSource.IN_FIRE, ability.minDmg + rand.nextFloat() * (ability.maxDmg - ability.minDmg));
		if(ability.ignite && rand.nextInt(6) == 0)
			target.setFire(5);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
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
