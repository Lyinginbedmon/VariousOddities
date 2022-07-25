package com.lying.variousoddities.species.abilities;

import java.util.Map;

import javax.annotation.Nullable;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityStatusEffect extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "status_effect");
	public static int TIME = Reference.Values.TICKS_PER_SECOND * 15;
	
	protected MobEffectInstance effect = null;
	private int timer = Integer.MAX_VALUE;
	
	public AbilityStatusEffect(ResourceLocation registryName, @Nullable MobEffectInstance effectIn)
	{
		super(registryName);
		this.effect = effectIn;
	}
	
	public AbilityStatusEffect(@Nullable MobEffectInstance effectIn)
	{
		this(REGISTRY_NAME, effectIn);
	}
	
	public int compare(Ability abilityIn)
	{
		AbilityStatusEffect statusEffect = (AbilityStatusEffect)abilityIn;
		MobEffectInstance effectA = getEffect();
		MobEffectInstance effectB = statusEffect.getEffect();
		
		if(effectB.getEffect() == effectA.getEffect())
			return effectB.getAmplifier() < effectA.getAmplifier() ? 1 : effectB.getAmplifier() > effectA.getAmplifier() ? -1 : 0;
		return 0;
	}
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public ResourceLocation getMapName(){ return new ResourceLocation(Reference.ModInfo.MOD_ID, "status_effect_"+getEffect().getEffect().getDisplayName().getString().toLowerCase()); }
	
	public Component translatedName()
	{
		MutableComponent name = (MutableComponent)getEffect().getEffect().getDisplayName();
		int amp = getEffect().getAmplifier();
		if(amp >= 1 && amp <= 9)
		{
			name.append(" ");
			name.append(Component.translatable("enchantment.level." + (amp + 1)));
		}
		return name;
	}
	
	public Type getType(){ return getEffect().getEffect().isBeneficial() ? Ability.Type.DEFENSE : Ability.Type.WEAKNESS; }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		if(this.effect != null)
			this.effect.save(compound);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		if(!compound.isEmpty())
			this.effect = MobEffectInstance.load(compound);
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::applyModifiers);
	}
	
	public MobEffectInstance getEffect(){ return this.effect; }
	
	public void tick(LivingEntity entity)
	{
		MobEffectInstance effect = getEffect();
		if(effect == null)
			return;
		
		MobEffect potion = effect.getEffect();
		int amplifier = effect.getAmplifier();
		if(!potion.isInstantenous() && potion.isDurationEffectTick(--timer, amplifier))
			potion.applyEffectTick(entity, amplifier);
		
		if(timer <= 0)
			timer = Integer.MAX_VALUE;
	}
	
	public void applyModifiers(LivingTickEvent event)
	{
		LivingEntity living = event.getEntity();
		for(AbilityStatusEffect effect : AbilityRegistry.getAbilitiesOfType(living, AbilityStatusEffect.class))
		{
			MobEffectInstance statusEffect = effect.getEffect();
			if(statusEffect != null && !statusEffect.getEffect().getAttributeModifiers().isEmpty())
			{
				MobEffect potion = statusEffect.getEffect();
				int amplifier = statusEffect.getAmplifier();
				
				// If the entity has a stronger version of this effect active, ignore this effect
				if(living.getEffect(potion).getAmplifier() != amplifier)
					continue;
				
				Map<Attribute, AttributeModifier> attributeMap = potion.getAttributeModifiers();
				for(Attribute attribute : attributeMap.keySet())
				{
					if(living.getAttribute(attribute) == null) continue;
					AttributeInstance instance = living.getAttribute(attribute);
					
					AttributeModifier modifier = attributeMap.get(attribute);
					double amount = potion.getAttributeModifierValue(amplifier, modifier);
					
					instance.removeModifier(modifier);
					instance.addTransientModifier(new AttributeModifier(modifier.getId(), potion.getDisplayName() + " " + amplifier, amount, modifier.getOperation()));
				}
			}
		}
	}
	
	public void onAbilityRemoved(LivingEntity living)
	{
		MobEffectInstance statusEffect = getEffect();
		if(statusEffect == null) return;
		
		MobEffect potion = statusEffect.getEffect();
		int amplifier = statusEffect.getAmplifier();
		if(living.hasEffect(potion) && living.getEffect(potion).getAmplifier() != amplifier) return;
		
		Map<Attribute, AttributeModifier> attributeMap = potion.getAttributeModifiers();
		for(Attribute attribute : attributeMap.keySet())
		{
			if(living.getAttribute(attribute) == null) continue;
			living.getAttribute(attribute).removeModifier(attributeMap.get(attribute));
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityStatusEffect(MobEffectInstance.load(compound));
		}
	}
}
