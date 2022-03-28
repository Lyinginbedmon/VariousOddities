package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AbilityDarkvision extends ToggledAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "darkvision");
	
	public AbilityDarkvision()
	{
		super(REGISTRY_NAME);
	}
	
	public ResourceLocation getMapName(){ return getRegistryName(); }
	
	public ITextComponent translatedName(){ return new TranslationTextComponent("ability."+getMapName()); }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Type.UTILITY; }
	
	public static EffectInstance getEffect(){ return new EffectInstance(Effects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false); }
	
	public static boolean isDarkvisionActive(LivingEntity entity)
	{
		return AbilityRegistry.hasAbility(entity, AbilityDarkvision.REGISTRY_NAME) && (entity.getType() != EntityType.PLAYER || AbilityRegistry.getAbilityByName(entity, AbilityDarkvision.REGISTRY_NAME).isActive());
	}
	
	public static class Builder extends ToggledAbility.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public ToggledAbility createAbility(CompoundNBT compound)
		{
			return new AbilityDarkvision();
		}
	}
}
