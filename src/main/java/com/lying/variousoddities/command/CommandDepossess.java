package com.lying.variousoddities.command;

import java.util.List;
import java.util.UUID;

import com.google.common.base.Predicate;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.PlayerData.BodyCondition;
import com.lying.variousoddities.capabilities.PlayerData.SoulCondition;
import com.lying.variousoddities.reference.Reference;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandDepossess extends CommandBase
{
    private static final SimpleCommandExceptionType INVALID_ENTITY_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("argument.entity.invalid"));
 	
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".depossess.";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> literal = newLiteral("depossess").requires((source) -> { return source.hasPermissionLevel(2); } )
			.executes((source) -> { return tryPossessFromPlayer(source.getSource()); });
		
		dispatcher.register(literal);
	}
	
	private static int tryPossessFromPlayer(CommandSource source) throws CommandSyntaxException
	{
		PlayerEntity player = null;
		try
		{
			player = source.asPlayer();
		}
		catch(Exception e){ }
		if(player == null)
			return 0;
		
		PlayerData playerData = PlayerData.forPlayer(player);
		if(!playerData.isPossessing())
			throw INVALID_ENTITY_EXCEPTION.create();
		
		UUID possessedID = playerData.getPossessing();
		
		playerData.stopPossessing();
		playerData.setBodyCondition(BodyCondition.ALIVE);
		playerData.setSoulCondition(SoulCondition.ALIVE);
		
		LivingEntity target = null;
		List<MobEntity> candidates = player.getEntityWorld().getEntitiesWithinAABB(MobEntity.class, player.getBoundingBox().grow(128D), new Predicate<MobEntity>()
		{
			public boolean apply(MobEntity input){ return input.getUniqueID().equals(possessedID); }
		});
		if(!candidates.isEmpty())
			target = candidates.get(0);
		
		if(target != null)
			LivingData.forEntity(target).setPossessedBy(null);
		else
			throw INVALID_ENTITY_EXCEPTION.create();
		
		source.sendFeedback(new TranslationTextComponent(translationSlug+"end", target.getDisplayName()), true);
		
		return 15;
	}
}
