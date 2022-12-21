package com.lying.variousoddities.species.abilities;

import java.util.Map;
import java.util.UUID;

import com.lying.variousoddities.capabilities.AbilityData;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityFlight extends AbilityMoveMode implements IBonusJumpAbility
{
	public static UUID GRAVITY_UUID = UUID.fromString("8b21d611-ec03-4e91-a20c-bcb48f2c5dc1");
	private Grade quality = Grade.PERFECT;
	private double speed = 0.7D;
	
	public AbilityFlight(Grade qualityIn)
	{
		super();
		this.quality = qualityIn;
	}
	
	public AbilityFlight(Grade qualityIn, double speedIn)
	{
		this(qualityIn);
		this.speed = speedIn;
	}
	
	public int compare(Ability abilityIn)
	{
		AbilityFlight flight = (AbilityFlight)abilityIn;
		if(flight.quality != quality)
			return flight.quality.value < quality.value ? 1 : flight.quality.value > quality.value ? -1 : 0;
		return flight.speed < speed ? 1 : flight.speed > speed ? -1 : 0;
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		super.writeToNBT(compound);
		compound.putString("Quality", this.quality.getSerializedName());
		compound.putDouble("Speed", this.speed);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		super.readFromNBT(compound);
		this.quality = Grade.fromString(compound.getString("Quality"));
		this.speed = compound.contains("Speed", 6) ? compound.getDouble("Speed") : 0.5D;
	}
	
	public Component translatedName(){ return Component.translatable("ability.varodd.flying."+(isActive() ? "active" : "inactive"), quality.getSerializedName()); }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::addSlowFalling);
	}
	
	public double flySpeed(){ return this.speed; }
	
	public void addSlowFalling(LivingTickEvent event)
	{
		LivingEntity entity = event.getEntity();
		
		AttributeInstance gravity = entity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
		if(gravity == null)
			return;
		AttributeModifier mod = gravity.getModifier(GRAVITY_UUID);
		
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(entity);
		if(abilityMap.containsKey(getRegistryName()))
		{
			AbilityFlight flight = (AbilityFlight)abilityMap.get(getRegistryName());
			if(!flight.isActive() || !AbilityData.canBonusJump(entity))
			{
				if(mod != null)
					gravity.removeModifier(GRAVITY_UUID);
			}
			else
			{
				if(entity.isCrouching())
				{
					if(mod != null)
						gravity.removeModifier(GRAVITY_UUID);
				}
				else
				{
					entity.fallDistance = 0F;
					
					AttributeModifier modifier = makeModifier(flight.quality.gravity * (entity.isFallFlying() ? 0.5F : 1F));
					if(mod != null && mod.getAmount() != modifier.getAmount())
					{
						gravity.removeModifier(GRAVITY_UUID);
						gravity.addPermanentModifier(modifier);
					}
					else if(mod == null)
						gravity.addPermanentModifier(modifier);
				}
			}
		}
		else if(mod != null)
			gravity.removeModifier(GRAVITY_UUID);
	}
	
	public int getRate(){ return quality.jumpRate; }
	
	public boolean isValid(LivingEntity entity, Level world)
	{
		return isActive() && !entity.isOnGround();
	}
	
	public JumpType jumpType(){ return JumpType.AIR; }
	
	public static AttributeModifier makeModifier(double gravity)
	{
		return new AttributeModifier(GRAVITY_UUID, "gravity_modifier", -gravity, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
	
	public static class Builder extends ToggledAbility.Builder
	{
		public Builder(){ super(); }
		
		public ToggledAbility createAbility(CompoundTag compound)
		{
			return new AbilityFlight(Grade.fromString(compound.getString("Quality")), (compound.contains("Speed", 6) ? compound.getDouble("Speed") : 0.5D));
		}
	}
	
	public static enum Grade implements StringRepresentable
	{
		PERFECT(4, 0.95D, 4),
		GOOD(3, 0.71D, 3),
		AVERAGE(2, 0.48D, 2),
		POOR(1, 0.24D, 2),
		CLUMSY(0, 0D, 2);
		
		private final double gravity;
		private final int jumpRate;
		private final int value;
		
		private Grade(int valueIn, double gravIn, int jumpsIn)
		{
			value = valueIn;
			gravity = gravIn;
			jumpRate = jumpsIn;
		}
		
		public String getSerializedName(){ return this.name().toLowerCase(); }
		
		public int jumpRate(){ return Reference.Values.TICKS_PER_SECOND / this.jumpRate; }
		
		public static Grade fromString(String nameIn)
		{
			for(Grade grade : values())
				if(grade.getSerializedName().equalsIgnoreCase(nameIn))
					return grade;
			return PERFECT;
		}
	}
}
