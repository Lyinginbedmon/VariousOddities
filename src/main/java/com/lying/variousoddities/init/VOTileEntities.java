package com.lying.variousoddities.init;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.tileentity.TileEntityDraftingTable;
import com.lying.variousoddities.tileentity.TileEntityEggKobold;
import com.lying.variousoddities.tileentity.TileEntityPhylactery;
import com.mojang.datafixers.types.Type;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class VOTileEntities
{
	public static final BlockEntityType<TileEntityDraftingTable> TABLE_DRAFTING = register("drafting_table", BlockEntityType.Builder.of(TileEntityDraftingTable::new, VOBlocks.TABLE_DRAFTING));
	public static final BlockEntityType<TileEntityEggKobold> EGG_KOBOLD = register("kobold_egg", BlockEntityType.Builder.of(TileEntityEggKobold::new, VOBlocks.EGG_KOBOLD));
	public static final BlockEntityType<TileEntityPhylactery> PHYLACTERY = register("phylactery", BlockEntityType.Builder.of(TileEntityPhylactery::new, VOBlocks.PHYLACTERY));
	
    @SubscribeEvent
	public static void registerTiles(final RegistryEvent.Register<BlockEntityType<?>> tileRegistryevent)
	{
    	IForgeRegistry<BlockEntityType<?>> registry = tileRegistryevent.getRegistry();
		register(registry, new ResourceLocation(Reference.ModInfo.MOD_ID, "kobold_egg"), EGG_KOBOLD);
		register(registry, new ResourceLocation(Reference.ModInfo.MOD_ID, "drafting_table"), TABLE_DRAFTING);
		register(registry, new ResourceLocation(Reference.ModInfo.MOD_ID, "phylactery"), PHYLACTERY);
	}
    
    private static void register(IForgeRegistry<BlockEntityType<?>> registry, ResourceLocation name, IForgeRegistryEntry<BlockEntityType<?>> tile)
    {
    	registry.register(tile.setRegistryName(name));
    }
    
    private static <T extends BlockEntity> BlockEntityType<T> register(String nameIn, BlockEntityType.Builder<T> builder)
    {
    	Type<?> type = Util.fetchChoiceType(References.BLOCK_ENTITY, nameIn);
    	return builder.build(type);
    }
}
