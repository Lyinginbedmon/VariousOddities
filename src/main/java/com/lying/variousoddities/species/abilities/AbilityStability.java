package com.lying.variousoddities.species.abilities;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;

public class AbilityStability extends AbilityModifier
{
	private static final UUID STABILITY_UUID = UUID.fromString("040fcbf0-c7f7-4849-b104-18c472680f63");
	
	public AbilityStability()
	{
		super(0.2F);
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Type.DEFENSE; }
	
	public void applyModifier(LivingTickEvent event)
	{
		LivingEntity entity = event.getEntity();
		AttributeInstance attribute = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
		if(attribute == null)
			return;
		
		if(AbilityRegistry.hasAbilityOfMapName(entity, getMapName()))
		{
			AbilityStability armour = (AbilityStability)AbilityRegistry.getAbilityByMapName(entity, getMapName());
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
				attribute.addPermanentModifier(modifier);
			}
		}
		else if(attribute.getModifier(STABILITY_UUID) != null)
			attribute.removeModifier(STABILITY_UUID);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityStability();
		}
	}
}
