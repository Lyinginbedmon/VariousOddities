package com.lying.variousoddities.init;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.tileentity.*;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class VOTileEntities
{
	public static final TileEntityType<TileEntityDraftingTable> TABLE_DRAFTING = TileEntityType.Builder.create(TileEntityDraftingTable::new, VOBlocks.TABLE_DRAFTING).build(null);
	public static final TileEntityType<TileEntityEggKobold> EGG_KOBOLD = TileEntityType.Builder.create(TileEntityEggKobold::new, VOBlocks.EGG_KOBOLD).build(null);
	
    @SubscribeEvent
	public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> tileRegistryevent)
	{
    	IForgeRegistry<TileEntityType<?>> registry = tileRegistryevent.getRegistry();
		register(registry, new ResourceLocation(Reference.ModInfo.MOD_ID, "kobold_egg"), EGG_KOBOLD);
		register(registry, new ResourceLocation(Reference.ModInfo.MOD_ID, "drafting_table"), TABLE_DRAFTING);
	}
    
    private static void register(IForgeRegistry<TileEntityType<?>> registry, ResourceLocation name, IForgeRegistryEntry<TileEntityType<?>> tile)
    {
    	registry.register(tile.setRegistryName(name));
    }
}
