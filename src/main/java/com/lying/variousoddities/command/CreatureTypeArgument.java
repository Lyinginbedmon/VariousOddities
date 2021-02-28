package com.lying.variousoddities.command;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.lying.variousoddities.types.EnumCreatureType;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class CreatureTypeArgument implements ArgumentType<EnumCreatureType>
{
	public static final DynamicCommandExceptionType TYPE_INVALID = new DynamicCommandExceptionType((color) -> {
		return new TranslationTextComponent("argument.creature_type.invalid", color);
		});
 	public static final SuggestionProvider<CommandSource> SUPERTYPE_SUGGEST = SuggestionProviders.register(new ResourceLocation("creature_supertypes"), (context, builder) -> {
 		return ISuggestionProvider.suggest(EnumCreatureType.getSupertypeNames(), builder);
 		});
 	public static final SuggestionProvider<CommandSource> SUBTYPE_SUGGEST = SuggestionProviders.register(new ResourceLocation("creature_subtypes"), (context, builder) -> {
 		return ISuggestionProvider.suggest(EnumCreatureType.getSubtypeNames(), builder);
 		});
	
	private CreatureTypeArgument()
	{
		
	}
	
	public static CreatureTypeArgument type()
	{
		return new CreatureTypeArgument();
	}
	
	public static EnumCreatureType getType(CommandContext<CommandSource> context, String name)
	{
		return context.getArgument(name, EnumCreatureType.class);
	}
	
	public EnumCreatureType parse(StringReader reader) throws CommandSyntaxException
	{
		String name = reader.readUnquotedString();
		EnumCreatureType function = EnumCreatureType.fromName(name);
		if(function != null)
			return function;
		else
			throw TYPE_INVALID.create(name);
	}
	
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		return ISuggestionProvider.suggest(EnumCreatureType.names(), builder);
	}
	
	public Collection<String> getExamples()
	{
		return EnumCreatureType.names();
	}
	
	static
	{
		ArgumentTypes.register("creature_type", CreatureTypeArgument.class, new ArgumentSerializer<>(CreatureTypeArgument::type));
	}
}
