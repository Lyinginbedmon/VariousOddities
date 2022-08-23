package com.lying.variousoddities.data;

import javax.annotation.Nonnull;

import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
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
	
	protected void registerTags()
	{
		getOrCreateBuilder(VOBlocks.UNPHASEABLE)
			.add(
					Blocks.BEDROCK, Blocks.BARRIER,
					Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK,
					Blocks.NETHER_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.END_PORTAL, Blocks.END_GATEWAY);
	}
}
