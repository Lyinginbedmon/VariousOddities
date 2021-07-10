package com.lying.variousoddities.species.abilities;

import java.util.UUID;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class AbilityStability extends AbilityModifier
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "stability");
	private static final UUID STABILITY_UUID = UUID.fromString("040fcbf0-c7f7-4849-b104-18c472680f63");
	
	public AbilityStability()
	{
		super(REGISTRY_NAME, 0.2F);
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Type.DEFENSE; }
	
	public void applyModifier(LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		ModifiableAttributeInstance attribute = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
		if(attribute == null)
			return;
		
		if(AbilityRegistry.hasAbility(entity, getMapName()))
		{
			AbilityStability armour = (AbilityStability)AbilityRegistry.getAbilityByName(entity, getMapName());
			double amount = armour.amount;
			
			AttributeModifier modifier = attribute.getModifier(STABILITY_UUID);
			if(modifier != null && modifier.getAmount() != amount)
			{
				attribute.removeModifier(STABILITY_UUID);
				modifier = null;
			}
			
			if(modifier == null)
			{
				modifier = new AttributeModifier(STABILITY_UUID, "stability", amount, Operation.ADDITION);
				attribute.applyPersistentModifier(modifier);
			}
		}
		else if(attribute.getModifier(STABILITY_UUID) != null)
			attribute.removeModifier(STABILITY_UUID);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityStability();
		}
	}
}
