package com.lying.variousoddities.species.abilities;

import java.util.UUID;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;

public class AbilityModifierStr extends AbilityModifier
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "strength_modifier");
	private static final UUID STRENGTH_UUID = UUID.fromString("6444940e-a83a-4a76-a2ed-1847d8e1ebd0");
	
	public AbilityModifierStr(double amountIn)
	{
		super(REGISTRY_NAME, amountIn);
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return this.amount >= 0 ? Type.ATTACK : Type.WEAKNESS; }
	
	public Component translatedName()
	{
		return Component.translatable("ability.varodd.strength_modifier", translatedAmount());
	}
	
	public Component description()
	{
		return Component.translatable("ability.varodd:strength_modifier.desc"+(amount < 0 ? ".minus" : ".plus"), (int)Math.abs(amount));
	}
	
	public void applyModifier(LivingTickEvent event)
	{
		LivingEntity entity = event.getEntity();
		AttributeInstance attribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
		if(attribute == null)
			return;
		
		if(AbilityRegistry.hasAbility(entity, getMapName()))
		{
			AbilityModifierStr strength = (AbilityModifierStr)AbilityRegistry.getAbilityByName(entity, getMapName());
			double amount = strength.amount;
			
			AttributeModifier modifier = attribute.getModifier(STRENGTH_UUID);
			if(modifier != null && modifier.getAmount() != amount)
			{
				attribute.removeModifier(STRENGTH_UUID);
				modifier = null;
			}
			
			if(modifier == null)
			{
				modifier = new AttributeModifier(STRENGTH_UUID, "strength_modifier", amount, Operation.ADDITION);
				attribute.addPermanentModifier(modifier);
			}
		}
		else if(attribute.getModifier(STRENGTH_UUID) != null)
			attribute.removeModifier(STRENGTH_UUID);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundTag compound)
		{
			double amount = compound.contains("Amount", 6) ? compound.getDouble("Amount") : 2F;
			return new AbilityModifierStr(amount);
		}
	}
}
