package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class AbilityDarkvision extends ToggledAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "darkvision");
	
	public AbilityDarkvision()
	{
		super(REGISTRY_NAME);
	}
	
	public ResourceLocation getMapName(){ return getRegistryName(); }
	
	public Component translatedName(){ return Component.translatable("ability."+getMapName()); }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Type.UTILITY; }
	
	public static MobEffectInstance getEffect(){ return new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false); }
	
	public static boolean isDarkvisionActive(LivingEntity entity)
	{
		return AbilityRegistry.hasAbility(entity, AbilityDarkvision.REGISTRY_NAME) && (entity.getType() != EntityType.PLAYER || AbilityRegistry.getAbilityByName(entity, AbilityDarkvision.REGISTRY_NAME).isActive());
	}
	
	public static class Builder extends ToggledAbility.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public ToggledAbility createAbility(CompoundTag compound)
		{
			return new AbilityDarkvision();
		}
	}
}
