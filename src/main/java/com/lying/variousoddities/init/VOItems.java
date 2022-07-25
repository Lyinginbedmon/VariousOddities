package com.lying.variousoddities.init;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.client.gui.GuiBody;
import com.lying.variousoddities.client.gui.GuiPlayerBody;
import com.lying.variousoddities.client.gui.GuiWarg;
import com.lying.variousoddities.inventory.ContainerBody;
import com.lying.variousoddities.inventory.ContainerPlayerBody;
import com.lying.variousoddities.inventory.ContainerWarg;
import com.lying.variousoddities.item.ItemHeldFlag;
import com.lying.variousoddities.item.ItemMossBottle;
import com.lying.variousoddities.item.ItemPhylactery;
import com.lying.variousoddities.item.ItemSap;
import com.lying.variousoddities.item.ItemScrollRemaking;
import com.lying.variousoddities.item.ItemSpellContainer;
import com.lying.variousoddities.item.VOItemGroup;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.platform.ScreenManager;

import net.minecraft.inventory.container.ContainerType;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class VOItems
{
	private static final List<Item> ITEMS = Lists.newArrayList();
	private static final List<BlockItem> BLOCK_ITEMS = Lists.newArrayList();
	private static final List<ContainerType<?>> CONTAINERS = Lists.newArrayList();
	
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
	
	// Containers
	public static final ContainerType<ContainerWarg> CONTAINER_WARG	= registerContainer("warg_inventory", ContainerWarg::fromNetwork);
	public static final ContainerType<ContainerBody> CONTAINER_BODY	= registerContainer("body_inventory", ContainerBody::fromNetwork);
	public static final ContainerType<ContainerPlayerBody> CONTAINER_PLAYER_BODY	= registerContainer("player_body_inventory", ContainerPlayerBody::fromNetwork);
	
	public static Item register(String nameIn, Item itemIn)
	{
		ForgeRegistries.ITEMS.register(Reference.ModInfo.MOD_PREFIX+nameIn, itemIn);
		ITEMS.add(itemIn);
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
		ForgeRegistries.ITEMS.register(Reference.ModInfo.MOD_PREFIX+nameIn, itemIn);
		BLOCK_ITEMS.add(itemIn);
		return itemIn;
	}
	
	public static <T extends Container> ContainerType<T> registerContainer(String nameIn, IContainerFactory<T> factoryIn)
	{
		ContainerType<T> type = IForgeContainerType.create(factoryIn);
		type.setRegistryName(Reference.ModInfo.MOD_ID, nameIn);
		CONTAINERS.add(type);
		return type;
	}
	
    public static void init()
    {
    	ItemHeldFlag.registerSubItems();
    	ItemSpellContainer.registerSubItems(registry);
    }
    
    @SubscribeEvent
    public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> containerRegistryEvent)
    {
    	IForgeRegistry<ContainerType<?>> registry = containerRegistryEvent.getRegistry();
    	registry.registerAll(CONTAINERS.toArray(new ContainerType<?>[0]));
    	
    	DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			ScreenManager.registerFactory(CONTAINER_WARG, GuiWarg::new);
		});
    	DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			ScreenManager.registerFactory(CONTAINER_BODY, GuiBody::new);
		});
    	DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			ScreenManager.registerFactory(CONTAINER_PLAYER_BODY, GuiPlayerBody::new);
		});
    }
}
