package com.lying.variousoddities.init;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;

public class VOLootTables
{
	public static final List<String> INJECTED_LOOT_TABLES = Lists.newArrayList();
	
	public static final ResourceLocation KOBOLD			= new ResourceLocation(Reference.ModInfo.MOD_ID, "entities/kobold");
	public static final ResourceLocation GOBLIN			= new ResourceLocation(Reference.ModInfo.MOD_ID, "entities/goblin");
	public static final ResourceLocation RAT			= new ResourceLocation(Reference.ModInfo.MOD_ID, "entities/rat");
	public static final ResourceLocation GIANT_RAT		= new ResourceLocation(Reference.ModInfo.MOD_ID, "entities/giant_rat");
	public static final ResourceLocation GHASTLING		= new ResourceLocation(Reference.ModInfo.MOD_ID, "entities/ghastling");
	public static final ResourceLocation CRAB			= new ResourceLocation(Reference.ModInfo.MOD_ID, "entities/crab");
	public static final ResourceLocation GIANT_CRAB		= new ResourceLocation(Reference.ModInfo.MOD_ID, "entities/giant_crab");
	public static final ResourceLocation SCORPION			= new ResourceLocation(Reference.ModInfo.MOD_ID, "entities/scorpion");
	public static final ResourceLocation GIANT_SCORPION		= new ResourceLocation(Reference.ModInfo.MOD_ID, "entities/giant_scorpion");
	public static final ResourceLocation WORG			= new ResourceLocation(Reference.ModInfo.MOD_ID, "entities/worg");
	public static final ResourceLocation WARG			= new ResourceLocation(Reference.ModInfo.MOD_ID, "entities/warg");
	
	public static final ResourceLocation SCALE_LAYER	= new ResourceLocation(Reference.ModInfo.MOD_ID, "blocks/scale_layer");
	
	public static void onLootLoadEvent(LootTableLoadEvent event)
	{
		if(event.getName().getNamespace().equals("minecraft"))
		{
			String name = event.getName().toString();
			String file = event.getName().getPath();
			if(INJECTED_LOOT_TABLES.contains(file))
			{
				VariousOddities.log.info("Injecting loot pool to loot table "+name);
				event.getTable().addPool(getInjectPool(file));
			}
		}
	}
	
	private static LootPool getInjectPool(String entryName)
	{
		return LootPool.lootPool()
				.add(getInjectEntry(entryName, 100))
				.setBonusRolls(UniformGenerator.between(0, 1))
				.name(Reference.ModInfo.MOD_ID + "_inject")
				.build();
	}
	
	private static LootPoolEntryContainer.Builder<?> getInjectEntry(String name, int weight)
	{
		ResourceLocation table = new ResourceLocation(Reference.ModInfo.MOD_ID, "inject/" + name);
		return LootTableReference.lootTableReference(table).setWeight(weight);
	}
	
	static
	{
//		INJECTED_LOOT_TABLES.add("village/village_weaponsmith");
	}
}