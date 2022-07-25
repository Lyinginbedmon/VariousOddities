package com.lying.variousoddities.magic.trigger;

import java.util.Collection;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class TriggerTime extends Trigger
{
	private long timeMin = 0;
	private long timeMax = Reference.Values.TICKS_PER_SECOND;
	
	public String type(){ return "time"; }
	
	public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"time" + (inverted ? "_inverted" : ""), timeMin, timeMax); }
	
	public boolean applyToTime(long timeIn)
	{
		long timeA = Math.max(timeMax, timeMin);
		long timeB = Math.min(timeMax, timeMin);
		timeMax = timeA;
		timeMin = timeB;
		
		return timeIn >= timeMin && timeIn <= timeMax;
	}
	
	public Collection<? extends Trigger> possibleVariables(){ return NO_VARIABLES; }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.putLong("TimeMin", timeMin);
		compound.putLong("TimeMax", timeMax);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		timeMin = compound.getLong("TimeMin") % Reference.Values.TICKS_PER_DAY;
		timeMax = compound.getLong("TimeMax") % Reference.Values.TICKS_PER_DAY;
	}
}
