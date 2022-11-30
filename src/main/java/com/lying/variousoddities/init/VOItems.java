package com.lying.variousoddities.init;

import com.lying.variousoddities.inventory.ContainerBody;
import com.lying.variousoddities.inventory.ContainerPlayerBody;
import com.lying.variousoddities.inventory.ContainerWarg;
import com.lying.variousoddities.item.ItemMossBottle;
import com.lying.variousoddities.item.ItemOddEgg;
import com.lying.variousoddities.item.ItemPhylactery;
import com.lying.variousoddities.item.ItemSap;
import com.lying.variousoddities.item.ItemScrollRemaking;
import com.lying.variousoddities.item.ItemSpellList;
import com.lying.variousoddities.item.ItemSpellScroll;
import com.lying.variousoddities.item.VOItemGroup;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class VOItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.ModInfo.MOD_ID);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Reference.ModInfo.MOD_ID);
    
	// Debug
	public static final RegistryObject<Item> SAP	= register("sap", new ItemSap(new Item.Properties().tab(VOItemGroup.LOOT)));
	
	// Spawn Eggs
	public static final RegistryObject<Item> SPAWN_EGG_KOBOLD	= register("kobold_spawn_egg", new ItemOddEgg(VOEntities.KOBOLD.get(), 16167425, 15826224, new Item.Properties()));
	public static final RegistryObject<Item> SPAWN_EGG_GOBLIN	= register("goblin_spawn_egg", new ItemOddEgg(VOEntities.GOBLIN.get(), 5349438, 8306542, new Item.Properties()));;
	public static final RegistryObject<Item> SPAWN_EGG_RAT		= register("rat_spawn_egg", new ItemOddEgg(VOEntities.RAT.get(), 6043662, 3679244, new Item.Properties()));;
	public static final RegistryObject<Item> SPAWN_EGG_RAT_GIANT	= register("giant_rat_spawn_egg", new ItemOddEgg(VOEntities.RAT_GIANT.get(), 6043662, 3679244, new Item.Properties()));;
	public static final RegistryObject<Item> SPAWN_EGG_SCORPION		= register("scorpion_spawn_egg", new ItemOddEgg(VOEntities.SCORPION.get(), 14704695, 14696759, new Item.Properties()));;
	public static final RegistryObject<Item> SPAWN_EGG_SCORPION_GIANT	= register("giant_scorpion_spawn_egg", new ItemOddEgg(VOEntities.SCORPION_GIANT.get(), 14704695, 6366997, new Item.Properties()));;
	public static final RegistryObject<Item> SPAWN_EGG_CRAB			= register("crab_spawn_egg", new ItemOddEgg(VOEntities.CRAB.get(), 10489616, 16775294, new Item.Properties()));;
	public static final RegistryObject<Item> SPAWN_EGG_CRAB_GIANT	= register("giant_crab_spawn_egg", new ItemOddEgg(VOEntities.CRAB_GIANT.get(), 10489616, 16775294, new Item.Properties()));;
	public static final RegistryObject<Item> SPAWN_EGG_WORG			= register("worg_spawn_egg", new ItemOddEgg(VOEntities.WORG.get(), 14670297, 3749941, new Item.Properties()));;
	public static final RegistryObject<Item> SPAWN_EGG_WARG			= register("warg_spawn_egg", new ItemOddEgg(VOEntities.WARG.get(), 6898719, 1248261, new Item.Properties()));;
	public static final RegistryObject<Item> SPAWN_EGG_GHASTLING	= register("ghastling_spawn_egg", new ItemOddEgg(VOEntities.GHASTLING.get(), 16382457, 12369084, new Item.Properties()));;
	
	// Mob module
	public static final RegistryObject<Item> SCALE_KOBOLD	= register("kobold_scale", new Item(new Item.Properties().tab(VOItemGroup.LOOT)));
	public static final RegistryObject<Item> MOSS_BOTTLE	= register("moss_bottle", new ItemMossBottle(new Item.Properties().tab(VOItemGroup.LOOT)));
	
	// Consumables
	public static final RegistryObject<Item> SCROLL_SPECIES	= register("remaking_scroll", new ItemScrollRemaking(false, new Item.Properties().tab(VOItemGroup.LOOT)));
	public static final RegistryObject<Item> SCROLL_RANDOM_SPECIES	= register("corrupted_remaking_scroll", new ItemScrollRemaking(true, new Item.Properties().tab(VOItemGroup.LOOT)));
	
	// Block items
	public static final RegistryObject<Item> DRAFTING_TABLE	= registerBlock("drafting_table", VOBlocks.TABLE_DRAFTING.get(), VOItemGroup.LOOT);
	public static final RegistryObject<Item> EGG_KOBOLD		= registerBlock("kobold_egg", VOBlocks.EGG_KOBOLD.get());
	public static final RegistryObject<Item> EGG_KOBOLD_INERT	= registerBlock("inert_kobold_egg", VOBlocks.EGG_KOBOLD_INERT.get());
	public static final RegistryObject<Item> MOSS_BLOCK		= registerBlock("moss_block", VOBlocks.MOSS_BLOCK.get());
	public static final RegistryObject<Item> LAYER_SCALE		= registerBlock("scale_layer", VOBlocks.LAYER_SCALE.get());
	public static final RegistryObject<Item> PHYLACTERY		= registerBlock("phylactery", new ItemPhylactery(new Item.Properties().tab(VOItemGroup.BLOCKS).stacksTo(1)));
	public static final RegistryObject<Item> PHYLACTERY_EMPTY	= registerBlock("empty_phylactery", new BlockItem(VOBlocks.PHYLACTERY_EMPTY.get(), new Item.Properties().tab(VOItemGroup.BLOCKS).stacksTo(1)));
	
	// Magic items
	public static final RegistryObject<Item> SPELL_LIST		= register("spell_list", new ItemSpellList(new Properties().tab(VOItemGroup.LOOT)));
	public static final RegistryObject<Item> SPELL_SCROLL	= register("spell_scroll", new ItemSpellScroll(new Properties().tab(VOItemGroup.LOOT)));
	
	// Containers
	public static final MenuType<ContainerWarg> CONTAINER_WARG	= registerContainer("warg_inventory", ContainerWarg::fromNetwork);
	public static final MenuType<ContainerBody> CONTAINER_BODY	= registerContainer("body_inventory", ContainerBody::fromNetwork);
	public static final MenuType<ContainerPlayerBody> CONTAINER_PLAYER_BODY	= registerContainer("player_body_inventory", ContainerPlayerBody::fromNetwork);
	
	public static RegistryObject<Item> register(String nameIn, Item itemIn)
	{
		return ITEMS.register(nameIn, () -> itemIn);
	}
	
	public static RegistryObject<Item> registerBlock(String nameIn, Block blockIn)
	{
		return registerBlock(nameIn, blockIn, VOItemGroup.BLOCKS);
	}
	
	public static RegistryObject<Item> registerBlock(String nameIn, Block blockIn, CreativeModeTab group)
	{
		return registerBlock(nameIn, new BlockItem(blockIn, new Item.Properties().tab(group)));
	}
	
	public static RegistryObject<Item> registerBlock(String nameIn, BlockItem itemIn)
	{
		return ITEMS.register(nameIn, () -> itemIn);
	}
	
	public static <T extends AbstractContainerMenu> MenuType<T> registerContainer(String nameIn, MenuType.MenuSupplier<T> factoryIn)
	{
		return (MenuType<T>)CONTAINERS.register(nameIn, () -> new MenuType<>(factoryIn)).get();
	}
    
	public static void init() { }
    
//    @SubscribeEvent
//    public static void onContainerRegistry(final RegistryEvent.Register<MenuType<?>> containerRegistryEvent)
//    {
//    	IForgeRegistry<MenuType<?>> registry = containerRegistryEvent.getRegistry();
//    	registry.registerAll(CONTAINERS.toArray(new MenuType<?>[0]));
//    	
//    	DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
//			ScreenManager.registerFactory(CONTAINER_WARG, GuiWarg::new);
//		});
//    	DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
//			ScreenManager.registerFactory(CONTAINER_BODY, GuiBody::new);
//		});
//    	DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
//			ScreenManager.registerFactory(CONTAINER_PLAYER_BODY, GuiPlayerBody::new);
//		});
//    }
}
