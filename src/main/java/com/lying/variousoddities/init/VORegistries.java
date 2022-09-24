package com.lying.variousoddities.init;

import java.util.HashMap;
import java.util.Map;

import com.lying.variousoddities.condition.Condition;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.templates.TemplateOperation;
import com.lying.variousoddities.species.templates.TemplatePrecondition;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public class VORegistries
{
	private static final ResourceLocation ABILITY_REG = new ResourceLocation(Reference.ModInfo.MOD_ID, "abilities");
	private static final ResourceKey<Registry<Ability.Builder>> ABILITY_KEY = ResourceKey.createRegistryKey(ABILITY_REG);
	public static final DeferredRegister<Ability.Builder> ABILITIES = DeferredRegister.create(ABILITY_KEY, Reference.ModInfo.MOD_ID);
	
	private static final ResourceLocation OPERATIONS_REG = new ResourceLocation(Reference.ModInfo.MOD_ID, "template_operations");
	private static final ResourceKey<Registry<TemplateOperation.Builder>> OPERATIONS_KEY = ResourceKey.createRegistryKey(OPERATIONS_REG);
	public static final DeferredRegister<TemplateOperation.Builder> OPERATIONS = DeferredRegister.create(OPERATIONS_KEY, Reference.ModInfo.MOD_ID);
	
	private static final ResourceLocation PRECONDITIONS_REG = new ResourceLocation(Reference.ModInfo.MOD_ID, "template_preconditions");
	private static final ResourceKey<Registry<TemplatePrecondition.Builder>> PRECONDITIONS_KEY = ResourceKey.createRegistryKey(PRECONDITIONS_REG);
	public static final DeferredRegister<TemplatePrecondition.Builder> PRECONDITIONS = DeferredRegister.create(PRECONDITIONS_KEY, Reference.ModInfo.MOD_ID);
	
	private static final ResourceLocation CONDITIONS_REG = new ResourceLocation(Reference.ModInfo.MOD_ID, "conditions");
	private static final ResourceKey<Registry<Condition>> CONDITIONS_KEY = ResourceKey.createRegistryKey(CONDITIONS_REG);
	public static final DeferredRegister<Condition> CONDITIONS = DeferredRegister.create(CONDITIONS_KEY, Reference.ModInfo.MOD_ID);
	
	public static final Map<ResourceLocation, Species> SPECIES = new HashMap<>();
	public static final Map<ResourceLocation, Template> TEMPLATES = new HashMap<>();
	
	public static void init(IEventBus bus)
	{
		
	}
}
