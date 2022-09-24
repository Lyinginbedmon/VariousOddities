package com.lying.variousoddities.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

public class VODataGenerators
{
	public static void onGatherData(GatherDataEvent event)
	{
		DataGenerator generator = event.getGenerator();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		generator.addProvider(event.includeServer(), new VOItemTags(generator, existingFileHelper));
		generator.addProvider(event.includeServer(), new VOBlockTags(generator, existingFileHelper));
		generator.addProvider(event.includeServer(), new VOEntityTags(generator, existingFileHelper));
		generator.addProvider(event.includeServer(), new VOSpeciesProvider(generator, existingFileHelper));
		generator.addProvider(event.includeServer(), new VOTemplatesProvider(generator, existingFileHelper));
		generator.addProvider(event.includeServer(), new VOLootProvider(generator, existingFileHelper));
		generator.addProvider(event.includeServer(), new VORecipeProvider(generator));
	}
}
