package com.lying.variousoddities.species.templates;

import com.lying.variousoddities.init.VORegistries;

import net.minecraftforge.registries.RegistryObject;

public class TemplatePreconditions
{
	public static final RegistryObject<TemplatePrecondition.Builder> TYPE = VORegistries.PRECONDITIONS.register("type", () -> new TypePrecondition.Builder());
	public static final RegistryObject<TemplatePrecondition.Builder> ABILITY = VORegistries.PRECONDITIONS.register("ability", () -> new AbilityPrecondition.Builder());
	
	public static void init() { }
}
