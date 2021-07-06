package com.lying.variousoddities.species.abilities;

import javax.annotation.Nonnull;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
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
	public static int TIME = Reference.Values.TICKS_PER_SECOND * 5;
	
	protected EffectInstance effect;
	
	public AbilityStatusEffect(ResourceLocation registryName, @Nonnull EffectInstance effectIn)
	{
		super(registryName);
		this.effect = effectIn;
	}
	
	public AbilityStatusEffect(@Nonnull EffectInstance effectIn)
	{
		this(REGISTRY_NAME, effectIn);
	}
	
	public int compare(Ability abilityIn)
	{
		AbilityStatusEffect statusEffect = (AbilityStatusEffect)abilityIn;
		if(statusEffect.effect.getPotion() == effect.getPotion())
			return statusEffect.effect.getAmplifier() < effect.getAmplifier() ? 1 : statusEffect.effect.getAmplifier() > effect.getAmplifier() ? -1 : 0;
		return 0;
	}
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public ResourceLocation getMapName(){ return new ResourceLocation(Reference.ModInfo.MOD_ID, "status_effect_"+effect.getEffectName().toLowerCase()); }
	
	public ITextComponent translatedName()
	{
		String name = I18n.format(this.effect.getPotion().getName());
		int amp = this.effect.getAmplifier();
		if(amp >= 1 && amp <= 9)
			name += ' ' + I18n.format("enchantment.level." + (amp + 1));
		return new StringTextComponent(name);
	}
	
	public Type getType(){ return effect.getPotion().isBeneficial() ? Ability.Type.DEFENSE : Ability.Type.WEAKNESS; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		this.effect.write(compound);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		this.effect = EffectInstance.read(compound);
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::manageStatusEffect);
	}
	
	public void manageStatusEffect(LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		for(Ability ability : AbilityRegistry.getAbilitiesOfType(entity, getRegistryName()))
		{
			AbilityStatusEffect status = (AbilityStatusEffect)ability;
			EffectInstance effect = status.effect;
			Effect potion = effect.getPotion();
			boolean shouldApply = !entity.isPotionActive(potion);
			if(entity.isPotionActive(potion))
			{
				EffectInstance active = entity.getActivePotionEffect(potion);
				shouldApply = active.getAmplifier() < effect.getAmplifier() || (active.getAmplifier() == effect.getAmplifier() && active.getDuration() < TIME);
			}
			
			if(shouldApply)
				entity.addPotionEffect(new EffectInstance(effect.getPotion(), TIME, effect.getAmplifier(), effect.isAmbient(), effect.doesShowParticles()));
		}
	}
	
	public void onAbilityRemoved(LivingEntity entity)
	{
		Effect potion = effect.getPotion();
		if(entity.isPotionActive(potion) && entity.getActivePotionEffect(potion).getAmplifier() == effect.getAmplifier())
			entity.removeActivePotionEffect(potion);
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
