package com.lying.variousoddities.species.abilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class AbilityDarkvision extends ToggledAbility
{
	public AbilityDarkvision()
	{
		super();
	}
	
	public ResourceLocation getMapName(){ return getRegistryName(); }
	
	public Component translatedName(){ return Component.translatable("ability."+getMapName()); }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Type.UTILITY; }
	
	public static MobEffectInstance getEffect(){ return new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false); }
	
	public static boolean isDarkvisionActive(LivingEntity entity)
	{
		ResourceLocation registryName = AbilityRegistry.getClassRegistryKey(AbilityDarkvision.class).location();
		AbilityDarkvision armour = (AbilityDarkvision)AbilityRegistry.getAbilityByMapName(entity, registryName);
		return armour != null && (entity.getType() != EntityType.PLAYER || armour.isActive());
	}
	
	public static class Builder extends ToggledAbility.Builder
	{
		public Builder(){ super(); }
		
		public ToggledAbility createAbility(CompoundTag compound)
		{
			return new AbilityDarkvision();
		}
	}
}
