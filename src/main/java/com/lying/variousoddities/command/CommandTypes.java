package com.lying.variousoddities.command;

import java.util.EnumSet;
import java.util.List;

import com.lying.variousoddities.api.EnumArgumentChecked;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.Types;
import com.lying.variousoddities.world.savedata.TypesManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySummonArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
			.then(VariantTest.build())
			.then(VariantOrigin.build());
		
		dispatcher.register(literal);
	}
	
	private static class VariantList
	{
		private static final SimpleCommandExceptionType LIST_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent(translationSlug+"list.failed"));
		
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("list")
    				.then(newLiteral("all")
    					.executes(VariantList::listAll))
    				.then(newLiteral(TYPE)
    					.then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantList.listOfType(source.getArgument(TYPE, EnumCreatureType.class), source.getSource()); })))
    				.then(newLiteral(ENTITY)
    					.then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
    						.executes((source) -> { return VariantList.listEntityId(EntitySummonArgument.getEntityId(source, ENTITY), source.getSource()); })))
					.then(newLiteral(MOB)
    					.then(newArgument(MOB, EntityArgument.entity())
    						.executes((source) -> { return VariantList.listEntity(EntityArgument.getEntity(source, MOB), source.getSource()); })));
    	}
    	
    	public static int listAll(final CommandContext<CommandSource> context) throws CommandSyntaxException
    	{
    		CommandSource source = context.getSource();
    		
    		EnumSet<EnumCreatureType> supertypes = EnumCreatureType.SUPERTYPES;
    		EnumSet<EnumCreatureType> subtypes = EnumCreatureType.SUBTYPES;
    		
    		source.sendFeedback(new TranslationTextComponent(translationSlug+"list.all", supertypes.size(), subtypes.size()), true);
    		if(!supertypes.isEmpty())
    		{
    			source.sendFeedback(new TranslationTextComponent(translationSlug+"list.all.supertypes").modifyStyle((style) -> { return style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(translationSlug+"list.all.supertypes.definition"))); }), false);
    			for(EnumCreatureType type : supertypes)
    				source.sendFeedback(new StringTextComponent(" -").append(type.getTranslated().modifyStyle((style) -> { return style.setFormatting(TextFormatting.DARK_AQUA).setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.valueOf("/types list type "+type.name().toLowerCase()))); })), false);
    		}
    		
    		if(!subtypes.isEmpty())
    		{
    			source.sendFeedback(new TranslationTextComponent(translationSlug+"list.all.subtypes").modifyStyle((style) -> { return style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(translationSlug+"list.all.subtypes.definition"))); }), false);
    			for(EnumCreatureType type : subtypes)
    				source.sendFeedback(new StringTextComponent(" -").append(type.getTranslated().modifyStyle((style) -> { return style.setFormatting(TextFormatting.DARK_AQUA).setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.valueOf("/types list type "+type.name().toLowerCase()))); })), false);
    		}
    		
    		if(supertypes.isEmpty() && subtypes.isEmpty())
    			throw LIST_FAILED_EXCEPTION.create();
    		
    		return 15;
    	}
    	
    	public static int listOfType(EnumCreatureType type, final CommandSource source) throws CommandSyntaxException
    	{
    		TypesManager manager = TypesManager.get(source.getWorld());
    		List<ResourceLocation> mobs = manager.mobsOfType(type);
    		
    		ITextComponent typeName = type.getTranslated();
    		if(mobs.isEmpty())
    			throw LIST_FAILED_EXCEPTION.create();
    		else
    		{
    			if(!mobs.isEmpty())
    			{
					source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success.mobs", mobs.size(), typeName), true);
					boolean grey = true;
		    		for(ResourceLocation string : mobs)
		    		{
		    			IFormattableTextComponent text = new StringTextComponent("-").append(new StringTextComponent(string.toString()));
		    			if(grey)
		    				text.modifyStyle((style) -> { return style.applyFormatting(TextFormatting.GRAY); });
		    			source.sendFeedback(text, false);
		    			grey = !grey;
		    		}
    			}
    		}
    		
    		return Math.min(mobs.size(), 15);
    	}
    	
    	public static int listEntityId(ResourceLocation entityId, CommandSource source) throws CommandSyntaxException
    	{
    		TypesManager manager = TypesManager.get(source.getWorld());
    		return listTypes(manager.getMobTypes(entityId), new StringTextComponent(entityId.toString()), source, false, true);
    	}
    	
    	public static int listEntity(Entity entity, CommandSource source) throws CommandSyntaxException
    	{
    		TypesManager manager = TypesManager.get(source.getWorld());
    		return listTypes(manager.getMobTypes(entity), entity.getDisplayName(), source, false, true);
    	}
    	
    	private static int listTypes(List<EnumCreatureType> types, ITextComponent identifier, CommandSource source, boolean grey, boolean log) throws CommandSyntaxException
    	{
    		if(types.isEmpty())
    			throw LIST_FAILED_EXCEPTION.create();
    		else
    		{
    			TranslationTextComponent text = new TranslationTextComponent(translationSlug+"list.success", identifier, new Types(types).toHeader());
    			if(grey)
    				text.modifyStyle((style) -> { return style.applyFormatting(TextFormatting.GRAY); });
				source.sendFeedback(text, log);
    		}
    		return types.size();
    	}
	}
	
	private static class VariantAdd
	{
		private static final SimpleCommandExceptionType ADD_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent(translationSlug+"add.failed"));
		
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("add")
    				.then(newLiteral(ENTITY)
    					.then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantAdd.addTypeRegistry(EntitySummonArgument.getEntityId(source, ENTITY), source.getArgument(TYPE, EnumCreatureType.class), source.getSource(), true); }))))
					.then(newLiteral(MOB)
    					.then(newArgument(MOB, EntityArgument.entity()).then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantAdd.addTypeEntity(EntityArgument.getEntity(source, MOB), source.getArgument(TYPE, EnumCreatureType.class), source.getSource(), true); }))));
    	}
    	
    	public static int addTypeEntity(Entity entity, EnumCreatureType type, CommandSource source, boolean report) throws CommandSyntaxException
    	{
    		if(entity instanceof LivingEntity)
    		{
    			LivingData data = LivingData.forEntity((LivingEntity)entity);
    			data.addCustomType(type);
    			
				if(report)
				{
	    			source.sendFeedback(new TranslationTextComponent(translationSlug+"add.success", type.getTranslated(), entity.getDisplayName()), true);
					source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success", entity.getDisplayName(), EnumCreatureType.getTypes((LivingEntity)entity).toHeader()), false);
				}
    			
    			return 15;
    		}
    		
    		throw ADD_FAILED_EXCEPTION.create();
    	}
    	
    	public static int addTypeRegistry(ResourceLocation registry, EnumCreatureType type, CommandSource source, boolean report) throws CommandSyntaxException
    	{
    		TypesManager manager = TypesManager.get(source.getWorld());
			if(type.getHandler().canApplyTo(manager.getMobTypes(registry)))
			{
				manager.addToEntity(registry, type, report);
				
				if(report)
				{
					source.sendFeedback(new TranslationTextComponent(translationSlug+"add.success", type.getTranslated(), registry.toString()), true);
					source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success", registry.toString(), new Types(manager.getMobTypes(registry)).toHeader()), true);
				}
			}
			else if(report)
				throw ADD_FAILED_EXCEPTION.create();
			
    		return 15;
    	}
	}
	
	private static class VariantRemove
	{
		private static final SimpleCommandExceptionType REMOVE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent(translationSlug+"remove.failed"));
		
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("remove")
    				.then(newLiteral(ENTITY)
    					.then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantRemove.removeTypeRegistry(EntitySummonArgument.getEntityId(source, ENTITY), source.getArgument(TYPE, EnumCreatureType.class), source.getSource()); }))))
					.then(newLiteral(MOB)
    					.then(newArgument(MOB, EntityArgument.entity()).then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantRemove.removeTypeEntity(EntityArgument.getEntity(source, MOB), source.getArgument(TYPE, EnumCreatureType.class), source.getSource()); }))));
    	}
    	
    	public static int removeTypeEntity(Entity entity, EnumCreatureType type, CommandSource source) throws CommandSyntaxException
    	{
    		if(entity instanceof LivingEntity)
    		{
    			LivingEntity living = (LivingEntity)entity;
    			LivingData data = LivingData.forEntity(living);
    			data.removeCustomType(type);
    			
    			source.sendFeedback(new TranslationTextComponent(translationSlug+"remove.success", type.getTranslated(), living.getDisplayName()), true);
				source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success", entity.getDisplayName(), EnumCreatureType.getTypes(living).toHeader()), false);
    			return 15;
    		}
    		
    		throw REMOVE_FAILED_EXCEPTION.create();
    	}
    	
    	public static int removeTypeRegistry(ResourceLocation registry, EnumCreatureType type, CommandSource source) throws CommandSyntaxException
    	{
    		TypesManager manager = TypesManager.get(source.getWorld());
			if(manager.isMobOfType(registry, type))
				manager.removeFromEntity(registry, type, true);
			else
				throw REMOVE_FAILED_EXCEPTION.create();
			source.sendFeedback(new TranslationTextComponent(translationSlug+"remove.success", type.getTranslated(), registry.toString()), true);
			source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success", registry.toString(), new Types(manager.getMobTypes(registry)).toHeader()), true);
    		return 15;
    	}
	}
	
	private static class VariantClear
	{
		private static final SimpleCommandExceptionType CLEAR_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent(translationSlug+"clear.failed"));
		
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("clear")
    				.then(newLiteral(ENTITY)
    					.then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
    						.executes((source) -> { return VariantClear.clearTypeRegistry(EntitySummonArgument.getEntityId(source, ENTITY), source.getSource(), true); })))
					.then(newLiteral(MOB)
    					.then(newArgument(MOB, EntityArgument.entity())
    						.executes((source) -> { return VariantClear.clearTypeEntity(EntityArgument.getEntity(source, MOB), source.getSource(), true); })));
    	}
    	
    	public static int clearTypeEntity(Entity entity, CommandSource source, boolean report) throws CommandSyntaxException
    	{
    		if(entity instanceof LivingEntity)
    		{
    			LivingData data = LivingData.forEntity((LivingEntity)entity);
    			data.clearCustomTypes();
    			if(report)
    				source.sendFeedback(new TranslationTextComponent(translationSlug+"clear.success", entity.getDisplayName()), true);
    			return 15;
    		}
    		
    		if(report)
    			throw CLEAR_FAILED_EXCEPTION.create();
    		
    		return 0;
    	}
    	
    	public static int clearTypeRegistry(ResourceLocation registry, CommandSource source, boolean report)
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
		private static final SimpleCommandExceptionType TEST_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent(translationSlug+"test.failed"));
		
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("test")
    				.then(newLiteral(ENTITY)
    					.then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(newLiteral("is").then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantTest.testTypeRegistry(EntitySummonArgument.getEntityId(source, ENTITY), source.getArgument(TYPE, EnumCreatureType.class), source.getSource()); })))))
					.then(newLiteral(MOB)
    					.then(newArgument(MOB, EntityArgument.entity()).then(newLiteral("is").then(newArgument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantTest.testTypeEntity(EntityArgument.getEntity(source, MOB), source.getArgument(TYPE, EnumCreatureType.class), source.getSource()); })))));
    	}
    	
    	public static int testTypeRegistry(ResourceLocation entityId, EnumCreatureType type, CommandSource source) throws CommandSyntaxException
    	{
    		TypesManager manager = TypesManager.get(source.getWorld());
    		return testForType(manager.getMobTypes(entityId), type, source);
    	}
    	
    	public static int testTypeEntity(Entity entity, EnumCreatureType type, CommandSource source) throws CommandSyntaxException
    	{
    		return testForType(EnumCreatureType.getCreatureTypes(entity instanceof LivingEntity ? (LivingEntity)entity : null), type, source);
    	}
    	
    	private static int testForType(List<EnumCreatureType> types, EnumCreatureType type, CommandSource source) throws CommandSyntaxException
    	{
    		if(types.contains(type))
    		{
    			source.sendFeedback(new TranslationTextComponent(translationSlug+"test.success", type.getTranslated()), true);
    			return 15;
    		}
    		else
    			throw TEST_FAILED_EXCEPTION.create();
    	}
	}
	
	public static class VariantSet
	{
		private static final SimpleCommandExceptionType SET_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent(translationSlug+"set.failed"));
		
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
		
		private static int set(ResourceLocation mobRegistry, Entity targetEntity, CommandSource source, EnumCreatureType... types) throws CommandSyntaxException
		{
			if(mobRegistry != null)
				return setTypeRegistry(mobRegistry, source, types);
			else if(targetEntity != null && targetEntity instanceof LivingEntity)
				return setTypeEntity(targetEntity, source, types);
			
			throw SET_FAILED_EXCEPTION.create();
		}
		
		private static int setTypeRegistry(ResourceLocation registry, CommandSource source, EnumCreatureType... types) throws CommandSyntaxException
		{
			VariantClear.clearTypeRegistry(registry, source, false);
			for(EnumCreatureType type : types)
				VariantAdd.addTypeRegistry(registry, type, source, false);
			
			TypesManager.get(source.getWorld()).markDirty();
			source.sendFeedback(new TranslationTextComponent(translationSlug+"set.success", registry.toString(), new Types(TypesManager.get(source.getWorld()).getMobTypes(registry)).toHeader()), true);
			return 15;
		}
		
		private static int setTypeEntity(Entity entity, CommandSource source, EnumCreatureType... types) throws CommandSyntaxException
		{
			VariantClear.clearTypeEntity(entity, source, false);
			for(EnumCreatureType type : types)
				VariantAdd.addTypeEntity(entity, type, source, false);
			
			source.sendFeedback(new TranslationTextComponent(translationSlug+"set.success", entity.getDisplayName(), EnumCreatureType.getTypes((LivingEntity)entity).toHeader()), true);
			return 15;
		}
	}
	
	public static class VariantReset
	{
		public static LiteralArgumentBuilder<CommandSource> build()
		{
			return newLiteral("reset")
					.executes((source) -> { return reset(source.getSource()); });
		}
		
		private static int reset(CommandSource source)
		{
			TypesManager manager = TypesManager.get(source.getWorld());
			manager.resetMobs();
			source.sendFeedback(new TranslationTextComponent(translationSlug+"reset.mobs.success"), true);
			return 15;
		}
	}
	
	private static class VariantOrigin
	{
		private static final SimpleCommandExceptionType ORIGIN_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent(translationSlug+"origin.failed"));
		private static final SimpleCommandExceptionType ORIGIN_FAILED_SET_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent(translationSlug+"origin.set.failed"));
		
		private static final String DEST = "dimension";
		
		public static LiteralArgumentBuilder<CommandSource> build()
		{
			return newLiteral("origin")
					.then(newLiteral(MOB).then(newArgument(MOB, EntityArgument.entity())
						.executes((source) -> { return getHomeDimension(EntityArgument.getEntity(source, MOB), source.getSource()); })
						.then(newArgument(DEST, StringArgumentType.word())
							.executes((source) -> { return setHomeDimension(EntityArgument.getEntity(source, MOB), StringArgumentType.getString(source, DEST), source.getSource()); }))));
		}
		
		private static int getHomeDimension(Entity entityIn, CommandSource source) throws CommandSyntaxException
		{
			if(entityIn instanceof LivingEntity)
			{
				LivingData data = LivingData.forEntity((LivingEntity)entityIn);
				if(data != null && data.getHomeDimension() != null)
				{
					source.sendFeedback(new TranslationTextComponent(translationSlug+"origin.success", entityIn.getDisplayName(), data.getHomeDimension()), true);
					return 15;
				}
			}
			throw ORIGIN_FAILED_EXCEPTION.create();
		}
		
		private static int setHomeDimension(Entity entityIn, String destination, CommandSource source) throws CommandSyntaxException
		{
			if(entityIn instanceof LivingEntity)
			{
				LivingData data = LivingData.forEntity((LivingEntity)entityIn);
				ResourceLocation dest = null;
				try
				{
					dest = new ResourceLocation(destination);
				}
				catch(Exception e){ }
				if(data != null && dest != null)
				{
					data.setHomeDimension(dest);
					source.sendFeedback(new TranslationTextComponent(translationSlug+"origin.set.success", entityIn.getDisplayName(), data.getHomeDimension()), true);
				}
			}
			
			throw ORIGIN_FAILED_SET_EXCEPTION.create();
		}
	}
}
