package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AbilityBlind extends AbilityStatusEffect
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "blind");
	
	public AbilityBlind()
	{
		super(REGISTRY_NAME, new EffectInstance(Effects.BLINDNESS, Reference.Values.TICKS_PER_SECOND * 5, 4, false, false));
	}
	
	public ResourceLocation getMapName(){ return REGISTRY_NAME; }
	public ITextComponent translatedName(){ return new TranslationTextComponent("ability."+getMapName()); }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Ability.Type.WEAKNESS; }
	
	public static boolean canMobDetectEntity(LivingEntity mob, LivingEntity entity)
	{
		// If mob's vision of entity is compromised...
		double followRange = mob instanceof MobEntity ? mob.getAttributeValue(Attributes.FOLLOW_RANGE) : 4D;
		if(entity.isInvisible() && mob.getDistanceSq(entity) > followRange || mob.isPotionActive(Effects.BLINDNESS) || AbilityRegistry.hasAbility(mob, REGISTRY_NAME))
		{
			// Check if vision abilities can compensate
			return AbilityVision.canMobSeeEntity(mob, entity);
		}
		return true;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityBlind();
		}
	}
}
