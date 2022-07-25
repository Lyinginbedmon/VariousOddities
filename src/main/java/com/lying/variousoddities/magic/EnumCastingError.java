package com.lying.variousoddities.magic;

import net.minecraft.network.chat.Component;

public enum EnumCastingError
{
	NO_TARGET,
	ANTI_MAGIC,
	CONCENTRATING,
	INGREDIENTS,
	NO_FOCUS,
	NO_SPEECH,
	NO_GESTURE,
	BAD_AREA,
	UNKNOWN,
	CASTABLE,
	CASTER_LEVEL;
	
	private final String translationBase = "enum.varodd:casting_error.";
	
	public String getTranslated(){ return Component.translatable(translationBase+getSimpleName()+".name").getString(); }
	private String getSimpleName(){ return this.name().toLowerCase(); }
}
