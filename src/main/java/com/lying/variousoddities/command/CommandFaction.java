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

import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;

public class CommandFaction extends CommandBase
{
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".faction.";
	
	private static final String PLAYER = "player";
	private static final String AMOUNT = "amount";
	private static final String FACTION = "faction";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> literal = newLiteral("faction")
				.then(newLiteral("reputation")
						.then(VariantGive.build())
						.then(VariantSet.build())
						.then(VariantGet.build())
						.then(VariantList.build())
						.then(VariantReset.build()))
				.then(VariantManage.build());
		
		dispatcher.register(literal);
	}
	
	public static ITextComponent factionToInfo(String faction)
	{
		return new StringTextComponent(faction).modifyStyle((style) -> { return style.setFormatting(TextFormatting.DARK_AQUA).setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new TranslationTextComponent("command.varodd.faction.manage.click"))).setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.valueOf("/faction manage info "+faction))); } );
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
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("give")
					.then(newArgument(PLAYER, EntityArgument.player())
	    				.then(newArgument(AMOUNT, IntegerArgumentType.integer(-200, 200))
	    					.then(newLiteral("with")
		    					.then(newArgument(FACTION, StringArgumentType.word())
		    							.executes((source) -> { return givePlayerRep(EntityArgument.getPlayer(source, PLAYER), StringArgumentType.getString(source, FACTION), IntegerArgumentType.getInteger(source, AMOUNT), source.getSource()); })))));
    	}
    	
    	private static int givePlayerRep(PlayerEntity player, String faction, int amount, CommandSource source)
    	{
    		FactionReputation.addPlayerReputation(player, faction, ReputationChange.COMMAND, amount, null);
    		source.sendFeedback(new TranslationTextComponent(translationSlug+"give.success", player.getName(), amount, factionToInfo(faction)), true);
    		return (int)(15F * ((float)(FactionReputation.getPlayerReputation(player, faction) + 100) / 200F));
    	}
	}
	
	private static class VariantSet
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("set")
					.then(newArgument(PLAYER, EntityArgument.player())
		    				.then(newArgument(AMOUNT, IntegerArgumentType.integer(-100, 100))
		    					.then(newLiteral("with")
			    					.then(newArgument(FACTION, StringArgumentType.word())
			    							.executes((source) -> { return setPlayerRep(EntityArgument.getPlayer(source, PLAYER), StringArgumentType.getString(source, FACTION), IntegerArgumentType.getInteger(source, AMOUNT), source.getSource()); })))));
    	}
    	
    	private static int setPlayerRep(PlayerEntity player, String faction, int amount, CommandSource source)
    	{
    		FactionReputation.setPlayerReputation(player, faction, amount);
    		source.sendFeedback(new TranslationTextComponent(translationSlug+"set.success", player.getName(), factionToInfo(faction), amount), true);
    		return repToStrength(FactionReputation.getPlayerReputation(player, faction));
    	}
	}
	
	private static class VariantReset
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("reset")
					.then(newArgument(PLAYER, EntityArgument.player())
    					.then(newLiteral("with")
	    					.then(newArgument(FACTION, StringArgumentType.word())
    							.executes((source) -> { return resetPlayerRep(EntityArgument.getPlayer(source, PLAYER), StringArgumentType.getString(source, FACTION), source.getSource()); }))));
    	}
    	
    	private static int resetPlayerRep(PlayerEntity player, String faction, CommandSource source)
    	{
    		FactionManager manager = FactionManager.get(player.getEntityWorld());
    		Faction fac = manager.getFaction(faction);
    		if(fac == null)
	    		source.sendFeedback(new TranslationTextComponent(translationSlug+"reset.failed", player.getName(), faction), true);
    		else
    		{
	    		int reputation = fac.startingRep;
	    		FactionReputation.setPlayerReputation(player, faction, reputation);
	    		source.sendFeedback(new TranslationTextComponent(translationSlug+"reset.success", player.getName(), factionToInfo(faction), reputation), true);
	    	}
    		return repToStrength(FactionReputation.getPlayerReputation(player, faction));
    	}
	}
	
	private static class VariantGet
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("get")
					.then(newArgument(PLAYER, EntityArgument.player())
    					.then(newLiteral("with")
	    					.then(newArgument(FACTION, StringArgumentType.word())
	    						.executes((source) -> { return getPlayerRep(EntityArgument.getPlayer(source, PLAYER), StringArgumentType.getString(source, FACTION), source.getSource()); }))));
    	}
    	
    	private static int getPlayerRep(PlayerEntity player, String faction, CommandSource source)
    	{
    		int reputation = FactionReputation.getPlayerReputation(player, faction);
    		source.sendFeedback(new TranslationTextComponent(translationSlug+"get.success", player.getName(), factionToInfo(faction), reputation), true);
    		return repToStrength(reputation);
    	}
	}
	
	private static class VariantList
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("list")
					.then(newArgument(PLAYER, EntityArgument.player())
						.executes((source) -> { return listPlayerRep(EntityArgument.getPlayer(source, PLAYER), source.getSource()); }));
    	}
		
		private static int listPlayerRep(PlayerEntity player, CommandSource source)
		{
			FactionManager manager = FactionManager.get(source.getWorld());
			if(!manager.isEmpty())
			{
				source.sendFeedback(new TranslationTextComponent(translationSlug+"list", player.getName()), true);
    			Set<String> names = manager.factionNames();
    			for(String faction : names)
    			{
    				int reputation = FactionReputation.getPlayerReputation(player, faction);
    				source.sendFeedback(new StringTextComponent("-").append(new TranslationTextComponent(translationSlug+"list.entry", TextComponentUtils.wrapWithSquareBrackets(factionToInfo(faction)), reputation, EnumAttitude.fromRep(reputation).getTranslatedName())), false);
    			}
    			
    			return 15;
			}
			return 0;
		}
	}
	
	private static class VariantManage
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("manage")
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
    		
    		public static LiteralArgumentBuilder<CommandSource> build()
    		{
    			return newLiteral("add")
    					.then(newArgument(FACTION, StringArgumentType.word())
    						.executes((source) -> { return createFaction(StringArgumentType.getString(source, FACTION), 0, null, source.getSource()); })
    						.then(newArgument(REPUTATION, IntegerArgumentType.integer(-100, 100))
    							.executes((source) -> { return createFaction(StringArgumentType.getString(source, FACTION), IntegerArgumentType.getInteger(source, REPUTATION), null, source.getSource()); })
    							.then(newArgument(NBT, NBTCompoundTagArgument.nbt())
    								.executes((source) -> { return createFaction(StringArgumentType.getString(source, FACTION), IntegerArgumentType.getInteger(source, REPUTATION), NBTCompoundTagArgument.getNbt(source, NBT), source.getSource()); }))));
    		}
    		
    		private static int createFaction(String name, int startRep, CompoundNBT compound, CommandSource source)
    		{
    			FactionManager manager = FactionManager.get(source.getWorld());
    			
    			Faction faction = new Faction(name, startRep);
    			if(compound != null)
    			{
    				CompoundNBT factionData = faction.writeToNBT(new CompoundNBT());
    				factionData.merge(compound);
    				faction = Faction.readFromNBT(factionData);
    				
    				if(faction == null)
    					return 0;
    			}
    			
				manager.add(faction);
				source.sendFeedback(new TranslationTextComponent(translationSlug+"manage.add", TextComponentUtils.wrapWithSquareBrackets(factionToInfo(faction.name))), true);
    			return 15;
    		}
    	}
    	
    	private static class VariantDelete
    	{
    		public static LiteralArgumentBuilder<CommandSource> build()
    		{
    			return newLiteral("remove")
    					.then(newArgument(FACTION, StringArgumentType.word())
    						.executes((source) -> { return delete(StringArgumentType.getString(source, FACTION), source.getSource()); }))
    					.then(newLiteral("all")
    						.executes((source) -> { return deleteAll(source.getSource()); }));
    		}
    		
    		private static int delete(String faction, CommandSource source)
    		{
    			FactionManager manager = FactionManager.get(source.getWorld());
    			manager.remove(faction);
    			source.sendFeedback(new TranslationTextComponent(translationSlug+"manage.remove", faction), true);
    			return 15;
    		}
    		
    		private static int deleteAll(CommandSource source)
    		{
    			FactionManager manager = FactionManager.get(source.getWorld());
    			int size = manager.size();
    			manager.clear();
    			source.sendFeedback(new TranslationTextComponent(translationSlug+"manage.clear", size), true);
    			return size;
    		}
    	}
    	
    	private static class VariantRelation
    	{
    		private static final String FACTION_A = "faction A";
    		private static final String FACTION_B = "faction B";
    		private static final String REPUTATION = "reputation";
    		
    		public static LiteralArgumentBuilder<CommandSource> build()
    		{
    			return newLiteral("relation")
    					.then(newArgument(FACTION_A, StringArgumentType.word())
    						.then(newLiteral("with")
    							.then(newArgument(FACTION_B, StringArgumentType.word())
    								.then(newArgument(REPUTATION, IntegerArgumentType.integer(-100, 100))
    									.executes((source) -> { return addRelation(StringArgumentType.getString(source, FACTION_A), StringArgumentType.getString(source, FACTION_B), IntegerArgumentType.getInteger(source, REPUTATION), source.getSource()); })))));
    		}
    		
    		public static int addRelation(String factionA, String factionB, int rep, CommandSource source)
    		{
    			FactionManager manager = FactionManager.get(source.getWorld());
    			
    			Faction facA = manager.getFaction(factionA);
    			if(facA == null)
    				return 0;
    			else
    			{
    				facA.addRelation(factionB, rep);
    				manager.markDirty();
    				source.sendFeedback(new TranslationTextComponent(translationSlug+"manage.relation", factionA, factionB, rep, EnumAttitude.fromRep(rep).getTranslatedName()), true);
    				return 15;
    			}
    		}
    	}
    	
    	private static class VariantReload
    	{
    		public static LiteralArgumentBuilder<CommandSource> build()
    		{
    			return newLiteral("reload").executes((source) -> { return reload(source.getSource()); });
    		}
    		
    		private static int reload(CommandSource source)
    		{
    			FactionManager manager = FactionManager.get(source.getWorld());
    			manager.clear();
    			if(ConfigVO.MOBS.factionSettings.factionsInConfig())
    				manager.stringToFactions(ConfigVO.MOBS.factionSettings.factionString());
    			source.sendFeedback(new TranslationTextComponent(translationSlug+"manage.reload", manager.size()), true);
    			return manager.size();
    		}
    	}
    	
    	private static class VariantList
    	{
    		public static LiteralArgumentBuilder<CommandSource> build()
    		{
    			return newLiteral("list")
    					.executes((source) -> { return listFactions(source.getSource()); });
    		}
    		
    		private static int listFactions(CommandSource source)
    		{
    			FactionManager manager = FactionManager.get(source.getWorld());
    			Set<String> names = manager.factionNames();
    			source.sendFeedback(new TranslationTextComponent(translationSlug+"manage.list", names.size()), true);
    			for(String faction : names)
    				source.sendFeedback(new StringTextComponent("-").append(TextComponentUtils.wrapWithSquareBrackets(factionToInfo(faction))), false);
    			return manager.factionNames().size();
    		}
    	}
    	
    	private static class VariantInfo
    	{
    		public static LiteralArgumentBuilder<CommandSource> build()
    		{
    			return newLiteral("info")
    					.then(newArgument(FACTION, StringArgumentType.word())
    						.executes((source) -> { return factionInfo(StringArgumentType.getString(source, FACTION), source.getSource()); }));
    		}
    		
    		private static int factionInfo(String faction, CommandSource source)
    		{
    			FactionManager manager = FactionManager.get(source.getWorld());
    			Faction fac = manager.getFaction(faction);
    			if(fac == null)
    				return 0;
    			else
    			{
    				source.sendFeedback(new TranslationTextComponent(translationSlug+"manage.info.name", fac.name), true);
    				source.sendFeedback(new TranslationTextComponent(translationSlug+"manage.info.starting_rep", fac.startingRep, EnumAttitude.fromRep(fac.startingRep).getTranslatedName()), false);
    				
    				Map<String, Integer> relations = fac.getRelations();
    				if(!relations.isEmpty())
    				{
    					source.sendFeedback(new TranslationTextComponent(translationSlug+"manage.info.relations"), false);
	    				for(String relation : relations.keySet())
	    				{
	    					int rep = relations.get(relation);
	    					source.sendFeedback(new StringTextComponent("-").append(new TranslationTextComponent(translationSlug+"list.entry", TextComponentUtils.wrapWithSquareBrackets(factionToInfo(relation)), rep, EnumAttitude.fromRep(rep).getTranslatedName())), false);
	    				}
    				}
        			return 15;
    			}
    		}
    	}
	}
}