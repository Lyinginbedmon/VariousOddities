package com.lying.variousoddities.api.world.settlement;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;

public enum EnumRoomFunction implements StringRepresentable
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
	
	public MutableComponent getName()
	{
		return Component.translatable("enum."+Reference.ModInfo.MOD_ID+".room_function."+this.getSerializedName()).withStyle((style) -> { return style.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Component.translatable("enum."+Reference.ModInfo.MOD_ID+".room_function."+this.getSerializedName()+".definition"))); });
	}
	
	public String getSerializedName()
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
