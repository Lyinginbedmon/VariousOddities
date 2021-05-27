package com.lying.variousoddities.command;

import java.util.Set;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.SpeciesRegistry;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.types.Types;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
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
 	
	private static final DynamicCommandExceptionType SPECIES_MISSING_EXCEPTION = new DynamicCommandExceptionType((p_208922_0_) -> {
		return new TranslationTextComponent("command.varodd.abilities.species.missing", p_208922_0_);
	});
	private static final DynamicCommandExceptionType SPECIES_INVALID_EXCEPTION = new DynamicCommandExceptionType((p_208922_0_) -> {
		return new TranslationTextComponent("command.varodd.abilities.species.invalid", p_208922_0_);
	});
	
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".species.";
	private static final String ENTITY = "entity";
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
				.then(newLiteral("reload")
					.executes((source) -> { return reloadSpecies(source.getSource()); }))
				.then(newLiteral("get")
					.then(newArgument(ENTITY, EntityArgument.entity())
						.executes((source) -> { return getSpecies(EntityArgument.getEntity(source, ENTITY), source.getSource()); })))
				.then(newLiteral("set")
					.then(newArgument(ENTITY, EntityArgument.entity()).then(newLiteral("to")).then(newArgument(NAME, ResourceLocationArgument.resourceLocation()).suggests(SPECIES_SUGGESTIONS)
						.executes((source) -> { return setSpecies(EntityArgument.getEntity(source, ENTITY), ResourceLocationArgument.getResourceLocation(source, NAME), source.getSource()); }))));
		
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
		
		source.sendFeedback(new TranslationTextComponent(translationSlug+"info_name", species.getRegistryName().toString()), true);
		source.sendFeedback((new Types(species.getTypes())).toHeader(), false);
		if(!species.getAbilities().isEmpty())
		{
			source.sendFeedback(new TranslationTextComponent(translationSlug+"info_abilities"), false);
			for(Ability ability : species.getAbilities())
				source.sendFeedback(new StringTextComponent(" -").append(ability.getDisplayName()), false);
		}
		
		return 15;
	}
	
	private static int reloadSpecies(CommandSource source)
	{
		MinecraftServer server = source.getServer();
		server.getDataPackRegistries().getResourceManager();
		return 15;
	}
	
	private static int getSpecies(Entity entity, CommandSource source) throws CommandSyntaxException
	{
		if(entity instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)entity;
			LivingData data = LivingData.forEntity(living);
			if(data.getSpecies() != null)
				source.sendFeedback(new TranslationTextComponent(translationSlug+"get", living.getDisplayName(), data.getSpecies().getRegistryName()), true);
			else
				throw SPECIES_MISSING_EXCEPTION.create(living.getDisplayName());
		}
		return 15;
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
			}
			else
				throw SPECIES_INVALID_EXCEPTION.create(name);
		}
		return 15;
	}
}
