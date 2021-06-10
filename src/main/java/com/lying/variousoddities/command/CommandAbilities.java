package com.lying.variousoddities.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class CommandAbilities extends CommandBase
{
 	public static final SuggestionProvider<CommandSource> ABILITY_SUGGESTIONS = SuggestionProviders.register(new ResourceLocation("ability_names"), (context, builder) -> {
 		return ISuggestionProvider.suggestIterable(AbilityRegistry.getAbilityNames(), builder);
 		});
 	
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".abilities.";
	private static final String ENTITY = "entity";
	private static final String ABILITY = "ability";
	private static final String NBT = "nbt";
	
	private static IFormattableTextComponent CLICK_MODIFY = new TranslationTextComponent(translationSlug+"list_click").modifyStyle((style2) -> { return style2.setFormatting(TextFormatting.AQUA); });
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> literal = newLiteral("abilities").requires((source) -> { return source.hasPermissionLevel(2); } )
			.then(newArgument(ENTITY, EntityArgument.entity())
				.then(newLiteral("list").executes((source) -> { return listAbilities(EntityArgument.getEntity(source, ENTITY), source.getSource()); }))
				.then(VariantGet.build())
				.then(VariantAdd.build())
				.then(VariantEdit.build())
				.then(VariantRemove.build())
				.then(newLiteral("clear").executes((source) -> { return clearAbilities(EntityArgument.getEntity(source, ENTITY), source.getSource()); })));
		
		dispatcher.register(literal);
	}
	
	public static ITextComponent getAbilityWithEdit(Ability ability, LivingEntity host)
	{
		IFormattableTextComponent abilityEntry = (IFormattableTextComponent)ability.getDisplayName();
		IFormattableTextComponent mapNameEntry = new StringTextComponent(ability.getMapName().toString());
		
		abilityEntry.modifyStyle((style) -> { return style
				.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mapNameEntry.appendString("\n").append(CLICK_MODIFY)))
				.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/abilities "+(host.getType() ==  EntityType.PLAYER ? host.getName().getUnformattedComponentText() : host.getUniqueID().toString())+" edit "+ability.getMapName()+" "+(ability.writeAtomically(new CompoundNBT())).toString())); });
		
		return abilityEntry;
	}
	
	private static int listAbilities(Entity entity, CommandSource source)
	{
		if(entity instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)entity;
			Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(living);
			source.sendFeedback(new TranslationTextComponent(translationSlug+"list", living.getDisplayName(), abilityMap.size()), true);
			List<Ability> abilities = Lists.newArrayList(abilityMap.values());
			Collections.sort(abilities, Ability.SORT_ABILITY);
			for(Ability ability : abilities)
				source.sendFeedback(new StringTextComponent("  ").append(getAbilityWithEdit(ability, living)), false);
		}
		return 15;
	}
	
	private static int clearAbilities(Entity entity, CommandSource source)
	{
		if(entity instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)entity;
			LivingData data = LivingData.forEntity(living);
			int tally = data.getAbilities().size();
			data.getAbilities().clearCustomAbilities();
			source.sendFeedback(new TranslationTextComponent(translationSlug+"clear", tally, living.getDisplayName()), true);
		}
		return 15;
	}
	
	private static class VariantGet
	{
		private static final DynamicCommandExceptionType GET_INVALID_EXCEPTION = new DynamicCommandExceptionType((p_208922_0_) -> {
				return new TranslationTextComponent("commands.data.get.invalid", p_208922_0_);
			});
		   
		public static LiteralArgumentBuilder<CommandSource> build()
		{
			return newLiteral("get")
					.then(newArgument(ABILITY, ResourceLocationArgument.resourceLocation()).suggests(ABILITY_SUGGESTIONS)
						.executes((source) -> { return getAbility(EntityArgument.getEntity(source, ENTITY), ResourceLocationArgument.getResourceLocation(source, ABILITY), source.getSource()); }));
		}
		
		private static int getAbility(Entity entity, ResourceLocation mapName, CommandSource source) throws CommandSyntaxException
		{
			if(entity instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entity;
				Ability ability = AbilityRegistry.getAbilityByName(living, mapName);
				if(ability != null)
				{
					CompoundNBT nbt = ability.writeAtomically(new CompoundNBT());
					source.sendFeedback(new TranslationTextComponent(translationSlug+"get", mapName.toString(), nbt.toFormattedComponent()), true);
				}
				else
					throw GET_INVALID_EXCEPTION.create(mapName);
			}
			return 15;
		}
	}
	
	private static class VariantAdd
	{
		
		public static LiteralArgumentBuilder<CommandSource> build()
		{
			return newLiteral("add")
					.then(newArgument(ABILITY, ResourceLocationArgument.resourceLocation()).suggests(ABILITY_SUGGESTIONS)
						.executes((source) -> { return addAbility(EntityArgument.getEntity(source, ENTITY), ResourceLocationArgument.getResourceLocation(source, ABILITY), new CompoundNBT(), source.getSource()); })
						.then(newArgument(NBT, NBTCompoundTagArgument.nbt())
							.executes((source) -> { return addAbility(EntityArgument.getEntity(source, ENTITY), ResourceLocationArgument.getResourceLocation(source, ABILITY), NBTCompoundTagArgument.getNbt(source, NBT), source.getSource()); })));
		}
		
		private static int addAbility(Entity entity, ResourceLocation registryName, CompoundNBT nbt, CommandSource source)
		{
			if(entity instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entity;
				LivingData data = LivingData.forEntity(living);
				Ability ability = AbilityRegistry.getAbility(registryName, nbt);
				if(ability != null)
					data.getAbilities().addCustomAbility(ability);
				source.sendFeedback(new TranslationTextComponent(translationSlug+"add", getAbilityWithEdit(ability, living), living.getDisplayName()), true);
			}
			return 15;
		}
	}
	
	private static class VariantEdit
	{
		private static final DynamicCommandExceptionType EDIT_INVALID_EXCEPTION = new DynamicCommandExceptionType((p_208922_0_) -> {
			return new TranslationTextComponent("commands.data.edit.invalid", p_208922_0_);
		});
		
		public static LiteralArgumentBuilder<CommandSource> build()
		{
			return newLiteral("edit")
					.then(newArgument(ABILITY, ResourceLocationArgument.resourceLocation())
						.then(newArgument(NBT, NBTCompoundTagArgument.nbt())
							.executes((source) -> { return editAbility(EntityArgument.getEntity(source, ENTITY), ResourceLocationArgument.getResourceLocation(source, ABILITY), NBTCompoundTagArgument.getNbt(source, NBT), source.getSource()); })));
		}
		
		private static int editAbility(Entity entity, ResourceLocation mapName, CompoundNBT nbt, CommandSource source) throws CommandSyntaxException
		{
			if(entity instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entity;
				LivingData data = LivingData.forEntity(living);
				Ability ability = AbilityRegistry.getAbilityByName(living, mapName);
				if(ability != null)
				{
					CompoundNBT originalNBT = ability.writeAtomically(new CompoundNBT());
					originalNBT.merge(nbt);
					
					Ability ability2 = AbilityRegistry.getAbility(originalNBT);
					if(ability2 != null)
					{
						data.getAbilities().removeCustomAbility(ability);
						data.getAbilities().addCustomAbility(ability2);
						source.sendFeedback(new TranslationTextComponent(translationSlug+"add", getAbilityWithEdit(ability2, living), living.getDisplayName()), true);
					}
					else
						throw EDIT_INVALID_EXCEPTION.create(mapName);
				}
				else
					throw EDIT_INVALID_EXCEPTION.create(mapName);
			}
			return 15;
		}
	}
	
	private static class VariantRemove
	{
		public static LiteralArgumentBuilder<CommandSource> build()
		{
			return newLiteral("remove")
					.then(newArgument(ABILITY, ResourceLocationArgument.resourceLocation())
						.executes((source) -> { return removeAbility(EntityArgument.getEntity(source, ENTITY), ResourceLocationArgument.getResourceLocation(source, ABILITY), source.getSource()); }));
		}
		
		private static int removeAbility(Entity entity, ResourceLocation mapName, CommandSource source)
		{
			if(entity instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entity;
				LivingData data = LivingData.forEntity(living);
				data.getAbilities().removeCustomAbility(mapName);
				source.sendFeedback(new TranslationTextComponent(translationSlug+"remove", mapName, living.getDisplayName()), true);
			}
			return 15;
		}
	}
}
