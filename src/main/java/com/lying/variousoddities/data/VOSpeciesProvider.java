package com.lying.variousoddities.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Sets;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.SpeciesRegistry;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

public class VOSpeciesProvider implements DataProvider
{
	private final DataGenerator.PathProvider pathProvider;
	protected ExistingFileHelper fileHelper;
	
	public VOSpeciesProvider(DataGenerator generatorIn, ExistingFileHelper fileHelperIn)
	{
		this.pathProvider = generatorIn.createPathProvider(DataGenerator.Target.DATA_PACK, "templates");
		this.fileHelper = fileHelperIn;
	}
	
	public String getName(){ return "Various Oddities species"; }
	
	public void run(CachedOutput cache) throws IOException
	{
		Set<ResourceLocation> set = Sets.newHashSet();
		Consumer<Species> consumer = (species) ->
		{
			if(!set.add(species.getRegistryName()))
				throw new IllegalStateException("Duplicate species "+species.getRegistryName());
			else
			{
				Path path = this.pathProvider.json(species.getRegistryName());
				try
				{
					DataProvider.saveStable(cache, species.toJson(), path);
				}
				catch(IOException e)
				{
					VariousOddities.log.error("Couldn't save species {}", path, e);
				}
			}
		};
		
		SpeciesRegistry.getDefaultSpecies().forEach(consumer);
	}
}
