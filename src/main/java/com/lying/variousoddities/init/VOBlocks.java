package com.lying.variousoddities.init;

import com.lying.variousoddities.block.*;
import com.lying.variousoddities.block.VOBlock;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class VOBlocks
{
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.ModInfo.MOD_ID);
	
    public static final TagKey<Block> UNPHASEABLE = createTag("unphaseable");
	
	public static final Block TABLE_DRAFTING	= register("drafting_table", new BlockDraftingTable(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BLUE).instabreak()));
	public static final Block MOSS_BLOCK		= register("moss_block", new BlockMoss(BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_GREEN).noCollission().isViewBlocking(VOBlock::isntSolid)));
	public static final Block EGG_KOBOLD		= register("kobold_egg", new BlockEggKobold(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_YELLOW).strength(0.8F).requiresCorrectToolForDrops()));//harvestTool(ToolType.PICKAXE).harvestLevel(2)));
	public static final Block EGG_KOBOLD_INERT	= register("inert_kobold_egg", new BlockEggBase(BlockEggBase.SHAPE_SMALL, BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_YELLOW).strength(0.8F).requiresCorrectToolForDrops()));//harvestTool(ToolType.PICKAXE).harvestLevel(2)));
	public static final Block LAYER_SCALE		= register("scale_layer", new BlockLayerScale(BlockBehaviour.Properties.of(Material.GRASS, MaterialColor.TERRACOTTA_YELLOW).strength(0.3F)));
	public static final Block PHYLACTERY		= register("phylactery", new BlockPhylacteryLich(BlockBehaviour.Properties.of(Material.STONE)));
	public static final Block PHYLACTERY_EMPTY	= register("empty_phylactery", new BlockPhylacteryBase(BlockBehaviour.Properties.of(Material.STONE)));
	
	public static Block register(String nameIn, Block blockIn)
	{
		BLOCKS.register(nameIn, () -> blockIn);
		return blockIn;
	}
    
    private static TagKey<Block> createTag(String name)
    {
    	return BlockTags.create(new ResourceLocation(Reference.ModInfo.MOD_ID, name));
    }
}
