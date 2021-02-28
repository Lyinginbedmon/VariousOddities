package com.lying.variousoddities.api.world.settlement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
	
	public static EnumRoomFunction fromName(String nameIn)
	{
		for(EnumRoomFunction function : values())
			if(function.name().equalsIgnoreCase(nameIn))
				return function;
		return null;
	}
	
	public String getString()
	{
		return name().toLowerCase();
	}
	
	public static Collection<String> names()
	{
		List<String> names = new ArrayList<>();
		for(EnumRoomFunction function : values())
			names.add(function.getString());
		return names;
	}
}
