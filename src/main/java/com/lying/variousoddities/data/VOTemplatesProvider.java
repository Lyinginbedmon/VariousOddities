package com.lying.variousoddities.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.TemplateRegistry;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;

public class VOTemplatesProvider implements IDataProvider
{
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	private final DataGenerator dataGenerator;
	
	public VOTemplatesProvider(DataGenerator generatorIn)
	{
		this.dataGenerator = generatorIn;
	}
	
	public String getName(){ return "Various Oddities templates"; }
	
	public void act(DirectoryCache cache) throws IOException
	{
		Path path = this.dataGenerator.getOutputFolder();
		Map<ResourceLocation, Template> map = Maps.newHashMap();
		TemplateRegistry.getDefaultTemplates().forEach((template) -> 
			{
				if(map.put(template.getRegistryName(), template) != null)
					throw new IllegalStateException("Duplicate template "+template.getRegistryName());
			});
		
		map.forEach((name, template) -> 
			{
//				System.out.println("Template ID for "+name+": "+template.uuid().toString());
				Path filePath = getPath(path, name);
				try
				{
					IDataProvider.save(GSON, cache, template.toJson(), filePath);
				}
				catch(IOException e)
				{
					VariousOddities.log.error("Couldn't save template {}", filePath, e);
				}
			});
	}
	
	private static Path getPath(Path pathIn, ResourceLocation id)
	{
		return pathIn.resolve("data/"+id.getNamespace()+"/templates/"+id.getPath()+".json");
	}
}
