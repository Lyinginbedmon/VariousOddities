package com.lying.variousoddities.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

public class VODataGenerators
{
	public static void onGatherData(GatherDataEvent event)
	{
		DataGenerator generator = event.getGenerator();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		if(event.includeServer())
		{
			generator.addProvider(new VOItemTags(generator, existingFileHelper));
    		generator.addProvider(new VOBlockTags(generator, existingFileHelper));
    		generator.addProvider(new VOEntityTags(generator, existingFileHelper));
			generator.addProvider(new VOSpeciesProvider(generator));
			generator.addProvider(new VOTemplatesProvider(generator));
			generator.addProvider(new VOLootProvider(generator, existingFileHelper));
		}
		if(event.includeClient())
		{
			;
		}
	}
}
