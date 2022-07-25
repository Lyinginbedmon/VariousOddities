package com.lying.variousoddities.species.abilities;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityPoison extends AbilityMeleeDamage
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "poison");
	
	// TODO Allow activated version of Poison ability to apply poison to held item
	
	private float triggerChance = 0.65F;
	private MobEffectInstance[] effects = {new MobEffectInstance(MobEffects.POISON, Reference.Values.TICKS_PER_SECOND * 7)};
	
	public AbilityPoison()
	{
		this(1F, new MobEffectInstance(MobEffects.POISON, Reference.Values.TICKS_PER_SECOND * 2));
	}
	
	public AbilityPoison(float chanceIn, MobEffectInstance... effectsIn)
	{
		super(REGISTRY_NAME);
		this.triggerChance = chanceIn;
		this.effects = effectsIn;
	}
	
	public Type getType(){ return Type.ATTACK; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.putFloat("Chance", this.triggerChance);
		
		ListTag effectsList = new ListTag();
		for(MobEffectInstance effect : this.effects)
			effectsList.add(effect.save(new CompoundTag()));
		compound.put("MobEffects", effectsList);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		this.triggerChance = compound.getFloat("Chance");
		ListTag list = compound.getList("MobEffects", 10);
		List<MobEffectInstance> effectList = Lists.newArrayList();
		for(int i=0; i<list.size(); i++)
		{
			MobEffectInstance effect = MobEffectInstance.load(list.getCompound(i));
			if(effect != null)
				effectList.add(effect);
		}
		this.effects = effectList.toArray(new MobEffectInstance[0]);
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::onLivingAttack);
	}
	
	public void onLivingAttack(LivingAttackEvent event)
	{
		DamageSource source = event.getSource();
		LivingEntity victim = event.getEntity();
		if(isValidDamageSource(source))
		{
			LivingEntity attacker = (LivingEntity)source.getDirectEntity();
			if(attacker != null && victim != attacker && AbilityRegistry.hasAbility(attacker, getMapName()) && canAbilityAffectEntity(victim, attacker))
			{
				AbilityPoison poison = (AbilityPoison)AbilityRegistry.getAbilityByName(attacker, getMapName());
				if(attacker.getRandom().nextFloat() < poison.triggerChance)
					for(MobEffectInstance effect : poison.effects)
					{
						MobEffectInstance instance = new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.isVisible());
						victim.addEffect(instance);
					}
			}
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundTag compound)
		{
			float chance = compound.contains("Chance", 5) ? compound.getFloat("Chance") : 0.15F;
			
			MobEffectInstance[] effects = {new MobEffectInstance(MobEffects.POISON, Reference.Values.TICKS_PER_SECOND * 15)};
			if(compound.contains("MobEffects", 9))
			{
				ListTag list = compound.getList("MobEffects", 10);
				List<MobEffectInstance> effectList = Lists.newArrayList();
				for(int i=0; i<list.size(); i++)
				{
					MobEffectInstance effect = MobEffectInstance.load(list.getCompound(i));
					if(effect != null)
						effectList.add(effect);
				}
				effects = effectList.toArray(new MobEffectInstance[0]);
			}
			
			return new AbilityPoison(chance, effects);
		}
	}
}
