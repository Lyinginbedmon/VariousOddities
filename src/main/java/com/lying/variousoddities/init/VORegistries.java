package com.lying.variousoddities.init;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.lying.variousoddities.condition.Condition;
import com.lying.variousoddities.condition.Conditions;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.templates.TemplateOperation;
import com.lying.variousoddities.species.templates.TemplateOperations;
import com.lying.variousoddities.species.templates.TemplatePrecondition;
import com.lying.variousoddities.species.templates.TemplatePreconditions;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public class VORegistries
{
	public static final DeferredRegister<Ability.Builder> ABILITIES = DeferredRegister.create(new ResourceLocation(Reference.ModInfo.MOD_ID, "abilities"), Reference.ModInfo.MOD_ID);
	public static final DeferredRegister<TemplateOperation.Builder> OPERATIONS = DeferredRegister.create(new ResourceLocation(Reference.ModInfo.MOD_ID, "template_operations"), Reference.ModInfo.MOD_ID);
	public static final DeferredRegister<TemplatePrecondition.Builder> PRECONDITIONS = DeferredRegister.create(new ResourceLocation(Reference.ModInfo.MOD_ID, "template_preconditions"), Reference.ModInfo.MOD_ID);
	public static final DeferredRegister<Condition> CONDITIONS = DeferredRegister.create(new ResourceLocation(Reference.ModInfo.MOD_ID, "conditions"), Reference.ModInfo.MOD_ID);
	
	public static final Supplier<IForgeRegistry<Ability.Builder>> ABILITIES_REGISTRY = VORegistries.ABILITIES.makeRegistry(() ->
		new RegistryBuilder<Ability.Builder>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, key, obj, old) -> {}
		).setDefaultKey(new ResourceLocation(Reference.ModInfo.MOD_ID, "null")));
	public static final Supplier<IForgeRegistry<TemplateOperation.Builder>> OPERATIONS_REGISTRY = VORegistries.OPERATIONS.makeRegistry(() ->
		new RegistryBuilder<TemplateOperation.Builder>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, key, obj, old) -> {}
		).setDefaultKey(new ResourceLocation(Reference.ModInfo.MOD_ID, "null")));
	public static final Supplier<IForgeRegistry<TemplatePrecondition.Builder>> PRECONDITIONS_REGISTRY = VORegistries.PRECONDITIONS.makeRegistry(() ->
		new RegistryBuilder<TemplatePrecondition.Builder>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, key, obj, old) -> {}
		).setDefaultKey(new ResourceLocation(Reference.ModInfo.MOD_ID, "null")));
	public static final Supplier<IForgeRegistry<Condition>> CONDITION_REGISTRY = VORegistries.CONDITIONS.makeRegistry(() ->
		new RegistryBuilder<Condition>().setMaxID(Integer.MAX_VALUE - 1).onAdd((owner, stage, id, key, obj, old) -> {}
		).setDefaultKey(new ResourceLocation(Reference.ModInfo.MOD_ID, "null")));
	
	public static final Map<ResourceLocation, Species> SPECIES = new HashMap<>();
	public static final Map<ResourceLocation, Template> TEMPLATES = new HashMap<>();
	
	public static void registerCustom(IEventBus modEventBus)
	{
        VORegistries.ABILITIES.register(modEventBus);
        VORegistries.PRECONDITIONS.register(modEventBus);
        VORegistries.CONDITIONS.register(modEventBus);
        VORegistries.PRECONDITIONS.register(modEventBus);
        
        AbilityRegistry.init();
        TemplatePreconditions.init();
        TemplateOperations.init();
        Conditions.init();
	}
}
