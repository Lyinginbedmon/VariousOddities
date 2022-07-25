package com.lying.variousoddities.command;

import java.util.Map;
import java.util.Set;

import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.faction.FactionBus.ReputationChange;
import com.lying.variousoddities.faction.FactionReputation;
import com.lying.variousoddities.faction.FactionReputation.EnumAttitude;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.world.savedata.FactionManager;
import com.lying.variousoddities.world.savedata.FactionManager.Faction;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class CommandFaction extends CommandBase
{
	public static final SimpleCommandExceptionType FACTION_NOT_FOUND = new SimpleCommandExceptionType(Component.translatable("argument.faction.notfound"));
 	public static final SuggestionProvider<CommandSourceStack> FACTION_SUGGEST = SuggestionProviders.register(new ResourceLocation("default_factions"), (context, builder) -> {
 		return ISuggestionProvider.suggest(FactionManager.defaultFactions(), builder);
 		});
 	
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".faction.";
	
	private static final String PLAYER = "player";
	private static final String AMOUNT = "amount";
	private static final String FACTION = "faction";
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("faction").requires((source) -> { return source.hasPermission(2); } )
				.then(Commands.literal("reputation")
						.then(VariantGive.build())
						.then(VariantSet.build())
						.then(VariantGet.build())
						.then(VariantList.build())
						.then(VariantReset.build()))
				.then(VariantManage.build());
		
		dispatcher.register(literal);
	}
	
	public static Faction getFaction(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException
	{
		FactionManager manager = FactionManager.get(context.getSource().getLevel());
		Faction faction = manager.getFaction(name);
		if(faction == null)
			throw FACTION_NOT_FOUND.create();
		else
			return faction;
	}
	
	public static Component factionToInfo(String faction)
	{
		return Component.literal(faction).withStyle((style) -> { return style.applyFormat(ChatFormatting.DARK_AQUA).withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Component.translatable("command.varodd.faction.manage.click"))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.valueOf("/faction manage info "+faction))); } );
	}
	
	/**
	 * Converts the ranged reputation value of -100 to +100 to a signal strength from 0 to 15;
	 * @param reputation
	 * @return
	 */
	public static int repToStrength(int reputation)
	{
		return (int)(15F * ((float)(reputation + 100) / 200F));
	}
	
	private static class VariantGive
	{
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("give")
					.then(Commands.argument(PLAYER, EntityArgument.player())
	    				.then(Commands.argument(AMOUNT, IntegerArgumentType.integer(-200, 200))
	    					.then(Commands.literal("with")
		    					.then(Commands.argument(FACTION, StringArgumentType.word()).suggests(FACTION_SUGGEST)
		    							.executes((source) -> { return givePlayerRep(EntityArgument.getPlayer(source, PLAYER), getFaction(source, StringArgumentType.getString(source, FACTION)), IntegerArgumentType.getInteger(source, AMOUNT), source.getSource()); })))));
    	}
    	
    	private static int givePlayerRep(Player player, Faction faction, int amount, CommandSourceStack source)
    	{
    		FactionReputation.addPlayerReputation(player, faction.name, ReputationChange.COMMAND, amount, null);
    		source.sendSuccess(Component.translatable(translationSlug+"give.success", player.getDisplayName(), amount, factionToInfo(faction.name)), true);
    		return (int)(15F * ((float)(FactionReputation.getPlayerReputation(player, faction.name) + 100) / 200F));
    	}
	}
	
	private static class VariantSet
	{
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("set")
					.then(Commands.argument(PLAYER, EntityArgument.player())
		    				.then(Commands.argument(AMOUNT, IntegerArgumentType.integer(-100, 100))
		    					.then(Commands.literal("with")
			    					.then(Commands.argument(FACTION, StringArgumentType.word()).suggests(FACTION_SUGGEST)
			    							.executes((source) -> { return setPlayerRep(EntityArgument.getPlayer(source, PLAYER), getFaction(source, StringArgumentType.getString(source, FACTION)), IntegerArgumentType.getInteger(source, AMOUNT), source.getSource()); })))));
    	}
    	
    	private static int setPlayerRep(Player player, Faction faction, int amount, CommandSourceStack source)
    	{
    		FactionReputation.setPlayerReputation(player, faction.name, amount);
    		source.sendSuccess(Component.translatable(translationSlug+"set.success", player.getDisplayName(), factionToInfo(faction.name), amount), true);
    		return repToStrength(FactionReputation.getPlayerReputation(player, faction.name));
    	}
	}
	
	private static class VariantReset
	{
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("reset")
					.then(Commands.argument(PLAYER, EntityArgument.player())
    					.then(Commands.literal("with")
	    					.then(Commands.argument(FACTION, StringArgumentType.word()).suggests(FACTION_SUGGEST)
    							.executes((source) -> { return resetPlayerRep(EntityArgument.getPlayer(source, PLAYER), getFaction(source, StringArgumentType.getString(source, FACTION)), source.getSource()); }))));
    	}
    	
    	private static int resetPlayerRep(Player player, Faction faction, CommandSourceStack source)
    	{
    		if(faction == null)
	    		source.sendFailure(Component.translatable(translationSlug+"reset.failed", player.getDisplayName(), faction));
    		else
    		{
	    		int reputation = faction.startingRep;
	    		FactionReputation.setPlayerReputation(player, faction.name, reputation);
	    		source.sendSuccess(Component.translatable(translationSlug+"reset.success", player.getDisplayName(), factionToInfo(faction.name), reputation), true);
	    	}
    		return repToStrength(FactionReputation.getPlayerReputation(player, faction.name));
    	}
	}
	
	private static class VariantGet
	{
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("get")
					.then(Commands.argument(PLAYER, EntityArgument.player())
    					.then(Commands.literal("with")
	    					.then(Commands.argument(FACTION, StringArgumentType.word()).suggests(FACTION_SUGGEST)
	    						.executes((source) -> { return getPlayerRep(EntityArgument.getPlayer(source, PLAYER), getFaction(source, StringArgumentType.getString(source, FACTION)), source.getSource()); }))));
    	}
    	
    	private static int getPlayerRep(Player player, Faction faction, CommandSourceStack source)
    	{
    		int reputation = FactionReputation.getPlayerReputation(player, faction.name);
    		source.sendSuccess(Component.translatable(translationSlug+"get.success", player.getDisplayName(), factionToInfo(faction.name), reputation), true);
    		return repToStrength(reputation);
    	}
	}
	
	private static class VariantList
	{
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("list")
					.then(Commands.argument(PLAYER, EntityArgument.player())
						.executes((source) -> { return listPlayerRep(EntityArgument.getPlayer(source, PLAYER), source.getSource()); }));
    	}
		
		private static int listPlayerRep(Player player, CommandSourceStack source)
		{
			FactionManager manager = FactionManager.get(source.getLevel());
			if(!manager.isEmpty())
			{
				source.sendSuccess(Component.translatable(translationSlug+"list", player.getDisplayName()), true);
    			Set<String> names = manager.factionNames();
    			for(String faction : names)
    			{
    				int reputation = FactionReputation.getPlayerReputation(player, faction);
    				source.sendSuccess(Component.literal("-").append(Component.translatable(translationSlug+"list.entry", ComponentUtils.wrapInSquareBrackets(factionToInfo(faction)), reputation, EnumAttitude.fromRep(reputation).getTranslatedName())), false);
    			}
    			
    			return 15;
			}
			return 0;
		}
	}
	
	private static class VariantManage
	{
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("manage")
    				.then(VariantAdd.build())		// Add new faction, inputs: Name, starting rep
    				.then(VariantDelete.build())	// Remove existing faction, inputs: Name
    				.then(VariantReload.build())	// Clear existing factions and reload from config
    				.then(VariantRelation.build())	// Add new faction relation, inputs: Name 1, Name 2, rep
    				.then(VariantList.build())		// List all existing factions
    				.then(VariantInfo.build());		// Show information on given faction, inputs: Name
    	}
    	
    	private static class VariantAdd
    	{
    		private static final String REPUTATION = "reputation";
    		private static final String NBT = "nbt";
    		
    		public static LiteralArgumentBuilder<CommandSourceStack> build()
    		{
    			return Commands.literal("add")
    					.then(Commands.argument(FACTION, StringArgumentType.word())
    						.executes((source) -> { return createFaction(StringArgumentType.getString(source, FACTION), 0, null, source.getSource()); })
    						.then(Commands.argument(REPUTATION, IntegerArgumentType.integer(-100, 100))
    							.executes((source) -> { return createFaction(StringArgumentType.getString(source, FACTION), IntegerArgumentType.getInteger(source, REPUTATION), null, source.getSource()); })
    							.then(Commands.argument(NBT, CompoundTagArgument.compoundTag())
    								.executes((source) -> { return createFaction(StringArgumentType.getString(source, FACTION), IntegerArgumentType.getInteger(source, REPUTATION), CompoundTagArgument.getCompoundTag(source, NBT), source.getSource()); }))));
    		}
    		
    		private static int createFaction(String name, int startRep, CompoundTag compound, CommandSourceStack source)
    		{
    			FactionManager manager = FactionManager.get(source.getLevel());
    			
    			Faction faction = new Faction(name, startRep);
    			if(compound != null)
    			{
    				CompoundTag factionData = faction.writeToNBT(new CompoundTag());
    				factionData.merge(compound);
    				faction = Faction.readFromNBT(factionData);
    				
    				if(faction == null)
    					return 0;
    			}
    			
				manager.add(faction);
				source.sendSuccess(Component.translatable(translationSlug+"manage.add", ComponentUtils.wrapInSquareBrackets(factionToInfo(faction.name))), true);
    			return 15;
    		}
    	}
    	
    	private static class VariantDelete
    	{
    		public static LiteralArgumentBuilder<CommandSourceStack> build()
    		{
    			return Commands.literal("remove")
    					.then(Commands.argument(FACTION, StringArgumentType.word()).suggests(FACTION_SUGGEST)
    						.executes((source) -> { return delete(StringArgumentType.getString(source, FACTION), source.getSource()); }))
    					.then(Commands.literal("all")
    						.executes((source) -> { return deleteAll(source.getSource()); }));
    		}
    		
    		private static int delete(String faction, CommandSourceStack source)
    		{
    			FactionManager manager = FactionManager.get(source.getLevel());
    			manager.remove(faction);
    			source.sendSuccess(Component.translatable(translationSlug+"manage.remove", faction), true);
    			return 15;
    		}
    		
    		private static int deleteAll(CommandSourceStack source)
    		{
    			FactionManager manager = FactionManager.get(source.getLevel());
    			int size = manager.size();
    			manager.clear();
    			source.sendSuccess(Component.translatable(translationSlug+"manage.clear", size), true);
    			return size;
    		}
    	}
    	
    	private static class VariantRelation
    	{
    		private static final String FACTION_A = "faction A";
    		private static final String FACTION_B = "faction B";
    		private static final String REPUTATION = "reputation";
    		
    		public static LiteralArgumentBuilder<CommandSourceStack> build()
    		{
    			return Commands.literal("relation")
    					.then(Commands.argument(FACTION_A, StringArgumentType.word()).suggests(FACTION_SUGGEST)
    						.then(Commands.literal("with")
    							.then(Commands.argument(FACTION_B, StringArgumentType.word()).suggests(FACTION_SUGGEST)
    								.then(Commands.argument(REPUTATION, IntegerArgumentType.integer(-100, 100))
    									.executes((source) -> { return addRelation(getFaction(source, StringArgumentType.getString(source, FACTION_A)), getFaction(source, StringArgumentType.getString(source, FACTION_B)), IntegerArgumentType.getInteger(source, REPUTATION), source.getSource()); })))));
    		}
    		
    		public static int addRelation(Faction factionA, Faction factionB, int rep, CommandSourceStack source)
    		{
    			FactionManager manager = FactionManager.get(source.getLevel());
    			if(factionA == null)
    				return 0;
    			else
    			{
    				factionA.addRelation(factionB, rep);
    				manager.markDirty();
    				source.sendSuccess(Component.translatable(translationSlug+"manage.relation", factionA.name, factionB.name, rep, EnumAttitude.fromRep(rep).getTranslatedName()), true);
    				return 15;
    			}
    		}
    	}
    	
    	private static class VariantReload
    	{
    		public static LiteralArgumentBuilder<CommandSourceStack> build()
    		{
    			return Commands.literal("reload").executes((source) -> { return reload(source.getSource()); });
    		}
    		
    		private static int reload(CommandSourceStack source)
    		{
    			FactionManager manager = FactionManager.get(source.getLevel());
    			manager.clear();
    			if(ConfigVO.MOBS.factionSettings.factionsInConfig())
    				manager.stringToFactions(ConfigVO.MOBS.factionSettings.factionString());
    			source.sendSuccess(Component.translatable(translationSlug+"manage.reload", manager.size()), true);
    			return manager.size();
    		}
    	}
    	
    	private static class VariantList
    	{
    		public static LiteralArgumentBuilder<CommandSourceStack> build()
    		{
    			return Commands.literal("list")
    					.executes((source) -> { return listFactions(source.getSource()); });
    		}
    		
    		private static int listFactions(CommandSourceStack source)
    		{
    			FactionManager manager = FactionManager.get(source.getLevel());
    			Set<String> names = manager.factionNames();
    			source.sendSuccess(Component.translatable(translationSlug+"manage.list", names.size()), true);
    			for(String faction : names)
    				source.sendSuccess(Component.literal("-").append(ComponentUtils.wrapInSquareBrackets(factionToInfo(faction))), false);
    			return manager.factionNames().size();
    		}
    	}
    	
    	private static class VariantInfo
    	{
    		public static LiteralArgumentBuilder<CommandSourceStack> build()
    		{
    			return Commands.literal("info")
    					.then(Commands.argument(FACTION, StringArgumentType.word()).suggests(FACTION_SUGGEST)
    						.executes((source) -> { return factionInfo(getFaction(source, StringArgumentType.getString(source, FACTION)), source.getSource()); }));
    		}
    		
    		private static int factionInfo(Faction faction, CommandSourceStack source)
    		{
    			if(faction == null)
    				return 0;
    			else
    			{
    				source.sendSuccess(Component.translatable(translationSlug+"manage.info.name", faction.name), true);
    				source.sendSuccess(Component.translatable(translationSlug+"manage.info.starting_rep", faction.startingRep, EnumAttitude.fromRep(faction.startingRep).getTranslatedName()), false);
    				
    				Map<String, Integer> relations = faction.getRelations();
    				if(!relations.isEmpty())
    				{
    					source.sendSuccess(Component.translatable(translationSlug+"manage.info.relations"), false);
	    				for(String relation : relations.keySet())
	    				{
	    					int rep = relations.get(relation);
	    					source.sendSuccess(Component.literal("-").append(Component.translatable(translationSlug+"list.entry", ComponentUtils.wrapInSquareBrackets(factionToInfo(relation)), rep, EnumAttitude.fromRep(rep).getTranslatedName())), false);
	    				}
    				}
        			return 15;
    			}
    		}
    	}
	}
}
