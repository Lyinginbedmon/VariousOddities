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

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.EntitySummonArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class CommandTypes extends CommandBase
{
 	public static final SuggestionProvider<CommandSourceStack> SUPERTYPE_SUGGEST = SuggestionProviders.register(new ResourceLocation("creature_supertypes"), (context, builder) -> {
 		return SharedSuggestionProvider.suggest(EnumCreatureType.getSupertypeNames(), builder);
 		});
 	public static final SuggestionProvider<CommandSourceStack> SUBTYPE_SUGGEST = SuggestionProviders.register(new ResourceLocation("creature_subtypes"), (context, builder) -> {
 		return SharedSuggestionProvider.suggest(EnumCreatureType.getSubtypeNames(), builder);
 		});
 	public static final SuggestionProvider<CommandSourceStack> TYPE_SUGGEST = SuggestionProviders.register(new ResourceLocation("creature_types"), (context, builder) -> {
	 	return SharedSuggestionProvider.suggest(EnumCreatureType.getTypeNames(), builder);
	 	});
 	
	private static final String TYPE = "type";
	private static final String MOB = "target";
	private static final String ENTITY = "entity";
	
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".types.";
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("types").requires((source) -> { return source.hasPermission(2); } )
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
		private static final SimpleCommandExceptionType LIST_FAILED_EXCEPTION = new SimpleCommandExceptionType(Component.translatable(translationSlug+"list.failed"));
		
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("list")
    				.then(Commands.literal("all")
    					.executes(VariantList::listAll))
    				.then(Commands.literal(TYPE)
    					.then(Commands.argument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantList.listOfType(source.getArgument(TYPE, EnumCreatureType.class), source.getSource()); })))
    				.then(Commands.literal(ENTITY)
    					.then(Commands.argument(ENTITY, EntitySummonArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
    						.executes((source) -> { return VariantList.listEntityId(EntitySummonArgument.getSummonableEntity(source, ENTITY), source.getSource()); })))
					.then(Commands.literal(MOB)
    					.then(Commands.argument(MOB, EntityArgument.entity())
    						.executes((source) -> { return VariantList.listEntity(EntityArgument.getEntity(source, MOB), source.getSource()); })));
    	}
    	
    	public static int listAll(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    	{
    		CommandSourceStack source = context.getSource();
    		
    		EnumSet<EnumCreatureType> supertypes = EnumCreatureType.SUPERTYPES;
    		EnumSet<EnumCreatureType> subtypes = EnumCreatureType.SUBTYPES;
    		
    		source.sendSuccess(Component.translatable(translationSlug+"list.all", supertypes.size(), subtypes.size()), true);
    		if(!supertypes.isEmpty())
    		{
    			source.sendSuccess(Component.translatable(translationSlug+"list.all.supertypes").withStyle((style) -> { return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable(translationSlug+"list.all.supertypes.definition"))); }), false);
    			for(EnumCreatureType type : supertypes)
    				source.sendSuccess(Component.literal(" -").append(type.getTranslated(true).withStyle((style) -> { return style.applyFormat(ChatFormatting.DARK_AQUA).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.valueOf("/types list type "+type.name().toLowerCase()))); })), false);
    		}
    		
    		if(!subtypes.isEmpty())
    		{
    			source.sendSuccess(Component.translatable(translationSlug+"list.all.subtypes").withStyle((style) -> { return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable(translationSlug+"list.all.subtypes.definition"))); }), false);
    			for(EnumCreatureType type : subtypes)
    				source.sendSuccess(Component.literal(" -").append(type.getTranslated(true).withStyle((style) -> { return style.applyFormat(ChatFormatting.DARK_AQUA).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.valueOf("/types list type "+type.name().toLowerCase()))); })), false);
    		}
    		
    		if(supertypes.isEmpty() && subtypes.isEmpty())
    			throw LIST_FAILED_EXCEPTION.create();
    		
    		return 15;
    	}
    	
    	public static int listOfType(EnumCreatureType type, final CommandSourceStack source) throws CommandSyntaxException
    	{
    		TypesManager manager = TypesManager.get(source.getLevel());
    		List<ResourceLocation> mobs = manager.mobsOfType(type);
    		
    		Component typeName = type.getTranslated(false);
    		if(mobs.isEmpty())
    			throw LIST_FAILED_EXCEPTION.create();
    		else
    		{
    			if(!mobs.isEmpty())
    			{
					source.sendSuccess(Component.translatable(translationSlug+"list.success.mobs", mobs.size(), typeName), true);
					boolean grey = true;
		    		for(ResourceLocation string : mobs)
		    		{
		    			MutableComponent text = Component.literal("-").append(Component.literal(string.toString()));
		    			if(grey)
		    				text.withStyle((style) -> { return style.applyFormat(ChatFormatting.GRAY); });
		    			source.sendSuccess(text, false);
		    			grey = !grey;
		    		}
    			}
    		}
    		
    		return Math.min(mobs.size(), 15);
    	}
    	
    	public static int listEntityId(ResourceLocation entityId, CommandSourceStack source) throws CommandSyntaxException
    	{
    		TypesManager manager = TypesManager.get(source.getLevel());
    		return listTypes(manager.getMobTypes(entityId), Component.literal(entityId.toString()), source, false, true);
    	}
    	
    	public static int listEntity(Entity entity, CommandSourceStack source) throws CommandSyntaxException
    	{
    		if(entity instanceof LivingEntity)
        		return listTypes(EnumCreatureType.getCreatureTypes((LivingEntity)entity), entity.getDisplayName(), source, false, true);
    		else
    			throw LIST_FAILED_EXCEPTION.create();
    	}
    	
    	private static int listTypes(List<EnumCreatureType> types, Component identifier, CommandSourceStack source, boolean grey, boolean log) throws CommandSyntaxException
    	{
    		if(types.isEmpty())
    			throw LIST_FAILED_EXCEPTION.create();
    		else
    		{
    			MutableComponent text = Component.translatable(translationSlug+"list.success", identifier, new Types(types).toHeader());
    			if(grey)
    				text.withStyle((style) -> { return style.applyFormat(ChatFormatting.GRAY); });
				source.sendSuccess(text, log);
    		}
    		return types.size();
    	}
	}
	
	private static class VariantAdd
	{
		private static final SimpleCommandExceptionType ADD_FAILED_EXCEPTION = new SimpleCommandExceptionType(Component.translatable(translationSlug+"add.failed"));
		
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("add")
    				.then(Commands.literal(ENTITY)
    					.then(Commands.argument(ENTITY, EntitySummonArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(Commands.argument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantAdd.addTypeRegistry(EntitySummonArgument.getSummonableEntity(source, ENTITY), source.getArgument(TYPE, EnumCreatureType.class), source.getSource(), true); }))))
					.then(Commands.literal(MOB)
    					.then(Commands.argument(MOB, EntityArgument.entity()).then(Commands.argument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantAdd.addTypeEntity(EntityArgument.getEntity(source, MOB), source.getArgument(TYPE, EnumCreatureType.class), source.getSource(), true); }))));
    	}
    	
    	public static int addTypeEntity(Entity entity, EnumCreatureType type, CommandSourceStack source, boolean report) throws CommandSyntaxException
    	{
    		if(entity instanceof LivingEntity)
    		{
    			LivingData data = LivingData.forEntity((LivingEntity)entity);
    			data.addCustomType(type);
    			
				if(report)
				{
	    			source.sendSuccess(Component.translatable(translationSlug+"add.success", type.getTranslated(false), entity.getDisplayName()), true);
					source.sendSuccess(Component.translatable(translationSlug+"list.success", entity.getDisplayName(), EnumCreatureType.getTypes((LivingEntity)entity).toHeader()), false);
				}
    			
    			return 15;
    		}
    		
    		throw ADD_FAILED_EXCEPTION.create();
    	}
    	
    	public static int addTypeRegistry(ResourceLocation registry, EnumCreatureType type, CommandSourceStack source, boolean report) throws CommandSyntaxException
    	{
    		TypesManager manager = TypesManager.get(source.getLevel());
			if(type.getHandler().canApplyTo(manager.getMobTypes(registry)))
			{
				manager.addToEntity(registry, type, report);
				
				if(report)
				{
					source.sendSuccess(Component.translatable(translationSlug+"add.success", type.getTranslated(false), registry.toString()), true);
					source.sendSuccess(Component.translatable(translationSlug+"list.success", registry.toString(), new Types(manager.getMobTypes(registry)).toHeader()), true);
				}
			}
			else if(report)
				throw ADD_FAILED_EXCEPTION.create();
			
    		return 15;
    	}
	}
	
	private static class VariantRemove
	{
		private static final SimpleCommandExceptionType REMOVE_FAILED_EXCEPTION = new SimpleCommandExceptionType(Component.translatable(translationSlug+"remove.failed"));
		
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("remove")
    				.then(Commands.literal(ENTITY)
    					.then(Commands.argument(ENTITY, EntitySummonArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(Commands.argument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantRemove.removeTypeRegistry(EntitySummonArgument.getSummonableEntity(source, ENTITY), source.getArgument(TYPE, EnumCreatureType.class), source.getSource()); }))))
					.then(Commands.literal(MOB)
    					.then(Commands.argument(MOB, EntityArgument.entity()).then(Commands.argument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantRemove.removeTypeEntity(EntityArgument.getEntity(source, MOB), source.getArgument(TYPE, EnumCreatureType.class), source.getSource()); }))));
    	}
    	
    	public static int removeTypeEntity(Entity entity, EnumCreatureType type, CommandSourceStack source) throws CommandSyntaxException
    	{
    		if(entity instanceof LivingEntity)
    		{
    			LivingEntity living = (LivingEntity)entity;
    			LivingData data = LivingData.forEntity(living);
    			data.removeCustomType(type);
    			
    			source.sendSuccess(Component.translatable(translationSlug+"remove.success", type.getTranslated(false), living.getDisplayName()), true);
				source.sendSuccess(Component.translatable(translationSlug+"list.success", entity.getDisplayName(), EnumCreatureType.getTypes(living).toHeader()), false);
    			return 15;
    		}
    		
    		throw REMOVE_FAILED_EXCEPTION.create();
    	}
    	
    	public static int removeTypeRegistry(ResourceLocation registry, EnumCreatureType type, CommandSourceStack source) throws CommandSyntaxException
    	{
    		TypesManager manager = TypesManager.get(source.getLevel());
			if(manager.isMobOfType(registry, type))
				manager.removeFromEntity(registry, type, true);
			else
				throw REMOVE_FAILED_EXCEPTION.create();
			source.sendSuccess(Component.translatable(translationSlug+"remove.success", type.getTranslated(false), registry.toString()), true);
			source.sendSuccess(Component.translatable(translationSlug+"list.success", registry.toString(), new Types(manager.getMobTypes(registry)).toHeader()), true);
    		return 15;
    	}
	}
	
	private static class VariantClear
	{
		private static final SimpleCommandExceptionType CLEAR_FAILED_EXCEPTION = new SimpleCommandExceptionType(Component.translatable(translationSlug+"clear.failed"));
		
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("clear")
    				.then(Commands.literal(ENTITY)
    					.then(Commands.argument(ENTITY, EntitySummonArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
    						.executes((source) -> { return VariantClear.clearTypeRegistry(EntitySummonArgument.getSummonableEntity(source, ENTITY), source.getSource(), true); })))
					.then(Commands.literal(MOB)
    					.then(Commands.argument(MOB, EntityArgument.entity())
    						.executes((source) -> { return VariantClear.clearTypeEntity(EntityArgument.getEntity(source, MOB), source.getSource(), true); })));
    	}
    	
    	public static int clearTypeEntity(Entity entity, CommandSourceStack source, boolean report) throws CommandSyntaxException
    	{
    		if(entity instanceof LivingEntity)
    		{
    			LivingData data = LivingData.forEntity((LivingEntity)entity);
    			data.clearCustomTypes();
    			if(report)
    				source.sendSuccess(Component.translatable(translationSlug+"clear.success", entity.getDisplayName()), true);
    			return 15;
    		}
    		
    		if(report)
    			throw CLEAR_FAILED_EXCEPTION.create();
    		
    		return 0;
    	}
    	
    	public static int clearTypeRegistry(ResourceLocation registry, CommandSourceStack source, boolean report)
    	{
    		TypesManager manager = TypesManager.get(source.getLevel());
    		for(EnumCreatureType type : manager.getMobTypes(registry))
    			manager.removeFromEntity(registry, type, false);
    		manager.markDirty();
    		if(report)
    			source.sendSuccess(Component.translatable(translationSlug+"clear.success", registry.toString()), true);
    		return 15;
    	}
	}
	
	public static class VariantTest
	{
		private static final SimpleCommandExceptionType TEST_FAILED_EXCEPTION = new SimpleCommandExceptionType(Component.translatable(translationSlug+"test.failed"));
		
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("test")
    				.then(Commands.literal(ENTITY)
    					.then(Commands.argument(ENTITY, EntitySummonArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(Commands.literal("is").then(Commands.argument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantTest.testTypeRegistry(EntitySummonArgument.getSummonableEntity(source, ENTITY), source.getArgument(TYPE, EnumCreatureType.class), source.getSource()); })))))
					.then(Commands.literal(MOB)
    					.then(Commands.argument(MOB, EntityArgument.entity()).then(Commands.literal("is").then(Commands.argument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(TYPE_SUGGEST)
    						.executes((source) -> { return VariantTest.testTypeEntity(EntityArgument.getEntity(source, MOB), source.getArgument(TYPE, EnumCreatureType.class), source.getSource()); })))));
    	}
    	
    	public static int testTypeRegistry(ResourceLocation entityId, EnumCreatureType type, CommandSourceStack source) throws CommandSyntaxException
    	{
    		TypesManager manager = TypesManager.get(source.getLevel());
    		return testForType(manager.getMobTypes(entityId), type, source);
    	}
    	
    	public static int testTypeEntity(Entity entity, EnumCreatureType type, CommandSourceStack source) throws CommandSyntaxException
    	{
    		return testForType(EnumCreatureType.getCreatureTypes(entity instanceof LivingEntity ? (LivingEntity)entity : null), type, source);
    	}
    	
    	private static int testForType(List<EnumCreatureType> types, EnumCreatureType type, CommandSourceStack source) throws CommandSyntaxException
    	{
    		if(types.contains(type))
    		{
    			source.sendSuccess(Component.translatable(translationSlug+"test.success", type.getTranslated(false)), true);
    			return 15;
    		}
    		else
    			throw TEST_FAILED_EXCEPTION.create();
    	}
	}
	
	public static class VariantSet
	{
		private static final SimpleCommandExceptionType SET_FAILED_EXCEPTION = new SimpleCommandExceptionType(Component.translatable(translationSlug+"set.failed"));
		
		private static final String TYPE2 = "type2";
		private static final String TYPE3 = "type3";
		private static final String TYPE4 = "type4";
		private static final String TYPE5 = "type5";
		
		public static LiteralArgumentBuilder<CommandSourceStack> build()
		{
			return Commands.literal("set")
    				.then(Commands.literal(ENTITY)
    					.then(Commands.argument(ENTITY, EntitySummonArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(Commands.literal("to")
    						.then(Commands.argument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUPERTYPE_SUGGEST)
								.executes((source) -> { return VariantSet.set(EntitySummonArgument.getSummonableEntity(source, ENTITY), null, source.getSource(), source.getArgument(TYPE, EnumCreatureType.class)); })
								.then(Commands.argument(TYPE2, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
									.executes((source) -> { return VariantSet.set(EntitySummonArgument.getSummonableEntity(source, ENTITY), null, source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class)); })
									.then(Commands.argument(TYPE3, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
										.executes((source) -> { return VariantSet.set(EntitySummonArgument.getSummonableEntity(source, ENTITY), null, source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class), source.getArgument(TYPE3, EnumCreatureType.class)); })
										.then(Commands.argument(TYPE4, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
											.executes((source) -> { return VariantSet.set(EntitySummonArgument.getSummonableEntity(source, ENTITY), null, source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class), source.getArgument(TYPE3, EnumCreatureType.class), source.getArgument(TYPE4, EnumCreatureType.class)); })
											.then(Commands.argument(TYPE5, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
												.executes((source) -> { return VariantSet.set(EntitySummonArgument.getSummonableEntity(source, ENTITY), null, source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class), source.getArgument(TYPE3, EnumCreatureType.class), source.getArgument(TYPE4, EnumCreatureType.class), source.getArgument(TYPE5, EnumCreatureType.class)); })))))))))
    					.then(Commands.literal(MOB).then(Commands.argument(MOB, EntityArgument.entity()).then(Commands.literal("to")
    						.then(Commands.argument(TYPE, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUPERTYPE_SUGGEST)
								.executes((source) -> { return VariantSet.set(null, EntityArgument.getEntity(source, MOB), source.getSource(), source.getArgument(TYPE, EnumCreatureType.class)); })
								.then(Commands.argument(TYPE2, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
									.executes((source) -> { return VariantSet.set(null, EntityArgument.getEntity(source, MOB), source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class)); })
									.then(Commands.argument(TYPE3, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
										.executes((source) -> { return VariantSet.set(null, EntityArgument.getEntity(source, MOB), source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class), source.getArgument(TYPE3, EnumCreatureType.class)); })
										.then(Commands.argument(TYPE4, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
											.executes((source) -> { return VariantSet.set(null, EntityArgument.getEntity(source, MOB), source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class), source.getArgument(TYPE3, EnumCreatureType.class), source.getArgument(TYPE4, EnumCreatureType.class)); })
											.then(Commands.argument(TYPE5, EnumArgumentChecked.enumArgument(EnumCreatureType.class)).suggests(SUBTYPE_SUGGEST)
												.executes((source) -> { return VariantSet.set(null, EntityArgument.getEntity(source, MOB), source.getSource(), source.getArgument(TYPE, EnumCreatureType.class), source.getArgument(TYPE2, EnumCreatureType.class), source.getArgument(TYPE3, EnumCreatureType.class), source.getArgument(TYPE4, EnumCreatureType.class), source.getArgument(TYPE5, EnumCreatureType.class)); })))))))));
		}
		
		private static int set(ResourceLocation mobRegistry, Entity targetEntity, CommandSourceStack source, EnumCreatureType... types) throws CommandSyntaxException
		{
			if(mobRegistry != null)
				return setTypeRegistry(mobRegistry, source, types);
			else if(targetEntity != null && targetEntity instanceof LivingEntity)
				return setTypeEntity(targetEntity, source, types);
			
			throw SET_FAILED_EXCEPTION.create();
		}
		
		private static int setTypeRegistry(ResourceLocation registry, CommandSourceStack source, EnumCreatureType... types) throws CommandSyntaxException
		{
			VariantClear.clearTypeRegistry(registry, source, false);
			for(EnumCreatureType type : types)
				VariantAdd.addTypeRegistry(registry, type, source, false);
			
			TypesManager.get(source.getLevel()).markDirty();
			source.sendSuccess(Component.translatable(translationSlug+"set.success", registry.toString(), new Types(TypesManager.get(source.getLevel()).getMobTypes(registry)).toHeader()), true);
			return 15;
		}
		
		private static int setTypeEntity(Entity entity, CommandSourceStack source, EnumCreatureType... types) throws CommandSyntaxException
		{
			VariantClear.clearTypeEntity(entity, source, false);
			for(EnumCreatureType type : types)
				VariantAdd.addTypeEntity(entity, type, source, false);
			
			source.sendSuccess(Component.translatable(translationSlug+"set.success", entity.getDisplayName(), EnumCreatureType.getTypes((LivingEntity)entity).toHeader()), true);
			return 15;
		}
	}
	
	public static class VariantReset
	{
		public static LiteralArgumentBuilder<CommandSourceStack> build()
		{
			return Commands.literal("reset")
					.executes((source) -> { return reset(source.getSource()); });
		}
		
		private static int reset(CommandSourceStack source)
		{
			TypesManager manager = TypesManager.get(source.getLevel());
			manager.resetMobs();
			source.sendSuccess(Component.translatable(translationSlug+"reset.mobs.success"), true);
			return 15;
		}
	}
	
	private static class VariantOrigin
	{
		private static final SimpleCommandExceptionType ORIGIN_FAILED_EXCEPTION = new SimpleCommandExceptionType(Component.translatable(translationSlug+"origin.failed"));
		private static final SimpleCommandExceptionType ORIGIN_FAILED_SET_EXCEPTION = new SimpleCommandExceptionType(Component.translatable(translationSlug+"origin.set.failed"));
		
		private static final String DEST = "dimension";
		
		public static LiteralArgumentBuilder<CommandSourceStack> build()
		{
			return Commands.literal("origin")
					.then(Commands.literal(MOB).then(Commands.argument(MOB, EntityArgument.entity())
						.executes((source) -> { return getHomeDimension(EntityArgument.getEntity(source, MOB), source.getSource()); })
						.then(Commands.argument(DEST, StringArgumentType.word())
							.executes((source) -> { return setHomeDimension(EntityArgument.getEntity(source, MOB), StringArgumentType.getString(source, DEST), source.getSource()); }))));
		}
		
		private static int getHomeDimension(Entity entityIn, CommandSourceStack source) throws CommandSyntaxException
		{
			if(entityIn instanceof LivingEntity)
			{
				LivingData data = LivingData.forEntity((LivingEntity)entityIn);
				if(data != null && data.getHomeDimension() != null)
				{
					source.sendSuccess(Component.translatable(translationSlug+"origin.success", entityIn.getDisplayName(), data.getHomeDimension()), true);
					return 15;
				}
			}
			throw ORIGIN_FAILED_EXCEPTION.create();
		}
		
		private static int setHomeDimension(Entity entityIn, String destination, CommandSourceStack source) throws CommandSyntaxException
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
					source.sendSuccess(Component.translatable(translationSlug+"origin.set.success", entityIn.getDisplayName(), data.getHomeDimension()), true);
				}
			}
			
			throw ORIGIN_FAILED_SET_EXCEPTION.create();
		}
	}
}
