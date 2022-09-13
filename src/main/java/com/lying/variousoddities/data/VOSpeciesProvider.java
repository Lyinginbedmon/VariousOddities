package com.lying.variousoddities.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.SpeciesRegistry;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.IDataProvider;
import net.minecraft.resources.ResourceLocation;

public class VOSpeciesProvider implements DataProvider
{
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	private final DataGenerator dataGenerator;
	
	public VOSpeciesProvider(DataGenerator generatorIn)
	{
		this.dataGenerator = generatorIn;
	}
	
	public String getName(){ return "Various Oddities species"; }
	
	public void run(CachedOutput cache) throws IOException
	{
		Path path = this.dataGenerator.getOutputFolder();
		Map<ResourceLocation, Species> map = Maps.newHashMap();
		SpeciesRegistry.getDefaultSpecies().forEach((species) -> 
			{
				if(map.put(species.getRegistryName(), species) != null)
					throw new IllegalStateException("Duplicate species "+species.getRegistryName());
			});
		
		map.forEach((name, species) -> 
			{
				Path filePath = getPath(path, name);
				try
				{
					DataProvider.save(GSON, cache, species.toJson(), filePath);
				}
				catch(IOException e)
				{
					VariousOddities.log.error("Couldn't save species {}", filePath, e);
				}
			});
	}
	
	private static Path getPath(Path pathIn, ResourceLocation id)
	{
		return pathIn.resolve("data/"+id.getNamespace()+"/species/"+id.getPath()+".json");
	}
}
