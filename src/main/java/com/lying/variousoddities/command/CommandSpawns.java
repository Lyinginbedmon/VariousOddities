package com.lying.variousoddities.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.ibm.icu.impl.Pair;
import com.lying.variousoddities.reference.Reference;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntitySummonArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.MobSpawnInfo.Spawners;
import net.minecraftforge.registries.ForgeRegistries;

public class CommandSpawns extends CommandBase
{
	public static final DynamicCommandExceptionType field_241044_a_ = new DynamicCommandExceptionType((p_241052_0_) -> {
		return new TranslationTextComponent("commands.locatebiome.invalid", p_241052_0_);
		});
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".spawns.";
	
	private static final String BIOME = "biome";
	private static final String ENTITY = "entity";
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> literal = newLiteral("vospawns").requires((source) -> { return source.hasPermissionLevel(2); } )
				.executes((source) -> { return listSpawnsHere(source.getSource()); })
					.then(newLiteral("in").then(newArgument(BIOME, ResourceLocationArgument.resourceLocation()).suggests(SuggestionProviders.field_239574_d_)
						.executes((source) -> { return listSpawnsInBiome(ResourceLocationArgument.getResourceLocation(source, BIOME), source.getSource()); })))
					.then(newLiteral("of").then(newArgument(ENTITY, EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
						.executes((source) -> { return listSpawnsOfEntity(EntitySummonArgument.getEntityId(source, ENTITY), source.getSource()); })));
		
		dispatcher.register(literal);
	}
	
	private static ITextComponent makeBiomeListCommand(ResourceLocation name)
	{
		return (new StringTextComponent("[").append(new StringTextComponent(name.toString())).append(new StringTextComponent("]"))).modifyStyle((style) -> 
		{
			return style.setFormatting(TextFormatting.DARK_AQUA).setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new TranslationTextComponent("command.varodd.spawns.list_biome.click"))).setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.valueOf("/vospawns in "+name.toString()))); 
		});
	}
	
	private static ITextComponent makeEntityListCommand(EntityType<?> name)
	{
		return (new StringTextComponent("[").append(name.getName()).append(new StringTextComponent("]"))).modifyStyle((style) -> 
		{
			return style.setFormatting(TextFormatting.DARK_AQUA).setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new TranslationTextComponent("command.varodd.spawns.list_entity.click"))).setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.valueOf("/vospawns of "+name.getRegistryName().toString()))); 
		});
	}
	
	private static int listSpawnsHere(CommandSource source)
	{
		return listSpawnsInBiome(source.getWorld().getBiome(new BlockPos(source.getPos())), source);
	}
	
	private static int listSpawnsInBiome(ResourceLocation biomeName, CommandSource source) throws CommandSyntaxException
	{
		return listSpawnsInBiome(ForgeRegistries.BIOMES.getValue(biomeName), source);
	}
	
	private static int listSpawnsOfEntity(ResourceLocation mobName, CommandSource source)
	{
		Optional<EntityType<?>> optional = EntityType.byKey(mobName.toString());
		if(!optional.isPresent())
			return 0;
		
		EntityType<?> entity = optional.get();
		Map<Biome, Pair<Integer, Integer>> biomeToWeight = new HashMap<>();
		for(Biome biome : ForgeRegistries.BIOMES.getValues())
		{
//			System.out.println("Biome "+biome.getRegistryName());
			MobSpawnInfo info = biome.getMobSpawnInfo();
			for(Spawners spawner : info.getSpawners(entity.getClassification()))
			{
				EntityType<?> type = spawner.type;
//				System.out.println("  "+type.getRegistryName());
				if(type == entity)
				{
					biomeToWeight.put(biome, Pair.of(spawner.itemWeight, totalSpawnWeight(info, entity.getClassification())));
					break;
				}
			}
		}
		
		source.sendFeedback(new TranslationTextComponent(translationSlug+"biome_entry", biomeToWeight.size(), entity.getName()), true);
		if(!biomeToWeight.isEmpty())
		{
			source.sendFeedback(new StringTextComponent("  ").append(new TranslationTextComponent(translationSlug+"type_entry", entity.getClassification().name())), false);
			for(Biome biome : biomeToWeight.keySet())
			{
				Pair<Integer, Integer> weightTotal = biomeToWeight.get(biome);
				int weight = weightTotal.first;
				int chance = (int)Math.round((double)weight / (double)weightTotal.second * 100);
				source.sendFeedback(new StringTextComponent("    ").append(new TranslationTextComponent(translationSlug+"entity_entry", makeBiomeListCommand(biome.getRegistryName()), weight, chance + "%")), false);
			}
		}
		return 15;
	}
	
	private static int listSpawnsInBiome(Biome biome, CommandSource source)
	{
		if(biome != null)
		{
			MobSpawnInfo info = biome.getMobSpawnInfo();
			source.sendFeedback(new TranslationTextComponent(translationSlug+"biome_entry", totalSpawnsInBiome(info), biome.getRegistryName()), false);
			for(EntityClassification entityType : info.getSpawnerTypes())
			{
				int totalWeight = totalSpawnWeight(info, entityType);
				if(totalWeight == 0)
					continue;
				
				source.sendFeedback(new StringTextComponent("  ").append(new TranslationTextComponent(translationSlug+"type_entry", entityType.name())), false);
				for(Spawners spawner : info.getSpawners(entityType))
				{
					EntityType<?> entity = spawner.type;
					int weight = spawner.itemWeight;
					int chance = (int)Math.round((double)weight / (double)totalWeight * 100);
					source.sendFeedback(new StringTextComponent("    ").append(new TranslationTextComponent(translationSlug+"entity_entry", makeEntityListCommand(entity), weight, chance + "%")), false);
				}
			}
		}
		return 15;
	}
	
	@SuppressWarnings("unused")
	private static int totalSpawnsInBiome(MobSpawnInfo info)
	{
		int spawns = 0;
		for(EntityClassification type : info.getSpawnerTypes())
			for(Spawners spawner : info.getSpawners(type))
				spawns++;
		return spawns;
	}
	
	private static int totalSpawnWeight(MobSpawnInfo info, EntityClassification type)
	{
		int totalWeight = 0;
		for(Spawners spawner : info.getSpawners(type))
			totalWeight += spawner.itemWeight;
		return totalWeight;
	}
}
