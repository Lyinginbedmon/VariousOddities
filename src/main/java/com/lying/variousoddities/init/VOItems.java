package com.lying.variousoddities.init;

import com.lying.variousoddities.inventory.ContainerBody;
import com.lying.variousoddities.inventory.ContainerPlayerBody;
import com.lying.variousoddities.inventory.ContainerWarg;
import com.lying.variousoddities.item.ItemMossBottle;
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

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class VOItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.ModInfo.MOD_ID);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Reference.ModInfo.MOD_ID);
    
	// Debug
	public static final Item SAP	= register("sap", new ItemSap(new Item.Properties().tab(VOItemGroup.LOOT)));
	
	// Mob module
	public static final Item SCALE_KOBOLD	= register("kobold_scale", new Item(new Item.Properties().tab(VOItemGroup.LOOT)));
	public static final Item MOSS_BOTTLE	= register("moss_bottle", new ItemMossBottle(new Item.Properties().tab(VOItemGroup.LOOT)));
	
	// Consumables
	public static final Item SCROLL_SPECIES	= register("remaking_scroll", new ItemScrollRemaking(false, new Item.Properties().tab(VOItemGroup.LOOT)));
	public static final Item SCROLL_RANDOM_SPECIES	= register("corrupted_remaking_scroll", new ItemScrollRemaking(true, new Item.Properties().tab(VOItemGroup.LOOT)));
	
	// Block items
	public static final BlockItem DRAFTING_TABLE	= registerBlock("drafting_table", VOBlocks.TABLE_DRAFTING, VOItemGroup.LOOT);
	public static final BlockItem EGG_KOBOLD		= registerBlock("kobold_egg", VOBlocks.EGG_KOBOLD);
	public static final BlockItem EGG_KOBOLD_INERT	= registerBlock("inert_kobold_egg", VOBlocks.EGG_KOBOLD_INERT);
	public static final BlockItem MOSS_BLOCK		= registerBlock("moss_block", VOBlocks.MOSS_BLOCK);
	public static final BlockItem LAYER_SCALE		= registerBlock("scale_layer", VOBlocks.LAYER_SCALE);
	public static final BlockItem PHYLACTERY		= registerBlock("phylactery", new ItemPhylactery(new Item.Properties().tab(VOItemGroup.BLOCKS).stacksTo(1)));
	public static final BlockItem PHYLACTERY_EMPTY	= registerBlock("empty_phylactery", new BlockItem(VOBlocks.PHYLACTERY_EMPTY, new Item.Properties().tab(VOItemGroup.BLOCKS).stacksTo(1)));
	
	// Magic items
	public static final Item SPELL_LIST		= register("spell_list", new ItemSpellList(new Properties().tab(VOItemGroup.LOOT)));
	public static final Item SPELL_SCROLL	= register("spell_scroll", new ItemSpellScroll(new Properties().tab(VOItemGroup.LOOT)));
	
	// Containers
	public static final MenuType<ContainerWarg> CONTAINER_WARG	= registerContainer("warg_inventory", ContainerWarg::fromNetwork);
	public static final MenuType<ContainerBody> CONTAINER_BODY	= registerContainer("body_inventory", ContainerBody::fromNetwork);
	public static final MenuType<ContainerPlayerBody> CONTAINER_PLAYER_BODY	= registerContainer("player_body_inventory", ContainerPlayerBody::fromNetwork);
	
	public static Item register(String nameIn, Item itemIn)
	{
		ITEMS.register(nameIn, () -> itemIn);
		return itemIn;
	}
	
	public static BlockItem registerBlock(String nameIn, Block blockIn)
	{
		return registerBlock(nameIn, blockIn, VOItemGroup.BLOCKS);
	}
	
	public static BlockItem registerBlock(String nameIn, Block blockIn, CreativeModeTab group)
	{
		return registerBlock(nameIn, new BlockItem(blockIn, new Item.Properties().tab(group)));
	}
	
	public static BlockItem registerBlock(String nameIn, BlockItem itemIn)
	{
		return (BlockItem)register(nameIn, itemIn);
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
