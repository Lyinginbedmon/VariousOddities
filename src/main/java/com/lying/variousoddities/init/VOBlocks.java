package com.lying.variousoddities.init;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.block.*;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class VOBlocks
{
	private static final List<Block> BLOCKS = new ArrayList<>();
	
	public static final Block TABLE_DRAFTING	= register("drafting_table", new BlockDraftingTable(AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BLUE_TERRACOTTA).zeroHardnessAndResistance()));
	public static final Block MOSS_BLOCK		= register("moss_block", new BlockMoss(AbstractBlock.Properties.create(Material.PLANTS, MaterialColor.GREEN).notSolid().setOpaque(VOBlock::isntSolid)));
	public static final Block EGG_KOBOLD		= register("kobold_egg", new BlockEggKobold(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.YELLOW_TERRACOTTA).hardnessAndResistance(0.8F).setRequiresTool().harvestTool(ToolType.PICKAXE).harvestLevel(2)));
	public static final Block EGG_KOBOLD_INERT	= register("inert_kobold_egg", new BlockEggBase(BlockEggBase.SHAPE_SMALL, AbstractBlock.Properties.create(Material.ROCK, MaterialColor.YELLOW_TERRACOTTA).hardnessAndResistance(0.8F).setRequiresTool().harvestTool(ToolType.PICKAXE).harvestLevel(2)));
	public static final Block LAYER_SCALE		= register("scale_layer", new BlockLayerScale(AbstractBlock.Properties.create(Material.SEA_GRASS, MaterialColor.YELLOW_TERRACOTTA).hardnessAndResistance(0.3F)));
	
	public static Block register(String nameIn, Block blockIn)
	{
		blockIn.setRegistryName(Reference.ModInfo.MOD_PREFIX+nameIn);
		BLOCKS.add(blockIn);
		return blockIn;
	}
	
    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent)
    {
    	blockRegistryEvent.getRegistry().registerAll(BLOCKS.toArray(new Block[0]));
    }
}
