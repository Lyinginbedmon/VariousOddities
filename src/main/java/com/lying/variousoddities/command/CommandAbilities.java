package com.lying.variousoddities.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.AbilityData;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class CommandAbilities extends CommandBase
{
 	public static final SuggestionProvider<CommandSourceStack> ABILITY_SUGGESTIONS = SuggestionProviders.register(new ResourceLocation("ability_names"), (context, builder) -> {
 		return SharedSuggestionProvider.suggestResource(AbilityRegistry.getAbilityNames(), builder);
 		});
 	
    private static final SimpleCommandExceptionType INVALID_ENTITY_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.entity.invalid"));
 	
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".abilities.";
	private static final String ENTITY = "entity";
	private static final String ABILITY = "ability";
	private static final String NBT = "nbt";
	
	private static MutableComponent CLICK_MODIFY = Component.translatable(translationSlug+"list_click").withStyle((style2) -> { return style2.applyFormat(ChatFormatting.AQUA); });
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("abilities").requires((source) -> { return source.hasPermission(2); } )
			.then(Commands.argument(ENTITY, EntityArgument.entity())
				.then(Commands.literal("list").executes((source) -> { return listAbilities(EntityArgument.getEntity(source, ENTITY), source.getSource()); }))
				.then(VariantGet.build())
				.then(VariantAdd.build())
				.then(VariantEdit.build())
				.then(VariantRemove.build())
				.then(Commands.literal("clear").executes((source) -> { return clearAbilities(EntityArgument.getEntity(source, ENTITY), source.getSource()); })));
		
		dispatcher.register(literal);
	}
	
	public static Component getAbilityWithEdit(Ability ability, LivingEntity host)
	{
		MutableComponent abilityEntry = (MutableComponent)ability.getDisplayName();
		MutableComponent mapNameEntry = Component.literal(ability.getMapName().toString());
		
		abilityEntry.withStyle((style) -> { return style
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mapNameEntry.append("\n").append(CLICK_MODIFY)))
				.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/abilities "+(host.getType() ==  EntityType.PLAYER ? host.getName().getString() : host.getUUID().toString())+" edit "+ability.getMapName()+" "+(ability.writeAtomically(new CompoundTag())).toString())); });
		
		return abilityEntry;
	}
	
	private static int listAbilities(Entity entity, CommandSourceStack source) throws CommandSyntaxException
	{
		if(entity instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)entity;
			Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(living);
			source.sendSuccess(Component.translatable(translationSlug+"list", living.getDisplayName(), abilityMap.size()), true);
			List<Ability> abilities = Lists.newArrayList(abilityMap.values());
			Collections.sort(abilities, Ability.SORT_ABILITY);
			for(Ability ability : abilities)
				source.sendSuccess(Component.literal("  ").append(getAbilityWithEdit(ability, living)), false);
		}
		else
			throw INVALID_ENTITY_EXCEPTION.create();
		return 15;
	}
	
	private static int clearAbilities(Entity entity, CommandSourceStack source) throws CommandSyntaxException
	{
		if(entity instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)entity;
			AbilityData data = AbilityData.forEntity(living);
			int tally = data.size();
			data.clearCustomAbilities();
			source.sendSuccess(Component.translatable(translationSlug+"clear", tally, living.getDisplayName()), true);
		}
		else
			throw INVALID_ENTITY_EXCEPTION.create();
		return 15;
	}
	
	private static class VariantGet
	{
		private static final DynamicCommandExceptionType GET_INVALID_EXCEPTION = new DynamicCommandExceptionType((p_208922_0_) -> {
				return Component.translatable("commands.data.get.invalid", p_208922_0_);
			});
		   
		public static LiteralArgumentBuilder<CommandSourceStack> build()
		{
			return Commands.literal("get")
					.then(Commands.argument(ABILITY, ResourceLocationArgument.id()).suggests(ABILITY_SUGGESTIONS)
						.executes((source) -> { return getAbility(EntityArgument.getEntity(source, ENTITY), ResourceLocationArgument.getId(source, ABILITY), source.getSource()); }));
		}
		
		private static int getAbility(Entity entity, ResourceLocation mapName, CommandSourceStack source) throws CommandSyntaxException
		{
			if(entity instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entity;
				Ability ability = AbilityRegistry.getAbilityByMapName(living, mapName);
				if(ability != null)
				{
					CompoundTag nbt = ability.writeAtomically(new CompoundTag());
					source.sendSuccess(Component.translatable(translationSlug+"get", mapName.toString(), NbtUtils.toPrettyComponent(nbt)), true);
				}
				else
					throw GET_INVALID_EXCEPTION.create(mapName);
			}
			else
				throw INVALID_ENTITY_EXCEPTION.create();
			return 15;
		}
	}
	
	private static class VariantAdd
	{
		
		public static LiteralArgumentBuilder<CommandSourceStack> build()
		{
			return Commands.literal("add")
					.then(Commands.argument(ABILITY, ResourceLocationArgument.id()).suggests(ABILITY_SUGGESTIONS)
						.executes((source) -> { return addAbility(EntityArgument.getEntity(source, ENTITY), ResourceLocationArgument.getId(source, ABILITY), new CompoundTag(), source.getSource()); })
						.then(Commands.argument(NBT, CompoundTagArgument.compoundTag())
							.executes((source) -> { return addAbility(EntityArgument.getEntity(source, ENTITY), ResourceLocationArgument.getId(source, ABILITY), CompoundTagArgument.getCompoundTag(source, NBT), source.getSource()); })));
		}
		
		private static int addAbility(Entity entity, ResourceLocation registryName, CompoundTag nbt, CommandSourceStack source) throws CommandSyntaxException
		{
			if(entity instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entity;
				AbilityData data = AbilityData.forEntity(living);
				Ability ability = AbilityRegistry.getAbility(registryName, nbt);
				if(ability != null)
					data.addCustomAbility(ability);
				source.sendSuccess(Component.translatable(translationSlug+"add", getAbilityWithEdit(ability, living), living.getDisplayName()), true);
			}
			else
				throw INVALID_ENTITY_EXCEPTION.create();
			return 15;
		}
	}
	
	private static class VariantEdit
	{
		private static final DynamicCommandExceptionType EDIT_INVALID_EXCEPTION = new DynamicCommandExceptionType((p_208922_0_) -> {
			return Component.translatable("commands.data.edit.invalid", p_208922_0_);
		});
		
		public static LiteralArgumentBuilder<CommandSourceStack> build()
		{
			return Commands.literal("edit")
					.then(Commands.argument(ABILITY, ResourceLocationArgument.id())
						.then(Commands.argument(NBT, CompoundTagArgument.compoundTag())
							.executes((source) -> { return editAbility(EntityArgument.getEntity(source, ENTITY), ResourceLocationArgument.getId(source, ABILITY), CompoundTagArgument.getCompoundTag(source, NBT), source.getSource()); })));
		}
		
		private static int editAbility(Entity entity, ResourceLocation mapName, CompoundTag nbt, CommandSourceStack source) throws CommandSyntaxException
		{
			if(entity instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entity;
				AbilityData data = AbilityData.forEntity(living);
				Ability ability = AbilityRegistry.getAbilityByMapName(living, mapName);
				if(ability != null)
				{
					CompoundTag originalNBT = ability.writeAtomically(new CompoundTag());
					originalNBT.merge(nbt);
					
					Ability ability2 = AbilityRegistry.getAbility(originalNBT);
					if(ability2 != null)
					{
						data.removeCustomAbility(ability);
						data.addCustomAbility(ability2);
						source.sendSuccess(Component.translatable(translationSlug+"add", getAbilityWithEdit(ability2, living), living.getDisplayName()), true);
					}
					else
						throw EDIT_INVALID_EXCEPTION.create(mapName);
				}
				else
					throw EDIT_INVALID_EXCEPTION.create(mapName);
			}
			else
				throw INVALID_ENTITY_EXCEPTION.create();
			return 15;
		}
	}
	
	private static class VariantRemove
	{
		public static LiteralArgumentBuilder<CommandSourceStack> build()
		{
			return Commands.literal("remove")
					.then(Commands.argument(ABILITY, ResourceLocationArgument.id())
						.executes((source) -> { return removeAbility(EntityArgument.getEntity(source, ENTITY), ResourceLocationArgument.getId(source, ABILITY), source.getSource()); }));
		}
		
		private static int removeAbility(Entity entity, ResourceLocation mapName, CommandSourceStack source) throws CommandSyntaxException
		{
			if(entity instanceof LivingEntity)
			{
				LivingEntity living = (LivingEntity)entity;
				AbilityData data = AbilityData.forEntity(living);
				data.removeCustomAbility(mapName);
				source.sendSuccess(Component.translatable(translationSlug+"remove", mapName, living.getDisplayName()), true);
			}
			else
				throw INVALID_ENTITY_EXCEPTION.create();
			return 15;
		}
	}
}
