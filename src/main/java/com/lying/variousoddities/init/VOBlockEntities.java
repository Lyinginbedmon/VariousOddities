package com.lying.variousoddities.init;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.tileentity.TileEntityDraftingTable;
import com.lying.variousoddities.tileentity.TileEntityEggKobold;
import com.lying.variousoddities.tileentity.TileEntityPhylactery;

import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class VOBlockEntities
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Reference.ModInfo.MOD_ID);
    
	public static final RegistryObject<BlockEntityType<TileEntityDraftingTable>> TABLE_DRAFTING = register("drafting_table", BlockEntityType.Builder.of(TileEntityDraftingTable::new, VOBlocks.TABLE_DRAFTING.get()));
	public static final RegistryObject<BlockEntityType<TileEntityEggKobold>> EGG_KOBOLD = register("kobold_egg", BlockEntityType.Builder.of(TileEntityEggKobold::new, VOBlocks.EGG_KOBOLD.get()));
	public static final RegistryObject<BlockEntityType<TileEntityPhylactery>> PHYLACTERY = register("phylactery", BlockEntityType.Builder.of(TileEntityPhylactery::new, VOBlocks.PHYLACTERY.get()));
    
    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, BlockEntityType.Builder<T> builder)
    {
        return BLOCK_ENTITIES.register(name, () -> builder.build(Util.fetchChoiceType(References.BLOCK_ENTITY, name)));
    }
}
