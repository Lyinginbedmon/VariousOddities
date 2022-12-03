package com.lying.variousoddities.init;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
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
	public static final DeferredRegister<Ability.Builder> ABILITIES						= DeferredRegister.create(Ability.Builder.REGISTRY_KEY, Reference.ModInfo.MOD_ID);
	public static final DeferredRegister<TemplateOperation.Builder> OPERATIONS			= DeferredRegister.create(TemplateOperation.Builder.REGISTRY_KEY, Reference.ModInfo.MOD_ID);
	public static final DeferredRegister<TemplatePrecondition.Builder> PRECONDITIONS	= DeferredRegister.create(TemplatePrecondition.Builder.REGISTRY_KEY, Reference.ModInfo.MOD_ID);
	public static final DeferredRegister<Condition> CONDITIONS							= DeferredRegister.create(Condition.REGISTRY_KEY, Reference.ModInfo.MOD_ID);
	
	public static final Supplier<IForgeRegistry<Ability.Builder>> ABILITIES_REGISTRY = ABILITIES.makeRegistry(RegistryBuilder::new);
	public static final Supplier<IForgeRegistry<TemplateOperation.Builder>> OPERATIONS_REGISTRY = OPERATIONS.makeRegistry(RegistryBuilder::new);
	public static final Supplier<IForgeRegistry<TemplatePrecondition.Builder>> PRECONDITIONS_REGISTRY = VORegistries.PRECONDITIONS.makeRegistry(RegistryBuilder::new);
	public static final Supplier<IForgeRegistry<Condition>> CONDITIONS_REGISTRY = VORegistries.CONDITIONS.makeRegistry(RegistryBuilder::new);
	
	public static final Map<ResourceLocation, Species> SPECIES = new HashMap<>();
	public static final Map<ResourceLocation, Template> TEMPLATES = new HashMap<>();
	
	public static void registerCustom(IEventBus modEventBus)
	{
		VariousOddities.log.info("Registered custom registries");
        AbilityRegistry.init();
        TemplatePreconditions.init();
        TemplateOperations.init();
        Conditions.init();
        
        VORegistries.ABILITIES.register(modEventBus);
        VORegistries.PRECONDITIONS.register(modEventBus);
        VORegistries.CONDITIONS.register(modEventBus);
        VORegistries.PRECONDITIONS.register(modEventBus);
	}
}
