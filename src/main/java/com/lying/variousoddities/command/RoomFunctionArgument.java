package com.lying.variousoddities.command;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.lying.variousoddities.api.world.settlement.EnumRoomFunction;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;

public class RoomFunctionArgument implements ArgumentType<EnumRoomFunction>
{
	public static final DynamicCommandExceptionType FUNCTION_INVALID = new DynamicCommandExceptionType((color) -> {
		return new TranslationTextComponent("argument.room_function.invalid", color);
		});
	
	private RoomFunctionArgument()
	{
		
	}
	
	public static RoomFunctionArgument function()
	{
		return new RoomFunctionArgument();
	}
	
	public static EnumRoomFunction getFunction(CommandContext<CommandSource> context, String name)
	{
		return context.getArgument(name, EnumRoomFunction.class);
	}
	
	public EnumRoomFunction parse(StringReader reader) throws CommandSyntaxException
	{
		String name = reader.readUnquotedString();
		EnumRoomFunction function = EnumRoomFunction.fromName(name);
		if(function != null)
			return function;
		else
			throw FUNCTION_INVALID.create(name);
	}
	
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
	{
		return ISuggestionProvider.suggest(EnumRoomFunction.names(), builder);
	}
	
	public Collection<String> getExamples()
	{
		return EnumRoomFunction.names();
	}
	
	public static class Serializer implements IArgumentSerializer<RoomFunctionArgument>
	{
		public void write(RoomFunctionArgument argument, PacketBuffer buffer)
		{
			
		}
		
		public RoomFunctionArgument read(PacketBuffer buffer)
		{
			return function();
		}
		
		public void write(RoomFunctionArgument p_212244_1_, JsonObject p_212244_2_)
		{
			
		}
	}
}
