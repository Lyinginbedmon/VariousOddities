package com.lying.variousoddities.api.world.settlement;

import net.minecraft.util.IStringSerializable;

public enum EnumRoomFunction implements IStringSerializable
{
	BARN,
	BARRACKS,
	BEDROOM,
	FARM,
	GUARDROOM,
	HOSPITAL,
	JAIL,
	KITCHEN,
	LABORATORY,
	LIBRARY,
	MINE,
	NEST,
	NONE,
	SCHOOL,
	SHRINE,
	STABLE,
	STORAGE,
	STORE,
	THRONE,
	TOMB,
	WORKSHOP;
	
	public String getString()
	{
		return name().toLowerCase();
	}
	
	public static EnumRoomFunction fromString(String str)
	{
    	for(EnumRoomFunction val : values())
    		if(val.name().equalsIgnoreCase(str))
    			return val;
    	return null;
	}
}
