package com.lying.variousoddities.magic.trigger;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.magic.trigger.TriggerAudible.TriggerAudibleChat;
import com.lying.variousoddities.magic.trigger.TriggerAudible.TriggerAudibleSound;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;

public abstract class Trigger
{
	private static final Map<String, Class<? extends Trigger>> CLASS_MAP = new HashMap<>();
	
	protected static final List<? extends Trigger> NO_VARIABLES = Arrays.asList();
	
	private boolean inverted = false;
	
	public abstract String type();
	
	public boolean inverted(){ return this.inverted; }
	
	public Trigger setInverted(boolean inv){ this.inverted = inv; return this; }
	
	public int totalVariables()
	{
		int total = 1;
		for(Trigger variable : getVariables())
			total += variable.totalVariables();
		return total;
	}
	
	public String toString()
	{
		String name = getTranslated(inverted()).getString();
		for(Trigger trigger : getVariables())
			name += trigger.toString();
		return name;
	}
	
	public void print()
	{
		print(1);
	}
	
	public void print(int inset)
	{
		String step = "";
		for(int i=0; i<(inset * 2); i++)
			step += " ";
		
		VariousOddities.log.info(step + getTranslated(inverted()).getString());
		for(Trigger trigger : getVariables())
			trigger.print(inset + 1);
	}
	
	public abstract Component getTranslated(boolean inverted);
	
	/** Reads variable-specific data from the given NBT compound */
	public void readFromNBT(CompoundTag compound){ }
	
	/** Writes variable-specific data to the given NBT compound.<br>
	 * Should NOT be used for storing the trigger itself!
	 */
	public CompoundTag writeToNBT(CompoundTag compound){ return compound; }
	
	public abstract Collection<? extends Trigger> possibleVariables();
	
	/** 
	 * Adds the given variable to this trigger.<br>
	 * All variables must validate for a trigger to validate. 
	 */
	public Trigger addVariable(Trigger triggerIn){ return this; }
	
	public List<? extends Trigger> getVariables(){ return NO_VARIABLES; }
	
	/** Constructs a trigger from the given NBT compound, if it contains a Type string */
	public static Trigger createTriggerFromNBT(CompoundTag compound)
	{
		if(compound.contains("Type"))
			return readTriggerFromNBT(compound.getString("Type"), compound);
		return null;
	}
	
	/** Constructs a trigger of the given type from the given NBT compound */
	@SuppressWarnings("deprecation")
	public static Trigger readTriggerFromNBT(String nameIn, CompoundTag compound)
	{
		if(CLASS_MAP.containsKey(nameIn))
		{
			Trigger instance = null;
			try
			{
				instance = CLASS_MAP.get(nameIn).newInstance();
			}
			catch(Exception e)
			{
				VariousOddities.log.error("Couldn't construct trigger of type "+nameIn);
			}
			if(instance != null)
			{
				instance.setInverted(compound.contains("Inverted") ? compound.getBoolean("Inverted") : false);
				instance.readFromNBT(compound.contains("Data") ? compound.getCompound("Data") : new CompoundTag());
				
				if(compound.contains("Variables") && !instance.possibleVariables().isEmpty())
				{
					ListTag variableData = compound.getList("Variables", 10);
					for(int i=0; i<variableData.size(); i++)
					{
						CompoundTag data = variableData.getCompound(i);
						Trigger variable = createTriggerFromNBT(data);
						if(variable != null)
							instance.addVariable(variable);
					}
				}
			}
			return instance;
		}
		return null;
	}
	
	/** Stores the given trigger in the given NBT compound in a format that can be read later */
	public static CompoundTag writeTriggerToNBT(Trigger triggerIn, CompoundTag compound)
	{
		compound.putString("Type", triggerIn.type());
		compound.putBoolean("Inverted", triggerIn.inverted());
		compound.put("Data", triggerIn.writeToNBT(new CompoundTag()));
		
		if(!triggerIn.possibleVariables().isEmpty() && !triggerIn.getVariables().isEmpty())
		{
			ListTag variableData = new ListTag();
			for(Trigger variable : triggerIn.getVariables())
				variableData.add(writeTriggerToNBT(variable, new CompoundTag()));
			compound.put("Variables", variableData);
		}
		
		return compound;
	}
	
	@SuppressWarnings("deprecation")
	private static void registerTrigger(Class<? extends Trigger> triggerClass)
	{
		Trigger instance = null;
		try
		{
			instance = triggerClass.newInstance();
		}
		catch(Exception e){ }
		if(instance != null)
			registerTrigger(instance);
	}
	
	private static void registerTrigger(Trigger instance)
	{
		if(CLASS_MAP.containsKey(instance.type()))
			return;
		
		CLASS_MAP.put(instance.type(), instance.getClass());
		
		if(!instance.possibleVariables().isEmpty())
			for(Trigger variable : instance.possibleVariables())
				registerTrigger(variable);
	}
	
	static
	{
		registerTrigger(TriggerEntity.class);
		registerTrigger(TriggerBlock.class);
		registerTrigger(TriggerItem.class);
		registerTrigger(TriggerTime.class);
		registerTrigger(TriggerAudibleChat.class);
		registerTrigger(TriggerAudibleSound.class);
	}
}
