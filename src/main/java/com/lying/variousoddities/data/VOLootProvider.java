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
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class VOLootProvider extends LootTableProvider
{
    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> tables = new ArrayList<>();
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
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables()
    {
        tables.clear();
        addBlockLootTables();
        addEntityLootTables();
        addChestLootTables();
        return tables;
    }
    
    private void addBlockLootTables()
    {
    	addBlockLootTable(VOLootTables.SCALE_LAYER.getPath(), LootTable.lootTable().withPool(
    			LootPool.lootPool()
    					.name("main")
    					.setRolls(ConstantValue.exactly(1))
    					.add(itemEntry(VOItems.SCALE_KOBOLD, 1).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))));
    }
    
    private void addEntityLootTables()
    {
    	addEntityLootTable(VOLootTables.KOBOLD.getPath(), LootTable.lootTable().withPool(
    			LootPool.lootPool()
    					.name("main")
    					.setRolls(ConstantValue.exactly(1))
    					.add(itemEntry(VOItems.SCALE_KOBOLD, 1).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 3.0F))))));
    	addEntityLootTable(VOLootTables.GOBLIN.getPath(), LootTable.lootTable().withPool(
    			LootPool.lootPool()));
    	addEntityLootTable(VOLootTables.RAT.getPath(), LootTable.lootTable().withPool(
    			LootPool.lootPool()));
    	addEntityLootTable(VOLootTables.GIANT_RAT.getPath(), LootTable.lootTable().withPool(
    			LootPool.lootPool()));
    	addEntityLootTable(VOLootTables.CRAB.getPath(), LootTable.lootTable().withPool(
    			LootPool.lootPool()));
    	addEntityLootTable(VOLootTables.GIANT_CRAB.getPath(), LootTable.lootTable().withPool(
    			LootPool.lootPool()));
    	addEntityLootTable(VOLootTables.SCORPION.getPath(), LootTable.lootTable().withPool(
    			LootPool.lootPool()));
    	addEntityLootTable(VOLootTables.GIANT_SCORPION.getPath(), LootTable.lootTable().withPool(
    			LootPool.lootPool()));
    	addEntityLootTable(VOLootTables.GHASTLING.getPath(), LootTable.lootTable().withPool(
    			LootPool.lootPool()
    					.name("main")
    					.setRolls(ConstantValue.exactly(3))
    					.add(itemEntry(Items.GUNPOWDER, 5))
    					.add(itemEntry(Items.GHAST_TEAR, 1))));
    	addEntityLootTable(VOLootTables.WORG.getPath(), LootTable.lootTable().withPool(
    			LootPool.lootPool()));
    	addEntityLootTable(VOLootTables.WARG.getPath(), LootTable.lootTable().withPool(
    			LootPool.lootPool()));
    }
    
    private void addChestLootTables()
    {
//        addChestLootTable("inject/chests/village/village_weaponsmith", LootTable.lootPool().withPool(
//                LootPool.lootPool()
//                        .name("main")
//                        .rolls(ConstantValue.exactly(1))
//                        .acceptCondition(RandomChance.lootPool(0.06F))
//                        .add(itemEntry(VEItems.COATING_SILVER, 1))
//                )
//        );
    }
    
    private void addLootTable(String location, LootTable.Builder lootTable, LootContextParamSet lootParameterSet)
    {
        if(location.startsWith("inject/"))
        {
            String actualLocation = location.replace("inject/", "");
            Preconditions.checkArgument(existingFileHelper.exists(new ResourceLocation("loot_tables/" + actualLocation + ".json"), PackType.SERVER_DATA), "Loot table %s does not exist in any known data pack", actualLocation);
        }
        tables.add(Pair.of
        		(
        				() -> lootBuilder -> lootBuilder.accept(new ResourceLocation(Reference.ModInfo.MOD_ID, location), lootTable), 
        				lootParameterSet
        		));
    }
    
    @SuppressWarnings("unused")
	private static LootPoolSingletonContainer.Builder<?> tableEntry(ResourceLocation table, int weight)
    {
        return LootTableReference.lootTableReference(table).setWeight(weight);
    }
    
	private static LootPoolSingletonContainer.Builder<?> itemEntry(Item item, int weight)
    {
        return LootItem.lootTableItem(item).setWeight(weight);
    }
    
	private void addBlockLootTable(String location, LootTable.Builder lootTable)
	{
		addLootTable(location, lootTable, LootContextParamSets.BLOCK);
	}
	
    @SuppressWarnings("unused")
	private void addChestLootTable(String location, LootTable.Builder lootTable)
    {
        addLootTable(location, lootTable, LootContextParamSets.CHEST);
    }
    
	private void addEntityLootTable(String location, LootTable.Builder lootTable)
    {
    	addLootTable(location, lootTable, LootContextParamSets.ENTITY);
    }
    
    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
        map.forEach((loc, table) -> LootTables.validate(validationtracker, loc, table));
    }
}
