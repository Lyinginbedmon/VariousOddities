package com.lying.variousoddities.command;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.config.ConfigVO;
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

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class CommandSpecies extends CommandBase
{
 	public static final SuggestionProvider<CommandSourceStack> SPECIES_SUGGESTIONS = SuggestionProviders.register(new ResourceLocation("species_names"), (context, builder) -> {
 		return SharedSuggestionProvider.suggestResource(VORegistries.SPECIES.keySet(), builder);
 		});
 	
 	
    private static final SimpleCommandExceptionType INVALID_ENTITY_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.entity.invalid"));
	private static final DynamicCommandExceptionType SPECIES_MISSING_EXCEPTION = new DynamicCommandExceptionType((p_208922_0_) -> {
		return Component.translatable("command.varodd.abilities.species.missing", p_208922_0_);
	});
	private static final DynamicCommandExceptionType SPECIES_INVALID_EXCEPTION = new DynamicCommandExceptionType((p_208922_0_) -> {
		return Component.translatable("command.varodd.abilities.species.invalid", p_208922_0_);
	});
	
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".species.";
	private static final String ENTITY = "entity";
	private static final String PLAYER = "player";
	private static final String NAME = "species";
	
	private static MutableComponent CLICK_INFO = Component.translatable(translationSlug+"list_click").withStyle((style2) -> { return style2.applyFormat(ChatFormatting.AQUA); });
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("species")
				.then(Commands.literal("list")
					.executes((source) -> { return listSpecies(source.getSource()); }))
				.then(Commands.literal("info")
					.then(Commands.argument(NAME, ResourceLocationArgument.id()).suggests(SPECIES_SUGGESTIONS)
						.executes((source) -> { return detailSpecies(ResourceLocationArgument.getId(source, NAME), source.getSource()); })))
				.then(Commands.literal("get")
					.then(Commands.argument(ENTITY, EntityArgument.entity())
						.executes((source) -> { return getSpecies(EntityArgument.getEntity(source, ENTITY), source.getSource()); })))
				.then(Commands.literal("set")
					.then(Commands.argument(ENTITY, EntityArgument.entity()).then(Commands.literal("to").then(Commands.argument(NAME, ResourceLocationArgument.id()).suggests(SPECIES_SUGGESTIONS)
						.executes((source) -> { return setSpecies(EntityArgument.getEntity(source, ENTITY), ResourceLocationArgument.getId(source, NAME), source.getSource()); })))))
				.then(Commands.literal("select")
					.executes((source) -> { return selectSpecies(source.getSource().getPlayer(), source.getSource()); })
					.then(Commands.argument(PLAYER, EntityArgument.player())
						.executes((source) -> { return selectSpecies(EntityArgument.getEntity(source, PLAYER), source.getSource()); })))
				.then(Templates.build());
		
		dispatcher.register(literal);
	}
	
	public static Component getSpeciesWithInfo(ResourceLocation species)
	{
		MutableComponent abilityEntry = Component.literal(species.toString());
		
		abilityEntry.withStyle((style) -> { return style
				.applyFormat(ChatFormatting.DARK_AQUA)
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, CLICK_INFO))
				.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/species info "+species.toString())); });
		
		return abilityEntry;
	}
	
	private static int listSpecies(CommandSourceStack source)
	{
		Set<ResourceLocation> speciesNames = VORegistries.SPECIES.keySet();
		source.sendSuccess(Component.translatable(translationSlug+"list", speciesNames.size()), true);
		for(ResourceLocation name : speciesNames)
			source.sendSuccess(Component.literal(" -").append(getSpeciesWithInfo(name)), false);
		
		return speciesNames.size();
	}
	
	private static int detailSpecies(ResourceLocation speciesName, CommandSourceStack source) throws CommandSyntaxException
	{
		Species species = VORegistries.SPECIES.get(speciesName);
		if(species == null)
			throw SPECIES_INVALID_EXCEPTION.create(speciesName);
		
		source.sendSuccess(Component.translatable(translationSlug+"info_name", species.getDisplayName()), true);
		if(species.hasTypes())
			source.sendSuccess(new Types(species.getCreatureTypes()).toHeader(), false);
		if(!species.getFullAbilities().isEmpty())
		{
			source.sendSuccess(Component.translatable(translationSlug+"info_abilities"), false);
			for(Ability ability : species.getFullAbilities())
				source.sendSuccess(Component.literal(" -").append(ability.getDisplayName()), false);
		}
		return 15;
	}
	
	private static int getSpecies(Entity entity, CommandSourceStack source) throws CommandSyntaxException
	{
		if(entity instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)entity;
			LivingData data = LivingData.getCapability(living);
			if(data.getSpecies() != null)
			{
				source.sendSuccess(Component.translatable(translationSlug+"get", living.getDisplayName(), data.getSpecies().getRegistryName()), true);
				return 15;
			}
			else
				throw SPECIES_MISSING_EXCEPTION.create(living.getDisplayName());
		}
		else
			throw INVALID_ENTITY_EXCEPTION.create();
	}
	
	private static int setSpecies(Entity entity, ResourceLocation name, CommandSourceStack source) throws CommandSyntaxException
	{
		if(entity instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)entity;
			LivingData data = LivingData.getCapability(living);
			
			Species species = SpeciesRegistry.getSpecies(name);
			if(species != null)
			{
				data.setSpecies(species);
				source.sendSuccess(Component.translatable(translationSlug+"set", living.getDisplayName(), name), true);
				return 15;
			}
			else
				throw SPECIES_INVALID_EXCEPTION.create(name);
		}
		else
			throw INVALID_ENTITY_EXCEPTION.create();
	}
	
	private static int selectSpecies(Entity entity, CommandSourceStack source) throws CommandSyntaxException
	{
		if(entity instanceof Player)
		{
			Player player = (Player)entity;
			if(!player.level.isClientSide)
				PacketHandler.sendTo((ServerPlayer)player, new PacketSpeciesOpenScreen(ConfigVO.MOBS.powerLevel.get(), false));
			return 15;
		}
		else
			throw INVALID_ENTITY_EXCEPTION.create();
	}
	
	private static class Templates
	{
	 	public static final SuggestionProvider<CommandSourceStack> TEMPLATE_SUGGESTIONS = SuggestionProviders.register(new ResourceLocation("template_names"), (context, builder) -> {
	 		return SharedSuggestionProvider.suggestResource(VORegistries.TEMPLATES.keySet(), builder);
	 		});
	 	
		private static final DynamicCommandExceptionType TEMPLATE_MISSING_EXCEPTION = new DynamicCommandExceptionType((p_208922_0_) -> {
			return Component.translatable("command.varodd.abilities.template.missing", p_208922_0_);
		});
		private static final DynamicCommandExceptionType TEMPLATE_INVALID_EXCEPTION = new DynamicCommandExceptionType((p_208922_0_) -> {
			return Component.translatable("command.varodd.abilities.template.invalid", p_208922_0_);
		});
		
		private static final String NAME = "template";
		
		private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".species.templates.";
		
		public static LiteralArgumentBuilder<CommandSourceStack> build()
		{
			return Commands.literal("templates")
					.then(Commands.literal("list")
						.executes((source) -> { return listAll(source.getSource()); })
						.then(Commands.argument(ENTITY, EntityArgument.entity())
							.executes((source) -> { return listEntity(EntityArgument.getEntity(source, ENTITY), source.getSource()); })))
					.then(Commands.literal("info").then(Commands.argument(NAME, ResourceLocationArgument.id()).suggests(TEMPLATE_SUGGESTIONS)
						.executes((source) -> { return detailTemplate(ResourceLocationArgument.getId(source, NAME), source.getSource()); })))
					.then(Commands.literal("apply").then(Commands.argument(NAME, ResourceLocationArgument.id()).suggests(TEMPLATE_SUGGESTIONS).then(Commands.literal("to").then(Commands.argument(ENTITY, EntityArgument.entity())
						.executes((source) -> { return addTemplate(ResourceLocationArgument.getId(source, NAME), EntityArgument.getEntity(source, ENTITY), source.getSource()); })))))
					.then(Commands.literal("remove").then(Commands.argument(NAME, ResourceLocationArgument.id()).suggests(TEMPLATE_SUGGESTIONS).then(Commands.literal("from").then(Commands.argument(ENTITY, EntityArgument.entity())
							.executes((source) -> { return removeTemplate(ResourceLocationArgument.getId(source, NAME), EntityArgument.getEntity(source, ENTITY), source.getSource()); })))))
					.then(Commands.literal("clear").then(Commands.argument(ENTITY, EntityArgument.entity())
						.executes((source) -> { return clearTemplates(EntityArgument.getEntity(source, ENTITY), source.getSource()); })))
					.then(Commands.literal("get").then(Commands.argument(ENTITY, EntityArgument.entity()).then(Commands.literal("has").then(Commands.argument(NAME, ResourceLocationArgument.id()).suggests(TEMPLATE_SUGGESTIONS)
							.executes((source) -> { return getTemplate(ResourceLocationArgument.getId(source, NAME), EntityArgument.getEntity(source, ENTITY), source.getSource()); })))))
					.then(Commands.literal("test").then(Commands.argument(ENTITY, EntityArgument.entity()).then(Commands.argument(NAME, ResourceLocationArgument.id())
							.executes((source) -> { return testTemplate(EntityArgument.getEntity(source, ENTITY), ResourceLocationArgument.getId(source, NAME)); }))));
		}
		
		public static Component getTemplateWithInfo(ResourceLocation template)
		{
			MutableComponent abilityEntry = Component.literal(template.toString());
			
			abilityEntry.withStyle((style) -> { return style
					.applyFormat(ChatFormatting.DARK_AQUA)
					.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, CLICK_INFO))
					.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/species templates info "+template.toString())); });
			
			return abilityEntry;
		}
		
		private static int listAll(CommandSourceStack source)
		{
			Set<ResourceLocation> templateNames = VORegistries.TEMPLATES.keySet();
			source.sendSuccess(Component.translatable(translationSlug+"list", templateNames.size()), true);
			for(ResourceLocation name : templateNames)
				source.sendSuccess(Component.literal(" -").append(getTemplateWithInfo(name)), false);
			
			return templateNames.size();
		}
		
		private static int listEntity(Entity entity, CommandSourceStack source) throws CommandSyntaxException
		{
			if(entity instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entity;
				LivingData livingData = LivingData.getCapability(living);
				if(livingData.getTemplates().isEmpty())
					throw TEMPLATE_MISSING_EXCEPTION.create(entity.getDisplayName());
				else
				{
					Collection<Template> templates = livingData.getTemplates();
					source.sendSuccess(Component.translatable(translationSlug+"list", templates.size()), true);
					for(Template template : templates)
						source.sendSuccess(Component.literal(" -").append(getTemplateWithInfo(template.getRegistryName())), false);
					
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
		
		private static int detailTemplate(ResourceLocation templateName, CommandSourceStack source) throws CommandSyntaxException
		{
			Template template = VORegistries.TEMPLATES.get(templateName);
			if(template == null)
				throw TEMPLATE_INVALID_EXCEPTION.create(templateName);
			
			source.sendSuccess(Component.translatable(translationSlug+"info_name", template.getDisplayName()), true);
			source.sendSuccess(Component.translatable(translationSlug+"info_uuid", template.uuid().toString()), false);
			source.sendSuccess(Component.translatable(translationSlug+"info_preconditions", template.getPreconditions().size()), false);
			template.getPreconditions().forEach((precondition) -> { source.sendSuccess(Component.literal(" -").append(precondition.translate()), false); });
			source.sendSuccess(Component.translatable(translationSlug+"info_operations", template.getOperations().size()), false);
			template.getOperations().forEach((operation) -> { source.sendSuccess(Component.literal(" -").append(operation.translate()), false); });
			return 15;
		}
		
		private static int addTemplate(ResourceLocation templateName, Entity entityIn, CommandSourceStack source) throws CommandSyntaxException
		{
			if(entityIn instanceof LivingEntity)
			{
				Template template = VORegistries.TEMPLATES.get(templateName);
				if(template == null)
					throw TEMPLATE_INVALID_EXCEPTION.create(templateName);
				else
				{
					LivingEntity living = (LivingEntity)entityIn;
					LivingData data = LivingData.getCapability(living);
					if(!data.addTemplateInitial(template))
						throw INVALID_ENTITY_EXCEPTION.create();
					
					source.sendSuccess(Component.translatable(translationSlug+"add.success", templateName, entityIn.getDisplayName()), true);
					return 15;
				}
			}
			else
				throw INVALID_ENTITY_EXCEPTION.create();
		}
		
		private static int removeTemplate(ResourceLocation templateName, Entity entityIn, CommandSourceStack source) throws CommandSyntaxException
		{
			if(entityIn instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entityIn;
				LivingData data = LivingData.getCapability(living);
				if(!data.hasTemplate(templateName))
					throw TEMPLATE_INVALID_EXCEPTION.create(templateName);
				else
				{
					data.removeTemplate(templateName);
					source.sendSuccess(Component.translatable(translationSlug+"remove.success", templateName, entityIn.getDisplayName()), true);
					return 15;
				}
			}
			else
				throw INVALID_ENTITY_EXCEPTION.create();
		}
		
		private static int clearTemplates(Entity entityIn, CommandSourceStack source) throws CommandSyntaxException
		{
			if(entityIn instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entityIn;
				LivingData data = LivingData.getCapability(living);
				if(!data.hasTemplates())
					throw TEMPLATE_MISSING_EXCEPTION.create(entityIn.getDisplayName());
				else
				{
					data.clearTemplates();
					source.sendSuccess(Component.translatable(translationSlug+"clear.success", entityIn.getDisplayName()), true);
					return 15;
				}
			}
			else
				throw INVALID_ENTITY_EXCEPTION.create();
		}
		
		private static int getTemplate(ResourceLocation templateName, Entity entityIn, CommandSourceStack source) throws CommandSyntaxException
		{
			if(entityIn instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entityIn;
				LivingData data = LivingData.getCapability(living);
				if(!data.hasTemplate(templateName))
					throw TEMPLATE_MISSING_EXCEPTION.create(entityIn.getDisplayName());
				else
				{
					source.sendSuccess(Component.translatable(translationSlug+"get.success", entityIn.getDisplayName(), templateName), true);
					return 15;
				}
			}
			else
				throw INVALID_ENTITY_EXCEPTION.create();
		}
	}
}
