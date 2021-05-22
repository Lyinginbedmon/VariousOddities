package com.lying.variousoddities.species.abilities;

import java.util.UUID;

import com.lying.variousoddities.api.event.AbilityEvent.AbilityUpdateEvent;
import com.lying.variousoddities.capabilities.Abilities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityFlight extends AbilityMoveMode
{
	public static UUID GRAVITY_UUID = UUID.fromString("8b21d611-ec03-4e91-a20c-bcb48f2c5dc1");
	public static ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "flight");
	private Grade quality = Grade.PERFECT;
	private double speed = 0.7D;
	
	public AbilityFlight(Grade qualityIn)
	{
		super(REGISTRY_NAME);
		this.quality = qualityIn;
	}
	
	public AbilityFlight(Grade qualityIn, double speedIn)
	{
		this(qualityIn);
		this.speed = speedIn;
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		super.writeToNBT(compound);
		compound.putString("Quality", this.quality.getString());
		compound.putDouble("Speed", this.speed);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		super.readFromNBT(compound);
		this.quality = Grade.fromString(compound.getString("Quality"));
		this.speed = compound.contains("Speed", 6) ? compound.getDouble("Speed") : 0.5D;
	}
	
	public ITextComponent translatedName(){ return new TranslationTextComponent("ability.varodd.flying."+(active() ? "active" : "inactive"), quality.getString()); }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::addSlowFalling);
		bus.addListener(this::handleAirJumps);
	}
	
	public double flySpeed(){ return this.speed; }
	
	public void addSlowFalling(LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		if(AbilityRegistry.hasAbility(entity, REGISTRY_NAME))
		{
			AbilityFlight flight = (AbilityFlight)AbilityRegistry.getAbilityByName(entity, getMapName());
			
			ModifiableAttributeInstance gravity = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
			if(gravity == null)
				return;
			
			AttributeModifier mod = gravity.getModifier(GRAVITY_UUID);
			if(!flight.active())
			{
				if(mod != null)
					gravity.removeModifier(GRAVITY_UUID);
			}
			else
			{
				entity.fallDistance = 0F;
				
				if(entity.isSneaking())
				{
					if(mod != null)
						gravity.removeModifier(GRAVITY_UUID);
				}
				else
				{
					AttributeModifier modifier = makeModifier(flight.quality.gravity * (entity.isElytraFlying() ? 0.5F : 1F));
					if(mod != null && mod.getAmount() != modifier.getAmount())
					{
						gravity.removeModifier(GRAVITY_UUID);
						gravity.applyPersistentModifier(modifier);
					}
					else if(mod == null)
						gravity.applyPersistentModifier(modifier);
				}
			}
		}
	}
	
	public void handleAirJumps(AbilityUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		if(AbilityRegistry.hasAbility(entity, getMapName()))
		{
			AbilityFlight flight = (AbilityFlight)AbilityRegistry.getAbilityByName(entity, getMapName());
			if(flight.active())
			{
				Abilities abilities = event.getAbilities();
				if(!abilities.canAirJump)
					if(entity.isOnGround() || abilities.airJumpTimer++ >= flight.quality.jumpRate)
					{
						abilities.canAirJump = true;
						abilities.airJumpTimer = 0;
						abilities.markDirty();
					}
			}
		}
	}
	
	public static AttributeModifier makeModifier(double gravity)
	{
		return new AttributeModifier(GRAVITY_UUID, "gravity_modifier", -gravity, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			AbilityFlight flight = new AbilityFlight(Grade.fromString(compound.getString("Quality")), (compound.contains("Speed", 6) ? compound.getDouble("Speed") : 0.5D));
			flight.isActive = compound.getBoolean("IsActive");
			return flight;
		}
	}
	
	public static enum Grade implements IStringSerializable
	{
		PERFECT(0.95D, 4),
		GOOD(0.71D, 3),
		AVERAGE(0.48D, 2),
		POOR(0.24D, 2),
		CLUMSY(0D, 2);
		
		private final double gravity;
		private final int jumpRate;
		
		private Grade(double gravIn, int jumpsIn)
		{
			gravity = gravIn;
			jumpRate = jumpsIn;
		}
		
		public String getString(){ return this.name().toLowerCase(); }
		
		public int jumpRate(){ return Reference.Values.TICKS_PER_SECOND / this.jumpRate; }
		
		public static Grade fromString(String nameIn)
		{
			for(Grade grade : values())
				if(grade.getString().equalsIgnoreCase(nameIn))
					return grade;
			return PERFECT;
		}
	}
}
