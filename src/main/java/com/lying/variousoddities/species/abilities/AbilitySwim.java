package com.lying.variousoddities.species.abilities;

import java.util.UUID;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilitySwim extends Ability implements IBonusJumpAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "swim");
	private static final UUID SWIM_SPEED_UUID = UUID.fromString("06f7628e-4794-49b2-8573-764b8246f56c");
	private static final int jumpRate = Reference.Values.TICKS_PER_SECOND;
	
	private double speed = 0.2D;
	
	public AbilitySwim()
	{
		this(0.2D);
	}
	public AbilitySwim(double speedIn)
	{
		super(REGISTRY_NAME);
		this.speed = speedIn;
	}
	
	public int compare(Ability abilityIn)
	{
		AbilitySwim swim = (AbilitySwim)abilityIn;
		return swim.speed < speed ? 1 : swim.speed > speed ? -1 : 0;
	}
	
	public Type getType() { return Type.UTILITY; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.putDouble("Speed", this.speed);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		this.speed = compound.contains("Speed", 6) ? compound.getDouble("Speed") : 0.2D;
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::applySwim);
	}
	
	public void applySwim(LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		ModifiableAttributeInstance attribute = entity.getAttribute(ForgeMod.SWIM_SPEED.get());
		if(attribute == null)
			return;
		
		if(AbilityRegistry.hasAbility(entity, getMapName()))
		{
			AbilitySwim swim = (AbilitySwim)AbilityRegistry.getAbilityByName(entity, getMapName());
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
				attribute.applyPersistentModifier(modifier);
			}
			
		}
		else if(attribute.getModifier(SWIM_SPEED_UUID) != null)
			attribute.removeModifier(SWIM_SPEED_UUID);
	}
	
	public int getRate(){ return jumpRate; }
	
	public boolean isValid(LivingEntity entity, World world)
	{
		return isEntitySwimming(entity);
	}
	
	public JumpType jumpType(){ return JumpType.WATER; }
	
	public static boolean isEntitySwimming(LivingEntity entity)
	{
		return entity.isAlive() && entity.getPose() == Pose.SWIMMING && entity.isActualySwimming();
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilitySwim(compound.contains("Speed", 6) ? compound.getDouble("Speed") : 0.2D);
		}
	}
}
