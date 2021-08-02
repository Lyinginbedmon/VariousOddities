package com.lying.variousoddities.species.abilities;

import javax.annotation.Nullable;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class AbilityStatusEffect extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "status_effect");
	public static int TIME = Reference.Values.TICKS_PER_SECOND * 15;
	
	protected EffectInstance effect = null;
	
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
	
	public EffectInstance getEffect(){ return this.effect; }
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityStatusEffect(EffectInstance.read(compound));
		}
	}
}
