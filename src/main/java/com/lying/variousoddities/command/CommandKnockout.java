package com.lying.variousoddities.command;

import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.reference.Reference;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandKnockout extends CommandBase
{
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".knockout.";
	
	private static final String ENTITY = "target";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> literal = newLiteral("knockout").requires((source) -> { return source.hasPermissionLevel(2); } )
				.executes((source) -> { return knockOutSelf(source.getSource()); })
					.then(newArgument(ENTITY, EntityArgument.entity())
							.executes((source) -> { return knockOutTarget(source.getSource(), EntityArgument.getEntity(source, ENTITY)); }));
		
		dispatcher.register(literal);
	}
	
	private static int knockOutSelf(CommandSource source)
	{
		try
		{
			return knockOutTarget(source, source.asPlayer());
		}
		catch (CommandSyntaxException e)
		{
			source.sendErrorMessage(new TranslationTextComponent(translationSlug+"failed", source.getName()));
			return 0;
		}
	}
	
	private static int knockOutTarget(CommandSource source, Entity entity)
	{
		if(entity == null)
		{
			source.sendErrorMessage(new TranslationTextComponent(translationSlug+"invalid"));
			return 0;
		}
		else if(entity instanceof LivingEntity)
		{
			if(entity.attackEntityFrom(VODamageSource.BLUDGEON, Float.MAX_VALUE))
			{
				source.sendFeedback(new TranslationTextComponent(translationSlug+"success", entity.getDisplayName()), true);
				return 15;
			}
		}
		
		source.sendErrorMessage(new TranslationTextComponent(translationSlug+"failed", source.getName()));
		return 0;
	}
}
