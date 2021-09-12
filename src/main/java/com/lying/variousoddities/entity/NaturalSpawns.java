package com.lying.variousoddities.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.MobSpawnInfo.Spawners;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID)
public class NaturalSpawns
{
	public static Map<String, String[]> NATURAL_SPAWNS = new HashMap<>();
	public static Map<String, Boolean> DEFAULT_NATURAL = new HashMap<>();
	
	private static final List<Biome> spawnsLoaded = Lists.newArrayList();
	
	static
	{
		addSpawns(VOEntities.CRAB, true,
				"{ocean,10,4}",
				"{minecraft:beaches,10,4}",
				"{minecraft:deep_ocean,10,4}",
				"{minecraft:ocean,10,4}",
				"{minecraft:frozen_ocean,10,4}",
				"{biomesoplenty:bayou,10,4}",
				"{biomesoplenty:mangrove,10,4}",
				"{biomesoplenty:coral_reef,10,4}",
				"{biomesoplenty:kelp_forest,10,4}");
		addSpawns(VOEntities.CRAB_GIANT, true,
				"{ocean,10,1}",
				"{minecraft:beaches,10,1}",
				"{minecraft:deep_ocean,10,1}",
				"{minecraft:ocean,10,1}",
				"{minecraft:frozen_ocean,100,1}",
				"{biomesoplenty:bayou,1,1}",
				"{biomesoplenty:mangrove,1,1}",
				"{biomesoplenty:coral_reef,1,1}",
				"{biomesoplenty:kelp_forest,4,1}");
		addSpawns(VOEntities.GOBLIN, true,
				"{spooky:forest:overworld,80,6}",
				"{minecraft:dark_forest,80,6}", 
				"{biomesoplenty:bamboo_forest,60,6}", 
				"{biomesoplenty:bayou,60,6}", 
				"{biomesoplenty:land_of_lakes,60,6}", 
				"{biomesoplenty:redwood_forest,60,6}");
		addSpawns(VOEntities.KOBOLD, true,
				"{hot:dry:sandy:overworld,10,6}",
				"{minecraft:desert,10,6}",
				"{minecraft:desert_hills,10,6}",
				"{biomesoplenty:brushland,8,6}",
				"{biomesoplenty:lush_desert,15,6}",
				"{biomesoplenty:outback,20,6}");
		addSpawns(VOEntities.RAT, true,
				"{forest:overworld,10,6}",
				"{minecraft:swampland,20,6}",
				"{minecraft:swampland_mutated,20,6}",
				"{minecraft:dark_forest,20,6}",
				"{biomesoplenty:coniferous_forest,60,6}",
				"{biomesoplenty:dead_forest,60,6}",
				"{biomesoplenty:dead_swamp,40,6}",
				"{biomesoplenty:flower_field,20,6}",
				"{biomesoplenty:lavender_fields,20,6}",
				"{biomesoplenty:temperate_rainforest,40,6}",
				"{biomesoplenty:pasture,10,6}");
		addSpawns(VOEntities.RAT_GIANT, true,
				"{forest:overworld,80,1}",
				"{minecraft:swampland,100,1}",
				"{minecraft:swampland_mutated,80,1}",
				"{minecraft:dark_forest,100,1}", 
				"{biomesoplenty:coniferous_forest,60,1}",
				"{biomesoplenty:dead_forest,60,1}",
				"{biomesoplenty:dead_swamp,80,1}");
		addSpawns(VOEntities.SCORPION, true,
				"{hot:dry:sandy:overworld,10,3}",
				"{dry:mesa:overworld,10,3}",
				"{hot:wasteland:overworld,10,3}",
				"{minecraft:desert,10,3}",
				"{minecraft:desert_hills,10,3}",
				"{biomesoplenty:brushland,80,3}",
				"{biomesoplenty:outback,80,3}",
				"{biomesoplenty:wasteland,80,3}",
				"{biomesoplenty:xeric_shrubland,80,3}");
		addSpawns(VOEntities.SCORPION_GIANT, true,
				"{hot:dry:sandy,60,2}",
				"{dry:mesa,60,2}",
				"{hot:wasteland,60,2}",
				"{minecraft:desert,60,2}",
				"{minecraft:desert_hills,60,2}",
				"{biomesoplenty:brushland,40,2}",
				"{biomesoplenty:outback,80,2}",
				"{biomesoplenty:wasteland,40,2}",
				"{biomesoplenty:xeric_shrubland,60,2}");
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBiomeLoadEvent(BiomeLoadingEvent event)
	{
		if(!ConfigVO.MOBS.spawnSettings.odditySpawnsEnabled())
			return;
		
        if(event.getName() != null)
        {
            Biome biome = ForgeRegistries.BIOMES.getValue(event.getName());
            if(biome != null && !spawnsLoaded.contains(biome))
            {
            	List<EntityType<?>> added = new ArrayList<>();
            	for(EntityType<?> type : VOEntities.ENTITIES)
            	{
            		if(ConfigVO.MOBS.spawnSettings.isOdditySpawnEnabled(type))
            		{
                		String[] spawnData = NATURAL_SPAWNS.get(type.getRegistryName().getPath());
                		ConfiguredSpawns settings = new ConfiguredSpawns(spawnData);
                		if(settings.containsBiome(event.getName()))
                		{
                			Tuple<Integer, Integer> spawnValues = settings.getSettingsForBiome(event.getName());
                			if(spawnValues != null)
                			{
                    			added.add(type);
                				int weight = Math.max(1, spawnValues.getA());
                				int max = Math.max(1, spawnValues.getB());
                                event.getSpawns().getSpawner(type.getClassification()).add(new MobSpawnInfo.Spawners(type, weight, 1, max));
                			}
                		}
            		}
            	}
            	
            	if(!added.isEmpty() && ConfigVO.GENERAL.verboseLogs())
            	{
            		VariousOddities.log.info("Appended VO mob spawns to "+event.getName());
            		
            		VariousOddities.log.info("#    Added: ");
            		for(EntityType<?> type : added)
            			VariousOddities.log.info("#       "+type.getRegistryName());
            		
        			for(EntityClassification spawner : EntityClassification.values())
        			{
        				if(!event.getSpawns().getSpawner(spawner).isEmpty())
        				{
	        				VariousOddities.log.info("#    Spawns: "+spawner);
	            			for(Spawners spawn : event.getSpawns().getSpawner(spawner))
	            				VariousOddities.log.info("#       "+spawn.type.getRegistryName()+", "+spawn.itemWeight);
        				}
        			}
            	}
            	
            	spawnsLoaded.add(biome);
            }
        }
    }
	
    public static BiomeDictionary.Type getType(String name)
    {
        Map<String, BiomeDictionary.Type> byName = BiomeDictionary.Type.getAll().stream().collect(Collectors.toMap(BiomeDictionary.Type::getName, Function.identity()));
        name = name.toUpperCase();
        return byName.get(name);
    }
    
    public static BiomeDictionary.Type[] toBiomeTypeArray(List<? extends String> strings)
    {
        BiomeDictionary.Type[] types = new BiomeDictionary.Type[strings.size()];
        for (int i = 0; i < strings.size(); i++) {
            String string = strings.get(i);
            types[i] = getType(string);
        }
        return types;
    }
	
	private static void addSpawns(EntityType<?> entityClass, boolean shouldSpawn, String... spawns)
	{
		DEFAULT_NATURAL.put(entityClass.getRegistryName().getPath(), shouldSpawn);
		addSpawns(entityClass, spawns);
	}
    
	private static void addSpawns(EntityType<?> entityClass, String... spawns)
	{
		NATURAL_SPAWNS.put(entityClass.getRegistryName().getPath(), spawns);
	}
	
	public static class ConfiguredSpawns
	{
		private static final String REGEX = "^(?:\\{(?=.*\\})|(?!.*\\}$))(?<biome>[a-zA-Z0-9_:])+,(?<weight>\\d+),(?<size>\\d+)\\}?$";
		
		private static List<Type> BIOME_TYPES = new ArrayList<>();
		private final String[] settings;
		
		public ConfiguredSpawns(String... settingsIn)
		{
			List<String> set = new ArrayList<>();
			for(String setting : settingsIn)
			{
				setting = setting.replace(" ", "");
				if(Pattern.matches(REGEX, setting))
					set.add(setting);
				else if(ConfigVO.GENERAL.verboseLogs())
					VariousOddities.log.error("Malformed spawn setting: "+setting);
			}
			
			settings = set.toArray(new String[0]);
		}
		
		private static String getDataFromSetting(String settingIn)
		{
			return settingIn.substring(1, settingIn.length() - 1).replace(" ", "");
		}
		
		public boolean containsBiome(ResourceLocation biomeName)
		{
			for(String setting : this.settings)
			{
				String[] data = getDataFromSetting(setting).split(",");
				if(biomeMatches(biomeName, data[0]))
					return true;
			}
			return false;
		}
		
		/** Returns true if the given biome matches the given set of type values */
		private static boolean isValidBiome(ResourceLocation biomeName, String[] values)
		{
			String set = "";
			for(String val : values)
			{
				if(set.length() == 0)
					set += "[";
				else
					set += ":";
				set += val;
			}
			set += "]";
			for(String val : values)
			{
				boolean include = val.charAt(0) != '!';
				String typeName = val.replace("!", " "); 
				if(typeName.equalsIgnoreCase("minecraft") || typeName.equalsIgnoreCase("biomesoplenty"))
					return false;
				
				Type type = getTypeFromName(typeName);
				if(type != null)
				{
					boolean found = false;
					Set<RegistryKey<Biome>> validBiomes = BiomeDictionary.getBiomes(type);
					for(RegistryKey<Biome> biome : validBiomes)
						if(biome.getLocation().equals(biomeName))
						{
							found = true;
							break;
						}
					
					if(found != include)
						return false;
				}
				else
				{
					VariousOddities.log.error("Couldn't identify biome type "+val+" in spawn configuration of "+set);
					return false;
				}
			}
			return true;
		}
		
		public Tuple<Integer, Integer> getSettingsForBiome(ResourceLocation biomeName)
		{
			for(String setting : this.settings)
			{
				String[] data = getDataFromSetting(setting).split(",");
				if(biomeMatches(biomeName, data[0]))
					return new Tuple<Integer, Integer>(Integer.valueOf(data[1]), Integer.valueOf(data[2]));
			}
			return null;
		}
		
		private static boolean biomeMatches(ResourceLocation biomeName, String settingEntry)
		{
			// Case 1: Setting includes a direct biome name
			ResourceLocation targetName = null;
			if(settingEntry.contains(":"))
			{
				try
				{
					targetName = new ResourceLocation(settingEntry.toLowerCase());
				}
				catch(Exception e){ }
			}
			if(targetName != null && biomeName.equals(targetName))
				return true;
			
			// Case 2: Setting includes a set of one or more types of biome
			if(isValidBiome(biomeName, settingEntry.split(":")))
				return true;
			
			return false;
		}
		
		private static Type getTypeFromName(String name)
		{
			for(Type type : BiomeDictionary.Type.getAll())
				if(type.getName().equalsIgnoreCase(name))
					return type;
			return null;
		}
		
		private static void getAllTypes()
		{
			BIOME_TYPES.clear();
			
			for(Type type : BiomeDictionary.Type.getAll())
				if(!BIOME_TYPES.contains(type))
					BIOME_TYPES.add(type);
		}
		
		static
		{
			getAllTypes();
		}
	}
}
