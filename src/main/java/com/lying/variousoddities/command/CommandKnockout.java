package com.lying.variousoddities.command;

import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.reference.Reference;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class CommandKnockout extends CommandBase
{
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".knockout.";
	
	private static final String ENTITY = "target";
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("knockout").requires((source) -> { return source.hasPermission(2); } )
				.executes((source) -> { return knockOutSelf(source.getSource()); })
					.then(Commands.argument(ENTITY, EntityArgument.entity())
							.executes((source) -> { return knockOutTarget(source.getSource(), EntityArgument.getEntity(source, ENTITY)); }));
		
		dispatcher.register(literal);
	}
	
	private static int knockOutSelf(CommandSourceStack source)
	{
		try
		{
			return knockOutTarget(source, source.getPlayer());
		}
		catch (CommandSyntaxException e)
		{
			source.sendErrorMessage(Component.translatable(translationSlug+"failed", source.getDisplayName()));
			return 0;
		}
	}
	
	private static int knockOutTarget(CommandSourceStack source, Entity entity)
	{
		if(entity == null)
		{
			source.sendErrorMessage(Component.translatable(translationSlug+"invalid"));
			return 0;
		}
		else if(entity instanceof LivingEntity)
		{
			if(entity.hurt(VODamageSource.BLUDGEON, Float.MAX_VALUE))
			{
				source.sendFeedback(Component.translatable(translationSlug+"success", entity.getDisplayName()), true);
				return 15;
			}
		}
		
		source.sendErrorMessage(Component.translatable(translationSlug+"failed", source.getDisplayName()));
		return 0;
	}
}
