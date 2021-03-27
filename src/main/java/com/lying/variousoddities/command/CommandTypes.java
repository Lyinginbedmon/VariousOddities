package com.lying.variousoddities.command;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.api.EnumArgumentChecked;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.types.CreatureTypeDefaults;
import com.lying.variousoddities.types.EnumCreatureType;
import com.lying.variousoddities.world.savedata.TypesManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySummonArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class CommandTypes extends CommandBase
{
 	public static final SuggestionProvider<CommandSource> SUPERTYPE_SUGGEST = SuggestionProviders.register(new ResourceLocation("creature_supertypes"), (context, builder) -> {
 		return ISuggestionProvider.suggest(EnumCreatureType.getSupertypeNames(), builder);
 		});
 	public static final SuggestionProvider<CommandSource> SUBTYPE_SUGGEST = SuggestionProviders.register(new ResourceLocation("creature_subtypes"), (context, builder) -> {
 		return ISuggestionProvider.suggest(EnumCreatureType.getSubtypeNames(), builder);
 		});
 	public static final SuggestionProvider<CommandSource> TYPE_SUGGEST = SuggestionProviders.register(new ResourceLocation("creature_types"), (context, builder) -> {
	 	return ISuggestionProvider.suggest(EnumCreatureType.getTypeNames(), builder);
	 	});
 	
	private static final String TYPE = "type";
	private static final String MOB = "target";
	private static final String ENTITY = "entity";
	
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".types.";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> literal = newLiteral("types").requires((source) -> { return source.hasPermissionLevel(2); } )
			.then(VariantList.build())
			.then(VariantRemove.build())
			.then(VariantAdd.build())
			.then(VariantClear.build())
			.then(VariantSet.build())
			.then(VariantReset.build())
			.then(VariantTest.build());
		
		dispatcher.register(literal);
	}
	
	public static ITextComponent makeErrorMessage(String translation, Object... args)
	{
		return new TranslationTextComponent(translation, args).modifyStyle((style) -> {
			return style.setFormatting(TextFormatting.RED);
		});
	}
	
	private static class VariantList
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("list")
    				.then(newLiteral("all")
    					.executes(VariantList::listAll))
    				.then(newLiteral(TYPE)
    					.then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes(VariantList::listOfType)))
    				.then(newLiteral(ENTITY)
    					.then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
    						.executes((source) -> { return VariantList.listEntityId(EntitySummonArgument.getEntityId(source, ENTITY), source.getSource()); })))
					.then(newLiteral(MOB)
    					.then(newArgument(MOB, EntityArgument.entity())
    						.executes((source) -> { return VariantList.listEntity(EntityArgument.getEntity(source, MOB), source.getSource()); })))
					.then(newLiteral("players")
						.executes(VariantList::listPlayers));
    	}
    	
    	public static int listAll(final CommandContext<CommandSource> context)
    	{
    		CommandSource source = context.getSource();
    		
    		List<EnumCreatureType> supertypes = new ArrayList<>();
    		List<EnumCreatureType> subtypes = new ArrayList<>();
    		for(EnumCreatureType type : EnumCreatureType.values())
    			if(type.isSupertype())
    				supertypes.add(type);
    			else
    				subtypes.add(type);
    		
    		source.sendFeedback(new TranslationTextComponent(translationSlug+"list.all", supertypes.size(), subtypes.size()), true);
    		if(!supertypes.isEmpty())
    		{
        		java.util.Collections.sort(supertypes);
    			source.sendFeedback(new TranslationTextComponent(translationSlug+"list.all.supertypes").modifyStyle((style) -> { return style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(translationSlug+"list.all.supertypes.definition"))); }), false);
    			for(EnumCreatureType type : supertypes)
    				source.sendFeedback(new StringTextComponent(" -").append(type.getTranslated().modifyStyle((style) -> { return style.setFormatting(TextFormatting.DARK_AQUA).setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.valueOf("/types list type "+type.name().toLowerCase()))); })), false);
    		}
    		
    		if(!subtypes.isEmpty())
    		{
        		java.util.Collections.sort(subtypes);
    			source.sendFeedback(new TranslationTextComponent(translationSlug+"list.all.subtypes").modifyStyle((style) -> { return style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(translationSlug+"list.all.subtypes.definition"))); }), false);
    			for(EnumCreatureType type : subtypes)
    				source.sendFeedback(new StringTextComponent(" -").append(type.getTranslated().modifyStyle((style) -> { return style.setFormatting(TextFormatting.DARK_AQUA).setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.valueOf("/types list type "+type.name().toLowerCase()))); })), false);
    		}
    		
    		return 15;
    	}
    	
    	public static int listPlayers(final CommandContext<CommandSource> source)
    	{
    		TypesManager manager = TypesManager.get(source.getSource().getWorld());
    		List<String> playerNames = manager.getTypedPlayers();
    		boolean grey = !true;
    		source.getSource().sendFeedback(new TranslationTextComponent(translationSlug+"list.players.success", playerNames.size()), true);
    		for(String playerName : playerNames)
    		{
    			IFormattableTextComponent name = new StringTextComponent(playerName);
    			if(CreatureTypeDefaults.isTypedPatron(playerName))
    				name.modifyStyle((style) -> { return style.applyFormatting(TextFormatting.GOLD); });
    			listTypes(manager.getPlayerTypes(playerName, false), name, source.getSource(), grey = !grey, false);
    		}
    		return 15;
    	}
    	
    	public static int listOfType(final CommandContext<CommandSource> source)
    	{
    		EnumCreatureType type = source.getArgument(TYPE, EnumCreatureType.class);
    		TypesManager manager = TypesManager.get(source.getSource().getWorld());
    		List<ResourceLocation> mobs = manager.mobsOfType(type);
    		List<String> players = manager.playersOfType(type);
    		
    		ITextComponent typeName = type.getTranslated();
    		if(mobs.isEmpty() && players.isEmpty())
    			source.getSource().sendFeedback(makeErrorMessage(translationSlug+"list.failed", typeName), true);
    		else
    		{
    			if(!mobs.isEmpty())
    			{
					source.getSource().sendFeedback(new TranslationTextComponent(translationSlug+"list.success.mobs", mobs.size(), typeName), true);
					boolean grey = true;
		    		for(ResourceLocation string : mobs)
		    		{
		    			IFormattableTextComponent text = new StringTextComponent("-").append(new StringTextComponent(string.toString()));
		    			if(grey)
		    				text.modifyStyle((style) -> { return style.applyFormatting(TextFormatting.GRAY); });
		    			source.getSource().sendFeedback(text, false);
		    			grey = !grey;
		    		}
    			}
    			if(!players.isEmpty())
    			{
					source.getSource().sendFeedback(new TranslationTextComponent(translationSlug+"list.success.players", players.size(), typeName), true);
					boolean grey = true;
		    		for(String string : players)
		    		{
		    			IFormattableTextComponent text = new StringTextComponent("-").append(new StringTextComponent(string));
		    			if(grey)
		    				text.modifyStyle((style) -> { return style.applyFormatting(TextFormatting.GRAY); });
		    			source.getSource().sendFeedback(text, false);
		    			grey = !grey;
		    		}
    			}
    		}
    		
    		return Math.min(mobs.size() + players.size(), 15);
    	}
    	
    	public static int listEntityId(ResourceLocation entityId, CommandSource source)
    	{
    		TypesManager manager = TypesManager.get(source.getWorld());
    		return listTypes(manager.getMobTypes(entityId), new StringTextComponent(entityId.toString()), source, false, true);
    	}
    	
    	public static int listEntity(Entity entity, CommandSource source)
    	{
    		TypesManager manager = TypesManager.get(source.getWorld());
    		return listTypes(manager.getMobTypes(entity), entity.getName(), source, false, true);
    	}
    	
    	private static int listTypes(List<EnumCreatureType> types, ITextComponent identifier, CommandSource source, boolean grey, boolean log)
    	{
    		if(types.isEmpty())
    			source.sendFeedback(makeErrorMessage(translationSlug+"list.failed", identifier), true);
    		else
    		{
    			TranslationTextComponent text = new TranslationTextComponent(translationSlug+"list.success", identifier, EnumCreatureType.typesToHeader(types));
    			if(grey)
    				text.modifyStyle((style) -> { return style.applyFormatting(TextFormatting.GRAY); });
				source.sendFeedback(text, log);
    		}
    		return types.size();
    	}
	}
	
	private static class VariantAdd
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("add")
    				.then(newLiteral(ENTITY)
    					.then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantAdd.addToEntity(EntitySummonArgument.getEntityId(source, ENTITY), source.getArgument(TYPE, EnumCreatureType.class), source.getSource(), true); }))))
					.then(newLiteral(MOB)
    					.then(newArgument(MOB, EntityArgument.entity()).then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantAdd.addToMob(EntityArgument.getEntity(source, MOB), source.getArgument(TYPE, EnumCreatureType.class), source.getSource(), true); }))));
    	}
    	
    	public static int addToMob(Entity entity, EnumCreatureType type, CommandSource source, boolean report)
    	{
    		TypesManager manager = TypesManager.get(source.getWorld());
    		if(entity.getType() == EntityType.PLAYER)
    		{
    			if(type.getHandler().canApplyTo(manager.getMobTypes(entity)))
    			{
    				manager.addToPlayer(entity.getName().getUnformattedComponentText(), type, report);
    				
    				if(report)
    				{
		    			source.sendFeedback(new TranslationTextComponent(translationSlug+"add.success", type.getTranslated(), entity.getName()), true);
						source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success", entity.getName(), EnumCreatureType.typesToHeader(manager.getMobTypes(entity))), false);
    				}
    			}
    			return 15;
    		}
    		else if(entity instanceof LivingEntity)
    			return addToEntity(entity.getType().getRegistryName(), type, source, true);
    		
			source.sendFeedback(makeErrorMessage(translationSlug+"add.failed", entity.getName()), true);
    		return 0;
    	}
    	
    	public static int addToEntity(ResourceLocation registry, EnumCreatureType type, CommandSource source, boolean report)
    	{
    		TypesManager manager = TypesManager.get(source.getWorld());
			if(type.getHandler().canApplyTo(manager.getMobTypes(registry)))
			{
				manager.addToEntity(registry, type, report);
				
				if(report)
				{
					source.sendFeedback(new TranslationTextComponent(translationSlug+"add.success", type.getTranslated(), registry.toString()), true);
					source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success", registry.toString(), EnumCreatureType.typesToHeader(manager.getMobTypes(registry))), true);
				}
			}
    		return 15;
    	}
	}
	
	private static class VariantRemove
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("remove")
    				.then(newLiteral(ENTITY)
    					.then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantRemove.removeFromEntity(EntitySummonArgument.getEntityId(source, ENTITY), source.getArgument(TYPE, EnumCreatureType.class), source.getSource()); }))))
					.then(newLiteral(MOB)
    					.then(newArgument(MOB, EntityArgument.entity()).then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantRemove.removeFromMob(EntityArgument.getEntity(source, MOB), source.getArgument(TYPE, EnumCreatureType.class), source.getSource()); }))));
    	}
    	
    	public static int removeFromMob(Entity entity, EnumCreatureType type, CommandSource source)
    	{
    		TypesManager manager = TypesManager.get(source.getWorld());
    		if(entity.getType() == EntityType.PLAYER)
    		{
    			if(manager.isPlayerOfType((PlayerEntity)entity, type))
    				manager.removeFromPlayer(entity.getName().getUnformattedComponentText(), type, true);
    			
    			source.sendFeedback(new TranslationTextComponent(translationSlug+"remove.success", type.getTranslated(), entity.getName()), true);
				source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success", entity.getName(), EnumCreatureType.typesToHeader(manager.getMobTypes(entity))), false);
    			return 15;
    		}
    		else if(entity instanceof LivingEntity)
    			return removeFromEntity(entity.getType().getRegistryName(), type, source);
    		source.sendFeedback(makeErrorMessage(translationSlug+"remove.failed", entity.getName()), true);
    		return 0;
    	}
    	
    	public static int removeFromEntity(ResourceLocation registry, EnumCreatureType type, CommandSource source)
    	{
    		TypesManager manager = TypesManager.get(source.getWorld());
			if(manager.isMobOfType(registry, type))
				manager.removeFromEntity(registry, type, true);
			source.sendFeedback(new TranslationTextComponent(translationSlug+"remove.success", type.getTranslated(), registry.toString()), true);
			source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success", registry.toString(), EnumCreatureType.typesToHeader(manager.getMobTypes(registry))), true);
    		return 15;
    	}
	}
	
	private static class VariantClear
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("clear")
    				.then(newLiteral(ENTITY)
    					.then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
    						.executes((source) -> { return VariantClear.clearFromEntity(EntitySummonArgument.getEntityId(source, ENTITY), source.getSource(), true); })))
					.then(newLiteral(MOB)
    					.then(newArgument(MOB, EntityArgument.entity())
    						.executes((source) -> { return VariantClear.clearFromMob(EntityArgument.getEntity(source, MOB), source.getSource(), true); })));
    	}
    	
    	public static int clearFromMob(Entity entity, CommandSource source, boolean report)
    	{
    		if(entity.getType() == EntityType.PLAYER)
    		{
        		TypesManager manager = TypesManager.get(source.getWorld());
    			
    			for(EnumCreatureType type : manager.getPlayerTypes((PlayerEntity)entity, true))
    				manager.removeFromPlayer(entity.getName().getUnformattedComponentText(), type, false);
    			manager.markDirty();
        		if(report)
        			source.sendFeedback(new TranslationTextComponent(translationSlug+"clear.success", entity.getName()), true);
    			return 15;
    		}
    		else if(entity instanceof LivingEntity)
    			return clearFromEntity(entity.getType().getRegistryName(), source, true);
    		if(report)
    			source.sendFeedback(makeErrorMessage(translationSlug+"clear.failed", entity.getName()), true);
    		return 0;
    	}
    	
    	public static int clearFromEntity(ResourceLocation registry, CommandSource source, boolean report)
    	{
    		TypesManager manager = TypesManager.get(source.getWorld());
    		for(EnumCreatureType type : manager.getMobTypes(registry))
    			manager.removeFromEntity(registry, type, false);
    		manager.markDirty();
    		if(report)
    			source.sendFeedback(new TranslationTextComponent(translationSlug+"clear.success", registry.toString()), true);
    		return 15;
    	}
	}
	
	public static class VariantTest
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("test")
    				.then(newLiteral(ENTITY)
    					.then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(newLiteral("is").then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantTest.testEntityId(EntitySummonArgument.getEntityId(source, ENTITY), source.getArgument(TYPE, EnumCreatureType.class), source.getSource()); })))))
					.then(newLiteral(MOB)
    					.then(newArgument(MOB, EntityArgument.entity()).then(newLiteral("is").then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantTest.testEntity(EntityArgument.getEntity(source, MOB), source.getArgument(TYPE, EnumCreatureType.class), source.getSource()); })))));
    	}
    	
    	public static int testEntityId(ResourceLocation entityId, EnumCreatureType type, CommandSource source)
    	{
    		TypesManager manager = TypesManager.get(source.getWorld());
    		return testForType(manager.getMobTypes(entityId), type, source);
    	}
    	
    	public static int testEntity(Entity entity, EnumCreatureType type, CommandSource source)
    	{
    		TypesManager manager = TypesManager.get(source.getWorld());
    		return testForType(manager.getMobTypes(entity), type, source);
    	}
    	
    	private static int testForType(List<EnumCreatureType> types, EnumCreatureType type, CommandSource source)
    	{
    		if(types.contains(type))
    		{
    			source.sendFeedback(new TranslationTextComponent(translationSlug+"test.success", type.getTranslated()), true);
    			return 15;
    		}
    		else
    		{
    			source.sendFeedback(makeErrorMessage(translationSlug+"test.failed", type.getTranslated()), true);
    			return 0;
    		}
    	}
	}
	
	public static class VariantSet
	{
		private static final String TYPE2 = "type2";
		private static final String TYPE3 = "type3";
		private static final String TYPE4 = "type4";
		private static final String TYPE5 = "type5";
		
		public static LiteralArgumentBuilder<CommandSource> build()
		{
			return newLiteral("set")
    				.then(newLiteral(ENTITY)
    					.then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(newLiteral("to")
    						.then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUPERTYPE_SUGGEST)
								.executes((source) -> { return VariantSet.set(EntitySummonArgument.getEntityId(source, ENTITY), null, source.getSource(), source.getArgument(TYPE, EnumCreatureType.class)); })
								.then(newArgument(TYPE2, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
									.executes((source) -> { return VariantSet.set(EntitySummonArgument.getEntityId(source, ENTITY), null, source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class)); })
									.then(newArgument(TYPE3, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
										.executes((source) -> { return VariantSet.set(EntitySummonArgument.getEntityId(source, ENTITY), null, source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class), source.getArgument(TYPE3, EnumCreatureType.class)); })
										.then(newArgument(TYPE4, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
											.executes((source) -> { return VariantSet.set(EntitySummonArgument.getEntityId(source, ENTITY), null, source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class), source.getArgument(TYPE3, EnumCreatureType.class), source.getArgument(TYPE4, EnumCreatureType.class)); })
											.then(newArgument(TYPE5, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
												.executes((source) -> { return VariantSet.set(EntitySummonArgument.getEntityId(source, ENTITY), null, source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class), source.getArgument(TYPE3, EnumCreatureType.class), source.getArgument(TYPE4, EnumCreatureType.class), source.getArgument(TYPE5, EnumCreatureType.class)); })))))))))
    					.then(newLiteral(MOB).then(newArgument(MOB, EntityArgument.entity()).then(newLiteral("to")
    						.then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUPERTYPE_SUGGEST)
								.executes((source) -> { return VariantSet.set(null, EntityArgument.getEntity(source, MOB), source.getSource(), source.getArgument(TYPE, EnumCreatureType.class)); })
								.then(newArgument(TYPE2, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
									.executes((source) -> { return VariantSet.set(null, EntityArgument.getEntity(source, MOB), source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class)); })
									.then(newArgument(TYPE3, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
										.executes((source) -> { return VariantSet.set(null, EntityArgument.getEntity(source, MOB), source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class), source.getArgument(TYPE3, EnumCreatureType.class)); })
										.then(newArgument(TYPE4, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
											.executes((source) -> { return VariantSet.set(null, EntityArgument.getEntity(source, MOB), source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class), source.getArgument(TYPE3, EnumCreatureType.class), source.getArgument(TYPE4, EnumCreatureType.class)); })
											.then(newArgument(TYPE5, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
												.executes((source) -> { return VariantSet.set(null, EntityArgument.getEntity(source, MOB), source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class), source.getArgument(TYPE3, EnumCreatureType.class), source.getArgument(TYPE4, EnumCreatureType.class), source.getArgument(TYPE5, EnumCreatureType.class)); })))))))));
		}
		
		private static int set(ResourceLocation mobRegistry, Entity targetEntity, CommandSource source, EnumCreatureType... types)
		{
			if(mobRegistry != null)
				return setTypeMob(mobRegistry, source, types);
			else if(targetEntity != null && targetEntity instanceof LivingEntity)
				return setTypeEntity(targetEntity, source, types);
			source.sendFeedback(new TranslationTextComponent(translationSlug+"set.failed"), true);
			return 0;
		}
		
		private static int setTypeMob(ResourceLocation registry, CommandSource source, EnumCreatureType... types)
		{
			VariantClear.clearFromEntity(registry, source, false);
			for(EnumCreatureType type : types)
				VariantAdd.addToEntity(registry, type, source, false);
			
			TypesManager.get(source.getWorld()).markDirty();
			source.sendFeedback(new TranslationTextComponent(translationSlug+"set.success", registry.toString(), EnumCreatureType.typesToHeader(TypesManager.get(source.getWorld()).getMobTypes(registry))), true);
			return 15;
		}
		
		private static int setTypeEntity(Entity entity, CommandSource source, EnumCreatureType... types)
		{
			VariantClear.clearFromMob(entity, source, false);
			for(EnumCreatureType type : types)
				VariantAdd.addToMob(entity, type, source, false);
			
			TypesManager.get(source.getWorld()).markDirty();
			source.sendFeedback(new TranslationTextComponent(translationSlug+"set.success", entity.getName(), EnumCreatureType.typesToHeader(TypesManager.get(source.getWorld()).getMobTypes(entity))), true);
			return 15;
		}
	}
	
	public static class VariantReset
	{
		public static LiteralArgumentBuilder<CommandSource> build()
		{
			return newLiteral("reset")
					.executes((source) -> { return reset(true, true, source.getSource()); })
					.then(newLiteral("mobs")
						.executes((source) -> { return reset(true, false, source.getSource()); }))
					.then(newLiteral("players")
						.executes((source) -> { return reset(false, true, source.getSource()); }));
		}
		
		private static int reset(boolean mobs, boolean players, CommandSource source)
		{
			TypesManager manager = TypesManager.get(source.getWorld());
			if(mobs)
			{
				manager.resetMobs();
				if(!players)
				{
					source.sendFeedback(new TranslationTextComponent(translationSlug+"reset.mobs.success"), true);
					return 15;
				}
			}
			
			if(players)
			{
				manager.resetPlayers();
				if(!mobs)
				{
					source.sendFeedback(new TranslationTextComponent(translationSlug+"reset.players.success"), true);
					return 15;
				}
			}
			
			source.sendFeedback(new TranslationTextComponent(translationSlug+"reset.success"), true);
			return 15;
		}
	}
}
