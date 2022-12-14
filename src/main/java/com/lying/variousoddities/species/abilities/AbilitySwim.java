package com.lying.variousoddities.species.abilities;

import java.util.UUID;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilitySwim extends Ability implements IBonusJumpAbility
{
	private static final UUID SWIM_SPEED_UUID = UUID.fromString("06f7628e-4794-49b2-8573-764b8246f56c");
	private static final int jumpRate = Reference.Values.TICKS_PER_SECOND;
	
	private double speed = 0.2D;
	
	public AbilitySwim()
	{
		this(0.2D);
	}
	public AbilitySwim(double speedIn)
	{
		super();
		this.speed = speedIn;
	}
	
	public int compare(Ability abilityIn)
	{
		AbilitySwim swim = (AbilitySwim)abilityIn;
		return swim.speed < speed ? 1 : swim.speed > speed ? -1 : 0;
	}
	
	public Type getType() { return Type.UTILITY; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.putDouble("Speed", this.speed);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		this.speed = compound.contains("Speed", 6) ? compound.getDouble("Speed") : 0.2D;
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::applySwim);
	}
	
	public void applySwim(LivingTickEvent event)
	{
		LivingEntity entity = event.getEntity();
		AttributeInstance attribute = entity.getAttribute(ForgeMod.SWIM_SPEED.get());
		if(attribute == null)
			return;
		
		if(AbilityRegistry.hasAbilityOfMapName(entity, getMapName()))
		{
			AbilitySwim swim = (AbilitySwim)AbilityRegistry.getAbilityByMapName(entity, getMapName());
			double amount = swim.speed;
			
			AttributeModifier modifier = attribute.getModifier(SWIM_SPEED_UUID);
			if(modifier != null && modifier.getAmount() != amount)
			{
				attribute.removeModifier(SWIM_SPEED_UUID);
				modifier = null;
			}
			
			if(modifier == null)
			{
				modifier = new AttributeModifier(SWIM_SPEED_UUID, "swim_speed", amount, Operation.MULTIPLY_TOTAL);
				attribute.addPermanentModifier(modifier);
			}
		}
		else if(attribute.getModifier(SWIM_SPEED_UUID) != null)
			attribute.removeModifier(SWIM_SPEED_UUID);
	}
	
	public int getRate(){ return jumpRate; }
	
	public boolean isValid(LivingEntity entity, Level world)
	{
		return isEntitySwimming(entity);
	}
	
	public JumpType jumpType(){ return JumpType.WATER; }
	
	public static boolean isEntitySwimming(LivingEntity entity)
	{
		return entity.isAlive() && entity.getPose() == Pose.SWIMMING && entity.isSwimming();
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilitySwim(compound.contains("Speed", 6) ? compound.getDouble("Speed") : 0.2D);
		}
	}
}
