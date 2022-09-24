package com.lying.variousoddities.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Sets;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.TemplateRegistry;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

public class VOTemplatesProvider implements DataProvider
{
	private final DataGenerator.PathProvider pathProvider;
	protected ExistingFileHelper fileHelper;
	
	public VOTemplatesProvider(DataGenerator generatorIn, ExistingFileHelper fileHelperIn)
	{
		this.pathProvider = generatorIn.createPathProvider(DataGenerator.Target.DATA_PACK, "templates");
		this.fileHelper = fileHelperIn;
	}
	
	public String getName(){ return "Various Oddities templates"; }
	
	public void run(CachedOutput cache) throws IOException
	{
		Set<ResourceLocation> set = Sets.newHashSet();
		Consumer<Template> consumer = (template) ->
		{
			if(!set.add(template.getRegistryName()))
				throw new IllegalStateException("Duplicate template "+template.getRegistryName());
			else
			{
				Path path = this.pathProvider.json(template.getRegistryName());
				try
				{
					DataProvider.saveStable(cache, template.toJson(), path);
				}
				catch(IOException e)
				{
					VariousOddities.log.error("Couldn't save template {}", path, e);
				}
			}
		};
		
		TemplateRegistry.getDefaultTemplates().forEach(consumer);
	}
}
