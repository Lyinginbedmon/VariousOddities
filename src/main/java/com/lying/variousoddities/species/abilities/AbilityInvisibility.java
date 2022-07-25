package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class AbilityInvisibility extends AbilityStatusEffect
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "invisibility");
	
	public AbilityInvisibility()
	{
		super(REGISTRY_NAME, null);
	}
	
	public ResourceLocation getMapName(){ return getRegistryName(); }
	
	public Component translatedName(){ return Component.translatable("ability."+getMapName()); }
	
	public MobEffectInstance getEffect(){ return new MobEffectInstance(MobEffects.INVISIBILITY, Reference.Values.TICKS_PER_SECOND * 10, 0, false, true); }
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityInvisibility();
		}
	}
}
