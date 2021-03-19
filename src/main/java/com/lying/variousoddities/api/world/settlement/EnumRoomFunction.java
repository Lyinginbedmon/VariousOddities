package com.lying.variousoddities.api.world.settlement;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;

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
	
	public ITextComponent getName()
	{
		return new TranslationTextComponent("enum."+Reference.ModInfo.MOD_ID+".room_function."+this.name().toLowerCase()).modifyStyle((style) -> { return style.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new TranslationTextComponent("enum."+Reference.ModInfo.MOD_ID+".room_function."+this.name().toLowerCase()+".definition"))); });
	}
	
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
