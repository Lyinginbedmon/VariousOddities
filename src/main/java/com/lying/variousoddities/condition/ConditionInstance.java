package com.lying.variousoddities.condition;

import java.util.UUID;

import javax.annotation.Nonnull;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class ConditionInstance
{
	private Condition condition;
	private UUID originID = null;
	private int ticksRemaining;
	private CompoundNBT storage = new CompoundNBT();
	
	public ConditionInstance(Condition conditionIn)
	{
		this.condition = conditionIn;
		this.ticksRemaining = Reference.Values.TICKS_PER_SECOND * 30;
	}
	
	public ConditionInstance(Condition conditionIn, int durationIn)
	{
		this(conditionIn);
		this.ticksRemaining = durationIn;
	}
	
	public ConditionInstance(Condition conditionIn, int durationIn, UUID originIn)
	{
		this(conditionIn, durationIn);
		this.originID = originIn;
	}
	
	public Condition condition() { return this.condition; }
	
	public UUID originUUID() { return this.originID; }
	
	public CompoundNBT storage() { return this.storage; }
	
	public void setStorage(@Nonnull CompoundNBT compound) { this.storage = compound; }
	
	public final CompoundNBT write(CompoundNBT compound)
	{
		compound.putString("Condition", condition.getRegistryName().toString());
		compound.putInt("TicksRemaining", ticksRemaining);
		if(originID != null)
			compound.putUniqueId("UUID", originID);
		if(!storage.isEmpty())
			compound.put("Storage", storage);
		return compound;
	}
	
	public static final ConditionInstance read(CompoundNBT compound)
	{
		if(compound.contains("Condition", 8))
		{
			ResourceLocation registryName = new ResourceLocation(compound.getString("Condition"));
			Condition condition = Conditions.getByRegistryName(registryName);
			if(condition == null)
				return null;
			
			ConditionInstance instance;
			if(compound.contains("TicksRemaining"))
				instance = new ConditionInstance(condition, compound.getInt("TicksRemaining"));
			else
				instance = new ConditionInstance(condition);
			
			if(compound.hasUniqueId("UUID"))
				instance.originID = compound.getUniqueId("UUID");
			
			if(compound.contains("Storage", 10))
				instance.storage = compound.getCompound("Storage");
			
			return instance;
		}
		return null;
	}
	
	public void start(LivingEntity entity)
	{
		this.condition.start(entity, this.originID, this.storage);
	}
	
	public void tick(LivingEntity entity)
	{
		this.condition.tick(entity, this.originID, this.storage, --this.ticksRemaining);
	}
	
	public void reset(LivingEntity entity)
	{
		this.condition.reset(entity, this.originID, this.storage);
	}
	
	public void end(LivingEntity entity)
	{
		this.condition.end(entity, this.originID, this.storage);
	}
	
	public boolean isExpired() { return this.ticksRemaining <= 0; }
}
