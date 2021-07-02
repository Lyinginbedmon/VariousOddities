package com.lying.variousoddities.species.abilities;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityPoison extends AbilityMeleeDamage
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "poison");
	
	// TODO Allow activated version of Poison ability to apply poison to held item
	
	private float triggerChance = 0.65F;
	private EffectInstance[] effects = {new EffectInstance(Effects.POISON, Reference.Values.TICKS_PER_SECOND * 7)};
	
	public AbilityPoison(float chanceIn, EffectInstance... effectsIn)
	{
		super(REGISTRY_NAME);
		this.triggerChance = chanceIn;
		this.effects = effectsIn;
	}
	
	public Type getType(){ return Type.ATTACK; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.putFloat("Chance", this.triggerChance);
		
		ListNBT effectsList = new ListNBT();
		for(EffectInstance effect : this.effects)
			effectsList.add(effect.write(new CompoundNBT()));
		compound.put("Effects", effectsList);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		this.triggerChance = compound.getFloat("Chance");
		ListNBT list = compound.getList("Effects", 10);
		List<EffectInstance> effectList = Lists.newArrayList();
		for(int i=0; i<list.size(); i++)
		{
			EffectInstance effect = EffectInstance.read(list.getCompound(i));
			if(effect != null)
				effectList.add(effect);
		}
		this.effects = effectList.toArray(new EffectInstance[0]);
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::onLivingAttack);
	}
	
	public void onLivingAttack(LivingAttackEvent event)
	{
		DamageSource source = event.getSource();
		LivingEntity victim = event.getEntityLiving();
		if(isValidDamageSource(source))
		{
			LivingEntity attacker = (LivingEntity)source.getImmediateSource();
			if(attacker != null && victim != attacker && AbilityRegistry.hasAbility(attacker, getMapName()))
			{
				AbilityPoison poison = (AbilityPoison)AbilityRegistry.getAbilityByName(attacker, getMapName());
				if(attacker.getRNG().nextFloat() < poison.triggerChance)
					for(EffectInstance effect : poison.effects)
					{
						EffectInstance instance = new EffectInstance(effect.getPotion(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.doesShowParticles());
						victim.addPotionEffect(instance);
					}
			}
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			float chance = compound.contains("Chance", 5) ? compound.getFloat("Chance") : 0.15F;
			
			EffectInstance[] effects = {new EffectInstance(Effects.POISON, Reference.Values.TICKS_PER_SECOND * 15)};
			if(compound.contains("Effects", 9))
			{
				ListNBT list = compound.getList("Effects", 10);
				List<EffectInstance> effectList = Lists.newArrayList();
				for(int i=0; i<list.size(); i++)
				{
					EffectInstance effect = EffectInstance.read(list.getCompound(i));
					if(effect != null)
						effectList.add(effect);
				}
				effects = effectList.toArray(new EffectInstance[0]);
			}
			
			return new AbilityPoison(chance, effects);
		}
	}
}
