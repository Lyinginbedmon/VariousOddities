package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityLightSensitivity extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "light_sensitivity");

	public AbilityLightSensitivity()
	{
		super(REGISTRY_NAME);
	}
	
	public Type getType(){ return Type.WEAKNESS; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::applySensitivity);
	}
	
	public void applySensitivity(LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		if(AbilityRegistry.hasAbility(entity, REGISTRY_NAME))
		{
			World world = entity.getEntityWorld();
			int light = world.getLight(entity.getPosition().add(0, entity.getEyeHeight(), 0));
			if(light == 15)
				entity.addPotionEffect(new EffectInstance(VOPotions.DAZZLED, Reference.Values.TICKS_PER_SECOND * 6, 0, false, false));
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityLightSensitivity();
		}
	}
}
