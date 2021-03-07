package com.lying.variousoddities.command;

import java.util.concurrent.CompletableFuture;

import com.lying.variousoddities.world.savedata.FactionManager;
import com.lying.variousoddities.world.savedata.FactionManager.Faction;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.TranslationTextComponent;

public class FactionArgument implements ArgumentType<String>
{
	public static final SimpleCommandExceptionType FACTION_NOT_FOUND = new SimpleCommandExceptionType(new TranslationTextComponent("argument.faction.notfound"));
	   
	private FactionArgument()
	{
		
	}
	
	public static FactionArgument faction()
	{
		return new FactionArgument();
	}
	
	public static String getFactionName(CommandContext<CommandSource> context, String name)
	{
		return StringArgumentType.getString(context, name);
	}
	
	public static Faction getFaction(CommandContext<CommandSource> context, String name) throws CommandSyntaxException
	{
		FactionManager manager = FactionManager.get(context.getSource().getWorld());
		Faction faction = manager.getFaction(getFactionName(context, name));
		if(faction == null)
			throw FACTION_NOT_FOUND.create();
		else
			return faction;
	}
	
	public String parse(StringReader reader) throws CommandSyntaxException
	{
		return reader.readUnquotedString();
	}
	
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		return ISuggestionProvider.suggest(FactionManager.defaultFactions(), builder);
	}
}
