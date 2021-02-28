package com.lying.variousoddities.magic;

import net.minecraft.util.text.TranslationTextComponent;

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
	
	public String getTranslated(){ return new TranslationTextComponent(translationBase+getSimpleName()+".name").getUnformattedComponentText(); }
	private String getSimpleName(){ return this.name().toLowerCase(); }
}
