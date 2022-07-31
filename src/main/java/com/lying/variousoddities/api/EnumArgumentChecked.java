package com.lying.variousoddities.api;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

/**
 * Band-aid solution to a Forge issue in command argument parsing that leads to empty strings in enum arguments.<br>
 * This results in a 
 * @author Lying
 */
public class EnumArgumentChecked<T extends Enum<T>> implements ArgumentType<T>
{
	public final SimpleCommandExceptionType ENUM_NOT_FOUND;
    private final Class<T> enumClass;
    
    public static <R extends Enum<R>> EnumArgumentChecked<R> enumArgument(Class<R> enumClass)
    {
        return new EnumArgumentChecked<>(enumClass, enumClass.getSimpleName());
    }
    
    protected EnumArgumentChecked(final Class<T> enumClass, String enumName)
    {
        this.enumClass = enumClass;
        ENUM_NOT_FOUND = new SimpleCommandExceptionType(Component.translatable("argument."+enumName.toLowerCase()+".notfound"));
    }
    
    public T parse(final StringReader reader) throws CommandSyntaxException
    {
    	String str = reader.readUnquotedString();
    	if(str == null || str.length() == 0)
    		throw ENUM_NOT_FOUND.create();
    	
    	for(T val : enumClass.getEnumConstants())
    		if(val.name().equalsIgnoreCase(str))
    			return val;
    	
    	throw ENUM_NOT_FOUND.create();
    }
    
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder)
    {
        return SharedSuggestionProvider.suggest(Stream.of(enumClass.getEnumConstants()).map(Object::toString), builder);
    }
    
    public Collection<String> getExamples()
    {
        return Stream.of(enumClass.getEnumConstants()).map(Object::toString).collect(Collectors.toList());
    }
    
    public static class Serializer implements IArgumentSerializer<EnumArgumentChecked<?>>
    {
        public void write(EnumArgumentChecked<?> argument, FriendlyByteBuf buffer)
        {
            buffer.writeUtf(argument.enumClass.getName());
        }
        
        @SuppressWarnings({"unchecked", "rawtypes"})
        public EnumArgumentChecked<?> read(FriendlyByteBuf buffer)
        {
            try
            {
                String name = buffer.readUtf();
                return new EnumArgumentChecked(Class.forName(name), name);
            }
            catch (ClassNotFoundException e)
            {
                return null;
            }
        }
        
        public void write(EnumArgumentChecked<?> argument, JsonObject json)
        {
            json.addProperty("enum", argument.enumClass.getName());
        }
    }
}
