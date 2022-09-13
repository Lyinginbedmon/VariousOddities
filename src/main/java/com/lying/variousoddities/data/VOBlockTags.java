package com.lying.variousoddities.data;

import javax.annotation.Nonnull;

import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;

public class VOBlockTags extends BlockTagsProvider
{
	public VOBlockTags(DataGenerator generatorIn, ExistingFileHelper existingFileHelper)
	{
		super(generatorIn, Reference.ModInfo.MOD_ID, existingFileHelper);
	}
	
	@Nonnull
	public String getName(){ return "Various Oddities block tags"; }
	
	@SuppressWarnings("unchecked")
	protected void registerTags()
	{
		tag(VOBlocks.UNPHASEABLE)
			.addTag(BlockTags.WITHER_IMMUNE)
			.add(Blocks.NETHER_PORTAL);
		tag(VOBlocks.GNAWABLE_HEALING)
			.add(Blocks.MELON, Blocks.PUMPKIN, Blocks.CARVED_PUMPKIN, Blocks.HAY_BLOCK, Blocks.CAKE, Blocks.WHEAT, Blocks.BEETROOTS, Blocks.CARROTS, Blocks.POTATOES);
		tag(VOBlocks.GNAWABLE)
			.addTags(VOBlocks.GNAWABLE_HEALING, BlockTags.PLANKS, BlockTags.WOODEN_SLABS, BlockTags.WOODEN_FENCES, BlockTags.WOODEN_DOORS, BlockTags.CROPS)
			.add(Blocks.MELON_STEM, Blocks.ATTACHED_MELON_STEM, Blocks.PUMPKIN_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
	}
}
