package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;

public class AbilityBlind extends AbilityStatusEffect
{
	public AbilityBlind()
	{
		super(new MobEffectInstance(MobEffects.BLINDNESS, Reference.Values.TICKS_PER_SECOND * 5, 4, false, false));
	}
	
	public ResourceLocation getMapName(){ return getRegistryName(); }
	public Component translatedName(){ return Component.translatable("ability."+getMapName()); }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Ability.Type.WEAKNESS; }
	
	public static boolean isMobBlind(LivingEntity mob)
	{
		return mob.hasEffect(MobEffects.BLINDNESS) || AbilityRegistry.hasAbilityOfMapName(mob, (new AbilityBlind()).getRegistryName());
	}
	
	public static boolean canMobDetectEntity(LivingEntity mob, LivingEntity entity)
	{
		// If mob's vision of entity is compromised...
		double followRange = mob instanceof Monster ? mob.getAttributeValue(Attributes.FOLLOW_RANGE) : 4D;
		if(entity.isInvisible() && mob.distanceToSqr(entity) > followRange || isMobBlind(mob))
		{
			// Check if vision abilities can compensate
			return AbilityVision.canMobSeeEntity(mob, entity);
		}
		return true;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityBlind();
		}
	}
}
