package com.lying.variousoddities.command;

import com.lying.variousoddities.reference.Reference;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;

public class CommandFaction extends CommandBase
{
	@SuppressWarnings("unused")
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".faction.";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> literal = newLiteral("faction")
				.then(newLiteral("reputation")
					.then(VariantAdd.build())
					.then(VariantRemove.build())
					.then(VariantSet.build())
					.then(VariantGet.build()))
				.then(VariantManage.build());
		
		dispatcher.register(literal);
	}
	
	private static class VariantAdd
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("add");
    	}
	}
	
	private static class VariantRemove
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("remove");
    	}
	}
	
	private static class VariantSet
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("set");
    	}
	}
	
	private static class VariantGet
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("get");
    	}
	}
	
	private static class VariantManage
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("manage")
    				.then(newLiteral("create"))	// Add new faction, inputs: Name, starting rep
    				.then(newLiteral("delete"))	// Remove existing faction, inputs: Name
    				.then(newLiteral("relation"))	// Add new faction, inputs: Name 1, Name 2, rep
    				.then(newLiteral("list"))	// List all existing factions
    				.then(newLiteral("info"));	// Show information on given faction, inputs: Name
    	}
	}
}
