package com.lying.variousoddities.species.templates;

import com.lying.variousoddities.init.VORegistries;

import net.minecraftforge.registries.RegistryObject;

public class TemplateOperations
{
	public static final RegistryObject<TemplateOperation.Builder> TYPE = VORegistries.OPERATIONS.register("type", () -> new TypeOperation.Builder());
	public static final RegistryObject<TemplateOperation.Builder> ABILITY = VORegistries.OPERATIONS.register("ability", () -> new AbilityOperation.Builder());
	public static final RegistryObject<TemplateOperation.Builder> COMPOUND = VORegistries.OPERATIONS.register("compound", () -> new CompoundOperation.Builder());
	public static final RegistryObject<TemplateOperation.Builder> REPLACE_SUPERTYPES = VORegistries.OPERATIONS.register("replace_supertypes", () -> new OperationReplaceSupertypes.Builder());
	
	public static void init() { }
}
