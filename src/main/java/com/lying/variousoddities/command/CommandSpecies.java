package com.lying.variousoddities.command;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSpeciesOpenScreen;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.SpeciesRegistry;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.Types;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class CommandSpecies extends CommandBase
{
 	public static final SuggestionProvider<CommandSource> SPECIES_SUGGESTIONS = SuggestionProviders.register(new ResourceLocation("species_names"), (context, builder) -> {
 		return ISuggestionProvider.suggestIterable(VORegistries.SPECIES.keySet(), builder);
 		});
 	
 	
    private static final SimpleCommandExceptionType INVALID_ENTITY_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("argument.entity.invalid"));
	private static final DynamicCommandExceptionType SPECIES_MISSING_EXCEPTION = new DynamicCommandExceptionType((p_208922_0_) -> {
		return new TranslationTextComponent("command.varodd.abilities.species.missing", p_208922_0_);
	});
	private static final DynamicCommandExceptionType SPECIES_INVALID_EXCEPTION = new DynamicCommandExceptionType((p_208922_0_) -> {
		return new TranslationTextComponent("command.varodd.abilities.species.invalid", p_208922_0_);
	});
	
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".species.";
	private static final String ENTITY = "entity";
	private static final String PLAYER = "player";
	private static final String NAME = "species";
	
	private static IFormattableTextComponent CLICK_INFO = new TranslationTextComponent(translationSlug+"list_click").modifyStyle((style2) -> { return style2.setFormatting(TextFormatting.AQUA); });
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> literal = newLiteral("species")
				.then(newLiteral("list")
					.executes((source) -> { return listSpecies(source.getSource()); }))
				.then(newLiteral("info")
					.then(newArgument(NAME, ResourceLocationArgument.resourceLocation()).suggests(SPECIES_SUGGESTIONS)
						.executes((source) -> { return detailSpecies(ResourceLocationArgument.getResourceLocation(source, NAME), source.getSource()); })))
				.then(newLiteral("get")
					.then(newArgument(ENTITY, EntityArgument.entity())
						.executes((source) -> { return getSpecies(EntityArgument.getEntity(source, ENTITY), source.getSource()); })))
				.then(newLiteral("set")
					.then(newArgument(ENTITY, EntityArgument.entity()).then(newLiteral("to").then(newArgument(NAME, ResourceLocationArgument.resourceLocation()).suggests(SPECIES_SUGGESTIONS)
						.executes((source) -> { return setSpecies(EntityArgument.getEntity(source, ENTITY), ResourceLocationArgument.getResourceLocation(source, NAME), source.getSource()); })))))
				.then(newLiteral("select")
					.executes((source) -> { return selectSpecies(source.getSource().asPlayer(), source.getSource()); })
					.then(newArgument(PLAYER, EntityArgument.player())
							.executes((source) -> { return selectSpecies(EntityArgument.getEntity(source, PLAYER), source.getSource()); })))
				.then(Templates.build());
		
		dispatcher.register(literal);
	}
	
	public static ITextComponent getSpeciesWithInfo(ResourceLocation species)
	{
		IFormattableTextComponent abilityEntry = new StringTextComponent(species.toString());
		
		abilityEntry.modifyStyle((style) -> { return style
				.setFormatting(TextFormatting.DARK_AQUA)
				.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, CLICK_INFO))
				.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/species info "+species.toString())); });
		
		return abilityEntry;
	}
	
	private static int listSpecies(CommandSource source)
	{
		Set<ResourceLocation> speciesNames = VORegistries.SPECIES.keySet();
		source.sendFeedback(new TranslationTextComponent(translationSlug+"list", speciesNames.size()), true);
		for(ResourceLocation name : speciesNames)
			source.sendFeedback(new StringTextComponent(" -").append(getSpeciesWithInfo(name)), false);
		
		return speciesNames.size();
	}
	
	private static int detailSpecies(ResourceLocation speciesName, CommandSource source) throws CommandSyntaxException
	{
		Species species = VORegistries.SPECIES.get(speciesName);
		if(species == null)
			throw SPECIES_INVALID_EXCEPTION.create(speciesName);
		
		source.sendFeedback(new TranslationTextComponent(translationSlug+"info_name", species.getDisplayName()), true);
		if(species.hasTypes())
			source.sendFeedback(new Types(species.getCreatureTypes()).toHeader(), false);
		if(!species.getFullAbilities().isEmpty())
		{
			source.sendFeedback(new TranslationTextComponent(translationSlug+"info_abilities"), false);
			for(Ability ability : species.getFullAbilities())
				source.sendFeedback(new StringTextComponent(" -").append(ability.getDisplayName()), false);
		}
		return 15;
	}
	
	private static int getSpecies(Entity entity, CommandSource source) throws CommandSyntaxException
	{
		if(entity instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)entity;
			LivingData data = LivingData.forEntity(living);
			if(data.getSpecies() != null)
			{
				source.sendFeedback(new TranslationTextComponent(translationSlug+"get", living.getDisplayName(), data.getSpecies().getRegistryName()), true);
				return 15;
			}
			else
				throw SPECIES_MISSING_EXCEPTION.create(living.getDisplayName());
		}
		else
			throw INVALID_ENTITY_EXCEPTION.create();
	}
	
	private static int setSpecies(Entity entity, ResourceLocation name, CommandSource source) throws CommandSyntaxException
	{
		if(entity instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)entity;
			LivingData data = LivingData.forEntity(living);
			
			Species species = SpeciesRegistry.getSpecies(name);
			if(species != null)
			{
				data.setSpecies(species);
				source.sendFeedback(new TranslationTextComponent(translationSlug+"set", living.getDisplayName(), name), true);
				return 15;
			}
			else
				throw SPECIES_INVALID_EXCEPTION.create(name);
		}
		else
			throw INVALID_ENTITY_EXCEPTION.create();
	}
	
	private static int selectSpecies(Entity entity, CommandSource source) throws CommandSyntaxException
	{
		if(entity instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity)entity;
			if(!player.world.isRemote)
				PacketHandler.sendTo((ServerPlayerEntity)player, new PacketSpeciesOpenScreen());
			return 15;
		}
		else
			throw INVALID_ENTITY_EXCEPTION.create();
	}
	
	private static class Templates
	{
	 	public static final SuggestionProvider<CommandSource> TEMPLATE_SUGGESTIONS = SuggestionProviders.register(new ResourceLocation("template_names"), (context, builder) -> {
	 		return ISuggestionProvider.suggestIterable(VORegistries.TEMPLATES.keySet(), builder);
	 		});
	 	
		private static final DynamicCommandExceptionType TEMPLATE_MISSING_EXCEPTION = new DynamicCommandExceptionType((p_208922_0_) -> {
			return new TranslationTextComponent("command.varodd.abilities.template.missing", p_208922_0_);
		});
		private static final DynamicCommandExceptionType TEMPLATE_INVALID_EXCEPTION = new DynamicCommandExceptionType((p_208922_0_) -> {
			return new TranslationTextComponent("command.varodd.abilities.template.invalid", p_208922_0_);
		});
		
		private static final String NAME = "template";
		
		private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".species.templates.";
		
		public static LiteralArgumentBuilder<CommandSource> build()
		{
			return newLiteral("templates")
					.then(newLiteral("list")
						.executes((source) -> { return listAll(source.getSource()); })
						.then(newArgument(ENTITY, EntityArgument.entity())
							.executes((source) -> { return listEntity(EntityArgument.getEntity(source, ENTITY), source.getSource()); })))
					.then(newLiteral("info").then(newArgument(NAME, ResourceLocationArgument.resourceLocation()).suggests(TEMPLATE_SUGGESTIONS)
						.executes((source) -> { return detailTemplate(ResourceLocationArgument.getResourceLocation(source, NAME), source.getSource()); })))
					.then(newLiteral("apply").then(newArgument(NAME, ResourceLocationArgument.resourceLocation()).suggests(TEMPLATE_SUGGESTIONS).then(newLiteral("to").then(newArgument(ENTITY, EntityArgument.entity())
						.executes((source) -> { return addTemplate(ResourceLocationArgument.getResourceLocation(source, NAME), EntityArgument.getEntity(source, ENTITY), source.getSource()); })))))
					.then(newLiteral("remove").then(newArgument(NAME, ResourceLocationArgument.resourceLocation()).suggests(TEMPLATE_SUGGESTIONS).then(newLiteral("from").then(newArgument(ENTITY, EntityArgument.entity())
							.executes((source) -> { return removeTemplate(ResourceLocationArgument.getResourceLocation(source, NAME), EntityArgument.getEntity(source, ENTITY), source.getSource()); })))))
					.then(newLiteral("clear").then(newArgument(ENTITY, EntityArgument.entity())
						.executes((source) -> { return clearTemplates(EntityArgument.getEntity(source, ENTITY), source.getSource()); })))
					.then(newLiteral("get").then(newArgument(ENTITY, EntityArgument.entity()).then(newLiteral("has").then(newArgument(NAME, ResourceLocationArgument.resourceLocation()).suggests(TEMPLATE_SUGGESTIONS)
							.executes((source) -> { return getTemplate(ResourceLocationArgument.getResourceLocation(source, NAME), EntityArgument.getEntity(source, ENTITY), source.getSource()); })))))
					.then(newLiteral("test").then(newArgument(ENTITY, EntityArgument.entity()).then(newArgument(NAME, ResourceLocationArgument.resourceLocation())
							.executes((source) -> { return testTemplate(EntityArgument.getEntity(source, ENTITY), ResourceLocationArgument.getResourceLocation(source, NAME)); }))));
		}
		
		public static ITextComponent getTemplateWithInfo(ResourceLocation template)
		{
			IFormattableTextComponent abilityEntry = new StringTextComponent(template.toString());
			
			abilityEntry.modifyStyle((style) -> { return style
					.setFormatting(TextFormatting.DARK_AQUA)
					.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, CLICK_INFO))
					.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/species templates info "+template.toString())); });
			
			return abilityEntry;
		}
		
		private static int listAll(CommandSource source)
		{
			Set<ResourceLocation> templateNames = VORegistries.TEMPLATES.keySet();
			source.sendFeedback(new TranslationTextComponent(translationSlug+"list", templateNames.size()), true);
			for(ResourceLocation name : templateNames)
				source.sendFeedback(new StringTextComponent(" -").append(getTemplateWithInfo(name)), false);
			
			return templateNames.size();
		}
		
		private static int listEntity(Entity entity, CommandSource source) throws CommandSyntaxException
		{
			if(entity instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entity;
				LivingData livingData = LivingData.forEntity(living);
				if(livingData.getTemplates().isEmpty())
					throw TEMPLATE_MISSING_EXCEPTION.create(entity.getDisplayName());
				else
				{
					Collection<Template> templates = livingData.getTemplates();
					source.sendFeedback(new TranslationTextComponent(translationSlug+"list", templates.size()), true);
					for(Template template : templates)
						source.sendFeedback(new StringTextComponent(" -").append(getTemplateWithInfo(template.getRegistryName())), false);
					
					return templates.size();
				}
			}
			else
				throw INVALID_ENTITY_EXCEPTION.create();
		}
		
		private static int testTemplate(Entity entity, ResourceLocation templateName) throws CommandSyntaxException
		{
			if(entity instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entity;
				
				Template template = VORegistries.TEMPLATES.get(templateName);
				if(template == null)
					throw TEMPLATE_INVALID_EXCEPTION.create(templateName);
				else if(template.isApplicableTo(living, EnumSet.copyOf(EnumCreatureType.getCreatureTypes(living)), AbilityRegistry.getCreatureAbilities(living)))
					return 15;
				else
					throw INVALID_ENTITY_EXCEPTION.create();
			}
			else
				throw INVALID_ENTITY_EXCEPTION.create();
		}
		
		private static int detailTemplate(ResourceLocation templateName, CommandSource source) throws CommandSyntaxException
		{
			Template template = VORegistries.TEMPLATES.get(templateName);
			if(template == null)
				throw TEMPLATE_INVALID_EXCEPTION.create(templateName);
			
			source.sendFeedback(new TranslationTextComponent(translationSlug+"info_name", template.getDisplayName()), true);
			source.sendFeedback(new TranslationTextComponent(translationSlug+"info_uuid", template.uuid().toString()), false);
			source.sendFeedback(new TranslationTextComponent(translationSlug+"info_preconditions", template.getPreconditions().size()), false);
			template.getPreconditions().forEach((precondition) -> { source.sendFeedback(new StringTextComponent(" -").append(precondition.translate()), false); });
			source.sendFeedback(new TranslationTextComponent(translationSlug+"info_operations", template.getOperations().size()), false);
			template.getOperations().forEach((operation) -> { source.sendFeedback(new StringTextComponent(" -").append(operation.translate()), false); });
			return 15;
		}
		
		private static int addTemplate(ResourceLocation templateName, Entity entityIn, CommandSource source) throws CommandSyntaxException
		{
			if(entityIn instanceof LivingEntity)
			{
				Template template = VORegistries.TEMPLATES.get(templateName);
				if(template == null)
					throw TEMPLATE_INVALID_EXCEPTION.create(templateName);
				else
				{
					LivingEntity living = (LivingEntity)entityIn;
					LivingData data = LivingData.forEntity(living);
					data.addTemplate(template);
					source.sendFeedback(new TranslationTextComponent(translationSlug+"add.success", templateName, entityIn.getDisplayName()), true);
					return 15;
				}
			}
			else
				throw INVALID_ENTITY_EXCEPTION.create();
		}
		
		private static int removeTemplate(ResourceLocation templateName, Entity entityIn, CommandSource source) throws CommandSyntaxException
		{
			if(entityIn instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entityIn;
				LivingData data = LivingData.forEntity(living);
				if(!data.hasTemplate(templateName))
					throw TEMPLATE_INVALID_EXCEPTION.create(templateName);
				else
				{
					data.removeTemplate(templateName);
					source.sendFeedback(new TranslationTextComponent(translationSlug+"remove.success", templateName, entityIn.getDisplayName()), true);
					return 15;
				}
			}
			else
				throw INVALID_ENTITY_EXCEPTION.create();
		}
		
		private static int clearTemplates(Entity entityIn, CommandSource source) throws CommandSyntaxException
		{
			if(entityIn instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entityIn;
				LivingData data = LivingData.forEntity(living);
				if(!data.hasTemplates())
					throw TEMPLATE_MISSING_EXCEPTION.create(entityIn.getDisplayName());
				else
				{
					data.clearTemplates();
					source.sendFeedback(new TranslationTextComponent(translationSlug+"clear.success", entityIn.getDisplayName()), true);
					return 15;
				}
			}
			else
				throw INVALID_ENTITY_EXCEPTION.create();
		}
		
		private static int getTemplate(ResourceLocation templateName, Entity entityIn, CommandSource source) throws CommandSyntaxException
		{
			if(entityIn instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entityIn;
				LivingData data = LivingData.forEntity(living);
				if(!data.hasTemplate(templateName))
					throw TEMPLATE_MISSING_EXCEPTION.create(entityIn.getDisplayName());
				else
				{
					source.sendFeedback(new TranslationTextComponent(translationSlug+"get.success", entityIn.getDisplayName(), templateName), true);
					return 15;
				}
			}
			else
				throw INVALID_ENTITY_EXCEPTION.create();
		}
	}
}
