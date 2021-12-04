package com.lying.variousoddities.command;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.PlayerData.BodyCondition;
import com.lying.variousoddities.capabilities.PlayerData.SoulCondition;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.utility.VOHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeMod;

public class CommandPossess extends CommandBase
{
    private static final SimpleCommandExceptionType INVALID_ENTITY_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("argument.entity.invalid"));
 	
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".possess.";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> literal = newLiteral("possess").requires((source) -> { return source.hasPermissionLevel(2); } )
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
		
		LivingEntity target = VOHelper.getEntityLookTarget(player, player.getAttributeValue(ForgeMod.REACH_DISTANCE.get()));
		if(target != null && target instanceof MobEntity)
		{
			PlayerData playerData = PlayerData.forPlayer(player);
			playerData.setPossession(true);
			playerData.setPossessing(target.getUniqueID());
			playerData.setBodyCondition(BodyCondition.ALIVE);
			playerData.setSoulCondition(SoulCondition.ROAMING);
			
			LivingData.forEntity(target).setPossessedBy(player.getUniqueID());
			source.sendFeedback(new StringTextComponent("You are now possessing ").append(target.getDisplayName()), true);
		}
		else
			throw INVALID_ENTITY_EXCEPTION.create();
		
		return 15;
	}
}
