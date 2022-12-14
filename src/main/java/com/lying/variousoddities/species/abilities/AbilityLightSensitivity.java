package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.init.VOMobEffects;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.utility.VOHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityLightSensitivity extends Ability
{
	private int lightLimit;
	
	public AbilityLightSensitivity()
	{
		this(8);
	}
	
	public AbilityLightSensitivity(int limit)
	{
		super();
		this.lightLimit = limit;
	}
	
	public Type getType(){ return Type.WEAKNESS; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.putInt("Light", this.lightLimit);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		this.lightLimit = compound.contains("Light") ? compound.getInt("Light") : 8;
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::applySensitivity);
	}
	
	public void applySensitivity(LivingTickEvent event)
	{
		LivingEntity entity = event.getEntity();
		if(AbilityRegistry.hasAbilityOfMapName(entity, getRegistryName()))
		{
			Level world = entity.getLevel();
			BlockPos eyePos = entity.blockPosition().offset(0D, entity.getEyeHeight(), 0D);
			int block = world.getBrightness(LightLayer.BLOCK, eyePos);
			int sky = VOHelper.getSkyLight(eyePos, world);
			
			int light = Math.max(block, sky);
			if(light > ((AbilityLightSensitivity)AbilityRegistry.getAbilityByMapName(entity, getRegistryName())).lightLimit)
				entity.addEffect(new MobEffectInstance(VOMobEffects.DAZZLED.get(), Reference.Values.TICKS_PER_SECOND * 6, 0, false, false));
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			return compound.contains("Light") ? new AbilityLightSensitivity(compound.getInt("Light")) : new AbilityLightSensitivity();
		}
	}
}
