package com.lying.variousoddities.condition;

import java.util.UUID;

import javax.annotation.Nonnull;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class ConditionInstance
{
	private Condition condition;
	private UUID originID = null;
	private int ticksRemaining;
	private CompoundTag storage = new CompoundTag();
	
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
	
	public CompoundTag storage() { return this.storage; }
	
	public void setStorage(@Nonnull CompoundTag compound) { this.storage = compound; }
	
	public final CompoundTag write(CompoundTag compound)
	{
		compound.putString("Condition", condition().getKey().toString());
		compound.putInt("TicksRemaining", ticksRemaining);
		if(originID != null)
			compound.putUUID("UUID", originID);
		if(!storage.isEmpty())
			compound.put("Storage", storage);
		return compound;
	}
	
	public static final ConditionInstance read(CompoundTag compound)
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
			
			if(compound.hasUUID("UUID"))
				instance.originID = compound.getUUID("UUID");
			
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
