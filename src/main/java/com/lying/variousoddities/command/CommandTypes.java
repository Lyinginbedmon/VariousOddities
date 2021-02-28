package com.lying.variousoddities.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.types.CreatureTypes;
import com.lying.variousoddities.types.EnumCreatureType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySummonArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class CommandTypes extends CommandBase
{
	private static final String TYPE = "type";
	private static final String MOB = "target";
	private static final String ENTITY = "entity";
	
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".types.";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> literal = newLiteral("types")
			.then(VariantList.build())
			.then(VariantRemove.build())
			.then(VariantAdd.build())
			.then(VariantClear.build())
			.then(VariantSet.build())
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
    					.then(newArgument(TYPE, CreatureTypeArgument.type())
    						.executes(VariantList::listOfType)))
    				.then(newLiteral(ENTITY)
    					.then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
    						.executes((source) -> { return VariantList.listTypes(CreatureTypes.getMobTypes(EntitySummonArgument.getEntityId(source, ENTITY)), new StringTextComponent(EntitySummonArgument.getEntityId(source, ENTITY).toString()), source.getSource()); })))
					.then(newLiteral(MOB)
    					.then(newArgument(MOB, EntityArgument.entity())
    						.executes((source) -> { return VariantList.listTypes(CreatureTypes.getMobTypes(EntityArgument.getEntity(source, MOB)), EntityArgument.getEntity(source, MOB).getName(), source.getSource()); })));
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
    	
    	public static int listOfType(final CommandContext<CommandSource> source)
    	{
    		EnumCreatureType type = CreatureTypeArgument.getType(source, TYPE);
    		List<ResourceLocation> mobs = CreatureTypes.mobsOfType(type);
    		List<String> players = CreatureTypes.playersOfType(type);
    		
    		ITextComponent typeName = type.getTranslated();
    		if(mobs.isEmpty() && players.isEmpty())
    			source.getSource().sendFeedback(makeErrorMessage(translationSlug+"list.failed", typeName), true);
    		else
    		{
    			if(!mobs.isEmpty())
    			{
					source.getSource().sendFeedback(new TranslationTextComponent(translationSlug+"list.success.mobs", mobs.size(), typeName), true);
		    		for(ResourceLocation string : mobs)
		    			source.getSource().sendFeedback(new StringTextComponent("-").append(new StringTextComponent(string.toString())), false);
    			}
    			if(!players.isEmpty())
    			{
					source.getSource().sendFeedback(new TranslationTextComponent(translationSlug+"list.success.players", players.size(), typeName), true);
		    		for(String string : players)
		    			source.getSource().sendFeedback(new StringTextComponent("-").append(new StringTextComponent(string)), false);
    			}
    		}
    		
    		return mobs.size();
    	}
    	
    	public static int listTypes(List<EnumCreatureType> types, ITextComponent identifier, CommandSource source)
    	{
    		if(types.isEmpty())
    			source.sendFeedback(makeErrorMessage(translationSlug+"list.failed", identifier), true);
    		else
				source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success", identifier, EnumCreatureType.typesToHeader(types)), true);
    		return types.size();
    	}
	}
	
	private static class VariantAdd
	{
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("add")
    				.then(newLiteral(ENTITY)
    					.then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(newArgument(TYPE, CreatureTypeArgument.type())
    						.executes((source) -> { return VariantAdd.addToEntity(EntitySummonArgument.getEntityId(source, ENTITY), CreatureTypeArgument.getType(source, TYPE), source.getSource()); }))))
					.then(newLiteral(MOB)
    					.then(newArgument(MOB, EntityArgument.entity()).then(newArgument(TYPE, CreatureTypeArgument.type())
    						.executes((source) -> { return VariantAdd.addToMob(EntityArgument.getEntity(source, MOB), CreatureTypeArgument.getType(source, TYPE), source.getSource()); }))));
    	}
    	
    	public static int addToMob(Entity entity, EnumCreatureType type, @Nullable CommandSource source)
    	{
    		if(entity.getType() == EntityType.PLAYER)
    		{
    			if(!CreatureTypes.isPlayerOfType((PlayerEntity)entity, type) && type.getHandler().canApplyTo(CreatureTypes.getMobTypes(entity)))
    				ConfigVO.MOBS.typeSettings.addToPlayer(entity.getName().getUnformattedComponentText(), type);
    			
    			if(source != null)
    			{
	    			source.sendFeedback(new TranslationTextComponent(translationSlug+"add.success", type.getTranslated(), entity.getName()), true);
					source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success", entity.getName(), EnumCreatureType.typesToHeader(CreatureTypes.getMobTypes(entity))), false);
    			}
    			return 15;
    		}
    		else if(entity instanceof LivingEntity)
    			return addToEntity(entity.getType().getRegistryName(), type, source);
    		
    		if(source != null)
    			source.sendFeedback(makeErrorMessage(translationSlug+"add.failed", entity.getName()), true);
    		return 0;
    	}
    	
    	public static int addToEntity(ResourceLocation registry, EnumCreatureType type, @Nullable CommandSource source)
    	{
			if(!CreatureTypes.isMobOfType(registry, type) && type.getHandler().canApplyTo(CreatureTypes.getMobTypes(registry)))
				ConfigVO.MOBS.typeSettings.addToEntity(registry.toString(), type);
			if(source != null)
			{
				source.sendFeedback(new TranslationTextComponent(translationSlug+"add.success", type.getTranslated(), registry.toString()), true);
				source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success", registry.toString(), EnumCreatureType.typesToHeader(CreatureTypes.getMobTypes(registry))), true);
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
    					.then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(newArgument(TYPE, CreatureTypeArgument.type())
    						.executes((source) -> { return VariantRemove.removeFromEntity(EntitySummonArgument.getEntityId(source, ENTITY), CreatureTypeArgument.getType(source, TYPE), source.getSource()); }))))
					.then(newLiteral(MOB)
    					.then(newArgument(MOB, EntityArgument.entity()).then(newArgument(TYPE, CreatureTypeArgument.type())
    						.executes((source) -> { return VariantRemove.removeFromMob(EntityArgument.getEntity(source, MOB), CreatureTypeArgument.getType(source, TYPE), source.getSource()); }))));
    	}
    	
    	public static int removeFromMob(Entity entity, EnumCreatureType type, CommandSource source)
    	{
    		if(entity.getType() == EntityType.PLAYER)
    		{
    			if(CreatureTypes.isPlayerOfType((PlayerEntity)entity, type))
    				ConfigVO.MOBS.typeSettings.removeFromPlayer(entity.getName().getUnformattedComponentText(), type);
    			
    			source.sendFeedback(new TranslationTextComponent(translationSlug+"remove.success", type.getTranslated(), entity.getName()), true);
				source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success", entity.getName(), EnumCreatureType.typesToHeader(CreatureTypes.getMobTypes(entity))), false);
    			return 15;
    		}
    		else if(entity instanceof LivingEntity)
    			return removeFromEntity(entity.getType().getRegistryName(), type, source);
    		source.sendFeedback(makeErrorMessage(translationSlug+"remove.failed", entity.getName()), true);
    		return 0;
    	}
    	
    	public static int removeFromEntity(ResourceLocation registry, EnumCreatureType type, CommandSource source)
    	{
			if(CreatureTypes.isMobOfType(registry, type))
				ConfigVO.MOBS.typeSettings.removeFromEntity(registry.toString(), type);
			source.sendFeedback(new TranslationTextComponent(translationSlug+"remove.success", type.getTranslated(), registry.toString()), true);
			source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success", registry.toString(), EnumCreatureType.typesToHeader(CreatureTypes.getMobTypes(registry))), true);
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
    						.executes((source) -> { return VariantClear.clearFromEntity(EntitySummonArgument.getEntityId(source, ENTITY), source.getSource()); })))
					.then(newLiteral(MOB)
    					.then(newArgument(MOB, EntityArgument.entity())
    						.executes((source) -> { return VariantClear.clearFromMob(EntityArgument.getEntity(source, MOB), source.getSource()); })));
    	}
    	
    	public static int clearFromMob(Entity entity, @Nullable CommandSource source)
    	{
    		if(entity.getType() == EntityType.PLAYER)
    		{
    			for(EnumCreatureType type : CreatureTypes.getPlayerTypes((PlayerEntity)entity, true))
    				ConfigVO.MOBS.typeSettings.removeFromPlayer(entity.getName().getUnformattedComponentText(), type);
    			
        		if(source != null)
        			source.sendFeedback(new TranslationTextComponent(translationSlug+"clear.success", entity.getName()), true);
    			return 15;
    		}
    		else if(entity instanceof LivingEntity)
    			return clearFromEntity(entity.getType().getRegistryName(), source);
    		if(source != null)
    			source.sendFeedback(makeErrorMessage(translationSlug+"clear.failed", entity.getName()), true);
    		return 0;
    	}
    	
    	public static int clearFromEntity(ResourceLocation registry, @Nullable CommandSource source)
    	{
    		for(EnumCreatureType type : CreatureTypes.getMobTypes(registry))
    			ConfigVO.MOBS.typeSettings.removeFromEntity(registry.toString(), type);
    		
    		if(source != null)
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
    					.then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).then(newLiteral("is").then(newArgument(TYPE, CreatureTypeArgument.type())
    						.executes((source) -> { return VariantTest.testForType(CreatureTypes.getMobTypes(EntitySummonArgument.getEntityId(source, ENTITY)), CreatureTypeArgument.getType(source, TYPE), source.getSource()); })))))
					.then(newLiteral(MOB)
    					.then(newArgument(MOB, EntityArgument.entity()).then(newLiteral("is").then(newArgument(TYPE, CreatureTypeArgument.type())
    						.executes((source) -> { return VariantTest.testForType(CreatureTypes.getMobTypes(EntityArgument.getEntity(source, MOB)), CreatureTypeArgument.getType(source, TYPE), source.getSource()); })))));
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
    						.then(newArgument(TYPE, CreatureTypeArgument.type()).suggests(CreatureTypeArgument.SUPERTYPE_SUGGEST)
								.executes((source) -> { return VariantSet.set(EntitySummonArgument.getEntityId(source, ENTITY), null, source.getSource(), CreatureTypeArgument.getType(source, TYPE)); })
								.then(newArgument(TYPE2, CreatureTypeArgument.type()).suggests(CreatureTypeArgument.SUBTYPE_SUGGEST)
									.executes((source) -> { return VariantSet.set(EntitySummonArgument.getEntityId(source, ENTITY), null, source.getSource(), CreatureTypeArgument.getType(source, TYPE), CreatureTypeArgument.getType(source, TYPE2)); })
									.then(newArgument(TYPE3, CreatureTypeArgument.type()).suggests(CreatureTypeArgument.SUBTYPE_SUGGEST)
										.executes((source) -> { return VariantSet.set(EntitySummonArgument.getEntityId(source, ENTITY), null, source.getSource(), CreatureTypeArgument.getType(source, TYPE), CreatureTypeArgument.getType(source, TYPE2), CreatureTypeArgument.getType(source, TYPE3)); })
										.then(newArgument(TYPE4, CreatureTypeArgument.type()).suggests(CreatureTypeArgument.SUBTYPE_SUGGEST)
											.executes((source) -> { return VariantSet.set(EntitySummonArgument.getEntityId(source, ENTITY), null, source.getSource(), CreatureTypeArgument.getType(source, TYPE), CreatureTypeArgument.getType(source, TYPE2), CreatureTypeArgument.getType(source, TYPE3), CreatureTypeArgument.getType(source, TYPE4)); })
											.then(newArgument(TYPE5, CreatureTypeArgument.type()).suggests(CreatureTypeArgument.SUBTYPE_SUGGEST)
												.executes((source) -> { return VariantSet.set(EntitySummonArgument.getEntityId(source, ENTITY), null, source.getSource(), CreatureTypeArgument.getType(source, TYPE), CreatureTypeArgument.getType(source, TYPE2), CreatureTypeArgument.getType(source, TYPE3), CreatureTypeArgument.getType(source, TYPE4), CreatureTypeArgument.getType(source, TYPE5)); })))))))))
    					.then(newLiteral(MOB).then(newArgument(MOB, EntityArgument.entity()).then(newLiteral("to")
    						.then(newArgument(TYPE, CreatureTypeArgument.type()).suggests(CreatureTypeArgument.SUPERTYPE_SUGGEST)
								.executes((source) -> { return VariantSet.set(null, EntityArgument.getEntity(source, MOB), source.getSource(), CreatureTypeArgument.getType(source, TYPE)); })
								.then(newArgument(TYPE2, CreatureTypeArgument.type()).suggests(CreatureTypeArgument.SUBTYPE_SUGGEST)
									.executes((source) -> { return VariantSet.set(null, EntityArgument.getEntity(source, MOB), source.getSource(), CreatureTypeArgument.getType(source, TYPE), CreatureTypeArgument.getType(source, TYPE2)); })
									.then(newArgument(TYPE3, CreatureTypeArgument.type()).suggests(CreatureTypeArgument.SUBTYPE_SUGGEST)
										.executes((source) -> { return VariantSet.set(null, EntityArgument.getEntity(source, MOB), source.getSource(), CreatureTypeArgument.getType(source, TYPE), CreatureTypeArgument.getType(source, TYPE2), CreatureTypeArgument.getType(source, TYPE3)); })
										.then(newArgument(TYPE4, CreatureTypeArgument.type()).suggests(CreatureTypeArgument.SUBTYPE_SUGGEST)
											.executes((source) -> { return VariantSet.set(null, EntityArgument.getEntity(source, MOB), source.getSource(), CreatureTypeArgument.getType(source, TYPE), CreatureTypeArgument.getType(source, TYPE2), CreatureTypeArgument.getType(source, TYPE3), CreatureTypeArgument.getType(source, TYPE4)); })
											.then(newArgument(TYPE5, CreatureTypeArgument.type()).suggests(CreatureTypeArgument.SUBTYPE_SUGGEST)
												.executes((source) -> { return VariantSet.set(null, EntityArgument.getEntity(source, MOB), source.getSource(), CreatureTypeArgument.getType(source, TYPE), CreatureTypeArgument.getType(source, TYPE2), CreatureTypeArgument.getType(source, TYPE3), CreatureTypeArgument.getType(source, TYPE4), CreatureTypeArgument.getType(source, TYPE5)); })))))))));
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
			VariantClear.clearFromEntity(registry, null);
			for(EnumCreatureType type : types)
				VariantAdd.addToEntity(registry, type, null);
			
			source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success", registry.toString(), EnumCreatureType.typesToHeader(CreatureTypes.getMobTypes(registry))), true);
			return 15;
		}
		
		private static int setTypeEntity(Entity entity, CommandSource source, EnumCreatureType... types)
		{
			VariantClear.clearFromMob(entity, null);
			for(EnumCreatureType type : types)
				VariantAdd.addToMob(entity, type, null);
			
			source.sendFeedback(new TranslationTextComponent(translationSlug+"list.success", entity.getName(), EnumCreatureType.typesToHeader(CreatureTypes.getMobTypes(entity))), true);
			return 15;
		}
	}
}
