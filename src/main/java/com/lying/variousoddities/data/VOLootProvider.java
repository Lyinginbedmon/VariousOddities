package com.lying.variousoddities.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.lying.variousoddities.init.VOItems;
import com.lying.variousoddities.init.VOLootTables;
import com.lying.variousoddities.reference.Reference;
import com.mojang.datafixers.util.Pair;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

public class VOLootProvider extends LootTableProvider
{
    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> tables = new ArrayList<>();
    private final ExistingFileHelper existingFileHelper;
    
	public VOLootProvider(DataGenerator dataGeneratorIn, ExistingFileHelper existingFileHelperIn)
	{
		super(dataGeneratorIn);
		this.existingFileHelper = existingFileHelperIn;
	}
	
	@Override
	public String getName()
	{
		return "Various Oddities loot tables";
	}
	
    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables()
    {
        tables.clear();
        addEntityLootTables();
        addChestLootTables();
        return tables;
    }
    
    private void addEntityLootTables()
    {
    	addEntityLootTable(VOLootTables.KOBOLD.getPath(), LootTable.builder().addLootPool(
    			LootPool.builder()
    					.name("main")
    					.rolls(ConstantRange.of(1))
    					.addEntry(itemEntry(VOItems.SCALE_KOBOLD, 1).acceptFunction(SetCount.builder(RandomValueRange.of(0.0F, 3.0F))))));
    	addEntityLootTable(VOLootTables.GOBLIN.getPath(), LootTable.builder().addLootPool(
    			LootPool.builder()));
    	addEntityLootTable(VOLootTables.RAT.getPath(), LootTable.builder().addLootPool(
    			LootPool.builder()));
    	addEntityLootTable(VOLootTables.GIANT_RAT.getPath(), LootTable.builder().addLootPool(
    			LootPool.builder()));
    	addEntityLootTable(VOLootTables.CRAB.getPath(), LootTable.builder().addLootPool(
    			LootPool.builder()));
    	addEntityLootTable(VOLootTables.GIANT_CRAB.getPath(), LootTable.builder().addLootPool(
    			LootPool.builder()));
    	addEntityLootTable(VOLootTables.SCORPION.getPath(), LootTable.builder().addLootPool(
    			LootPool.builder()));
    	addEntityLootTable(VOLootTables.GIANT_SCORPION.getPath(), LootTable.builder().addLootPool(
    			LootPool.builder()));
    	addEntityLootTable(VOLootTables.GHASTLING.getPath(), LootTable.builder().addLootPool(
    			LootPool.builder()
    					.name("main")
    					.rolls(ConstantRange.of(3))
    					.addEntry(itemEntry(Items.GUNPOWDER, 5))
    					.addEntry(itemEntry(Items.GHAST_TEAR, 1))));
    	addEntityLootTable(VOLootTables.WORG.getPath(), LootTable.builder().addLootPool(
    			LootPool.builder()));
    	addEntityLootTable(VOLootTables.WARG.getPath(), LootTable.builder().addLootPool(
    			LootPool.builder()));
    }
    
    private void addChestLootTables()
    {
//        addChestLootTable("inject/chests/village/village_weaponsmith", LootTable.builder().addLootPool(
//                LootPool.builder()
//                        .name("main")
//                        .rolls(ConstantRange.of(1))
//                        .acceptCondition(RandomChance.builder(0.06F))
//                        .addEntry(itemEntry(VEItems.COATING_SILVER, 1))
//                )
//        );
    }
    
    private void addLootTable(String location, LootTable.Builder lootTable, LootParameterSet lootParameterSet)
    {
        if(location.startsWith("inject/"))
        {
            String actualLocation = location.replace("inject/", "");
            Preconditions.checkArgument(existingFileHelper.exists(new ResourceLocation("loot_tables/" + actualLocation + ".json"), ResourcePackType.SERVER_DATA), "Loot table %s does not exist in any known data pack", actualLocation);
        }
        tables.add(Pair.of
        		(
        				() -> lootBuilder -> lootBuilder.accept(new ResourceLocation(Reference.ModInfo.MOD_ID, location), lootTable), 
        				lootParameterSet
        		));
    }
    
    @SuppressWarnings("unused")
	private static LootEntry.Builder<?> tableEntry(ResourceLocation table, int weight)
    {
        return TableLootEntry.builder(table).weight(weight);
    }
    
	private static StandaloneLootEntry.Builder<?> itemEntry(Item item, int weight)
    {
        return ItemLootEntry.builder(item).weight(weight);
    }
    
    @SuppressWarnings("unused")
	private void addChestLootTable(String location, LootTable.Builder lootTable) {
        addLootTable(location, lootTable, LootParameterSets.CHEST);
    }
    
	private void addEntityLootTable(String location, LootTable.Builder lootTable)
    {
    	addLootTable(location, lootTable, LootParameterSets.ENTITY);
    }
    
    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
        map.forEach((loc, table) -> LootTableManager.validateLootTable(validationtracker, loc, table));
    }
}
