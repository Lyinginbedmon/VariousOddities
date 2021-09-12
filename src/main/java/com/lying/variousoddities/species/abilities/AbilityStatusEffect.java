package com.lying.variousoddities.species.abilities;

import java.util.Map;

import javax.annotation.Nullable;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityStatusEffect extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "status_effect");
	public static int TIME = Reference.Values.TICKS_PER_SECOND * 15;
	
	protected EffectInstance effect = null;
	private int timer = Integer.MAX_VALUE;
	
	public AbilityStatusEffect(ResourceLocation registryName, @Nullable EffectInstance effectIn)
	{
		super(registryName);
		this.effect = effectIn;
	}
	
	public AbilityStatusEffect(@Nullable EffectInstance effectIn)
	{
		this(REGISTRY_NAME, effectIn);
	}
	
	public int compare(Ability abilityIn)
	{
		AbilityStatusEffect statusEffect = (AbilityStatusEffect)abilityIn;
		EffectInstance effectA = getEffect();
		EffectInstance effectB = statusEffect.getEffect();
		
		if(effectB.getPotion() == effectA.getPotion())
			return effectB.getAmplifier() < effectA.getAmplifier() ? 1 : effectB.getAmplifier() > effectA.getAmplifier() ? -1 : 0;
		return 0;
	}
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public ResourceLocation getMapName(){ return new ResourceLocation(Reference.ModInfo.MOD_ID, "status_effect_"+getEffect().getEffectName().toLowerCase()); }
	
	public ITextComponent translatedName()
	{
		String name = I18n.format(getEffect().getPotion().getName());
		int amp = getEffect().getAmplifier();
		if(amp >= 1 && amp <= 9)
			name += ' ' + I18n.format("enchantment.level." + (amp + 1));
		return new StringTextComponent(name);
	}
	
	public Type getType(){ return getEffect().getPotion().isBeneficial() ? Ability.Type.DEFENSE : Ability.Type.WEAKNESS; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		if(this.effect != null)
			this.effect.write(compound);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		if(!compound.isEmpty())
			this.effect = EffectInstance.read(compound);
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::applyModifiers);
	}
	
	public EffectInstance getEffect(){ return this.effect; }
	
	public void tick(LivingEntity entity)
	{
		EffectInstance effect = getEffect();
		if(effect == null)
			return;
		
		Effect potion = effect.getPotion();
		int amplifier = effect.getAmplifier();
		if(!potion.isInstant() && potion.isReady(--timer, amplifier))
			potion.performEffect(entity, amplifier);
		
		if(timer <= 0)
			timer = Integer.MAX_VALUE;
	}
	
	public void applyModifiers(LivingUpdateEvent event)
	{
		LivingEntity living = event.getEntityLiving();
		for(AbilityStatusEffect effect : AbilityRegistry.getAbilitiesOfType(living, AbilityStatusEffect.class))
		{
			EffectInstance statusEffect = effect.getEffect();
			if(statusEffect != null && !statusEffect.getPotion().getAttributeModifierMap().isEmpty())
			{
				Effect potion = statusEffect.getPotion();
				int amplifier = statusEffect.getAmplifier();
				
				// If the entity has a stronger version of this effect active, ignore this effect
				if(living.getActivePotionEffect(potion).getAmplifier() != amplifier)
					continue;
				
				Map<Attribute, AttributeModifier> attributeMap = potion.getAttributeModifierMap();
				for(Attribute attribute : attributeMap.keySet())
				{
					if(living.getAttribute(attribute) == null) continue;
					ModifiableAttributeInstance instance = living.getAttribute(attribute);
					
					AttributeModifier modifier = attributeMap.get(attribute);
					double amount = potion.getAttributeModifierAmount(amplifier, modifier);
					
					instance.removeModifier(modifier);
					instance.applyNonPersistentModifier(new AttributeModifier(modifier.getID(), potion.getName() + " " + amplifier, amount, modifier.getOperation()));
				}
			}
		}
	}
	
	public void onAbilityRemoved(LivingEntity living)
	{
		EffectInstance statusEffect = getEffect();
		if(statusEffect == null) return;
		
		Effect potion = statusEffect.getPotion();
		int amplifier = statusEffect.getAmplifier();
		if(living.isPotionActive(potion) && living.getActivePotionEffect(potion).getAmplifier() != amplifier) return;
		
		Map<Attribute, AttributeModifier> attributeMap = potion.getAttributeModifierMap();
		for(Attribute attribute : attributeMap.keySet())
		{
			if(living.getAttribute(attribute) == null) continue;
			living.getAttribute(attribute).removeModifier(attributeMap.get(attribute));
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityStatusEffect(EffectInstance.read(compound));
		}
	}
}
