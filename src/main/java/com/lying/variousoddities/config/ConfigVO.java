package com.lying.variousoddities.config;

import java.util.HashMap;
import java.util.Map;

import com.lying.variousoddities.entity.NaturalSpawns;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.types.CreatureTypeDefaults;
import com.lying.variousoddities.types.EnumCreatureType;
import com.lying.variousoddities.world.savedata.FactionManager;

import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigVO
{
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	
	public static final General GENERAL = new General(BUILDER);
	public static final Mobs MOBS = new Mobs(BUILDER);
	
	public static void updateCache()
	{
		GENERAL.updateCache();
		MOBS.updateCache();
	}
	
	public static class General
	{
		private final ForgeConfigSpec.BooleanValue verboseLog;
		
		private boolean verbose = false;
		
		public General(ForgeConfigSpec.Builder builder)
		{
			builder.push("general");
			verboseLog = builder.define("verboseLog", false);
			builder.pop();
		}
		
		public void updateCache()
		{
			if(verboseLog != null)
				verbose = verboseLog.get();
		}
		
		public boolean verboseLogs(){ return verbose; }
	}
	
	
	public static class Magic
	{
		public static int minSpellLevel, maxSpellLevel;
		public static String[] forbiddenSpells = new String[0];
		
		public static boolean isSpellForbidden(String spellIn)
		{
			for(String spell : forbiddenSpells)
				if(spellIn.equalsIgnoreCase(spell))
					return true;
			return false;
		}
	}
	
	public static class Mobs
	{
		public final AISettings aiSettings;
		/** Natural spawning toggles and other values */
		public final SpawnSettings spawnSettings;
		/** Natural spawning controls (biomes, weights, etc.) */
		public final SpawnConfig spawnConfig;
		
		public final TypeSettings typeSettings;
		public final FactionSettings factionSettings;
		
		public Mobs(ForgeConfigSpec.Builder builder)
		{
			builder.push("mobs");
	        
			aiSettings = new AISettings(builder);
			spawnSettings = new SpawnSettings(builder);
			spawnConfig = new SpawnConfig(builder);
			
			typeSettings = new TypeSettings(builder);
			factionSettings = new FactionSettings(builder);
			
			builder.pop();
		}
		
		public void updateCache()
		{
			if(aiSettings != null)
			{
				aiSettings.updateCache();
				spawnSettings.updateCache();
				spawnConfig.updateCache();
				
				typeSettings.updateCache();
				factionSettings.updateCache();
			}
		}
		
		public static class AISettings
		{
			/** Global optional AI control */
			private final ForgeConfigSpec.BooleanValue optionalAI;
			/** Individual optional AI control */
			private final Map<String, ForgeConfigSpec.BooleanValue> oddityAI = new HashMap<>();
			
			private boolean optionalAICache = true;
			private Map<String, Boolean> oddityAICache = new HashMap<>();
			
			public AISettings(ForgeConfigSpec.Builder builder)
			{
				builder.push("AI control");
					optionalAI = builder.comment("Enables the more destructive AI behaviours of many Oddities").worldRestart().define("optionalAI", true);
					
					builder.push("individual AI control");
				        for(ResourceLocation entry : VOEntities.getEntityAINameList())
				        {
				        	oddityAI.put(entry.getPath(), builder.define(entry.getPath(), true));
				        	oddityAICache.put(entry.getPath(), true);
				        }
			        builder.pop();
				builder.pop();
			}
			
			public void updateCache()
			{
				if(optionalAI != null)
				{
					optionalAICache = optionalAI.get();
					
					for(String oddity : oddityAI.keySet())
						oddityAICache.put(oddity, oddityAI.get(oddity).get());
				}
			}
			
			public boolean isOddityAIEnabled(EntityType<?> type){ return isOddityAIEnabled(type.getRegistryName()); }
			public boolean isOddityAIEnabled(ResourceLocation registry){ return isOddityAIEnabled(registry.getPath()); }
			public boolean isOddityAIEnabled(String mobName)
			{
				return (optionalAICache ? oddityAICache.containsKey(mobName) && oddityAICache.get(mobName) : false);
			}
		}
		
		public static class SpawnConfig
		{
			public Map<String, ForgeConfigSpec.ConfigValue<String>> entries = new HashMap<>();
			
			public Map<String, String> entryCache = new HashMap<>();
			
			public SpawnConfig(ForgeConfigSpec.Builder builder)
			{
				builder.push("spawn_config");
		        for(ResourceLocation entry : VOEntities.getEntityNameList())
		        {
		        	String entryName = entry.getPath();
		        	if(NaturalSpawns.NATURAL_SPAWNS.containsKey(entryName))
		        	{
		        		String compound = "";
		        		for(String str : NaturalSpawns.NATURAL_SPAWNS.get(entryName))
		        		{
		        			if(compound.length() > 0)
		        				compound += ",";
		        			compound += str;
		        		}
		        		entries.put(entryName, builder.worldRestart().define(entryName, compound));
		        	}
		        }
		        builder.pop();
			}
			
			public void updateCache()
			{
				if(!entries.isEmpty())
					for(String entry : entries.keySet())
						entryCache.put(entry, entries.get(entry).get());
			}
			
			public String[] getSpawnsFor(String name)
			{
				if(entryCache.containsKey(name))
				{
					String entry = entryCache.get(name);
					String[] split = entry.split("},");
					for(int i=0; i<split.length; i++)
					{
						if(!split[i].endsWith("}"))
							split[i] = split[i] + "}";
					}
					return split;
				}
				return null;
			}
		}
		
		public static class SpawnSettings
		{
			/** True if any oddity is allowed to spawn naturally */
			private ForgeConfigSpec.BooleanValue naturalSpawns;
			/** Specific true/false enabling for individual oddities */
			private Map<String, ForgeConfigSpec.ConfigValue<Boolean>> odditySpawns = new HashMap<>();
			
			private boolean naturalSpawnsCache = true;
			private Map<String, Boolean> odditySpawnsCache = new HashMap<>();
			
			public SpawnSettings(ForgeConfigSpec.Builder builder)
			{
				builder.push("spawn_switches");
				naturalSpawns = builder.comment("Setting this to FALSE will prevent all Oddities from spawning naturally, which is more ideal for adventure maps and the like").worldRestart().define("naturalSpawns", true);
				
				builder.push("individual spawn switches");
		        for(ResourceLocation entry : VOEntities.getEntityNameList())
		        {
		        	String entryName = entry.getPath();
		        	if(NaturalSpawns.NATURAL_SPAWNS.containsKey(entryName))
		        		odditySpawns.put(entryName, builder.define(entryName, NaturalSpawns.DEFAULT_NATURAL.containsKey(entryName) ? NaturalSpawns.DEFAULT_NATURAL.get(entryName) : Boolean.TRUE));
		        }
		        builder.pop();
		        
		        builder.pop();
			}
			
			public void updateCache()
			{
				if(naturalSpawns != null)
				{
					naturalSpawnsCache = naturalSpawns.get();
					
					for(String oddity : odditySpawns.keySet())
						odditySpawnsCache.put(oddity, odditySpawns.get(oddity).get());
				}
			}
			
			public boolean odditySpawnsEnabled(){ return naturalSpawnsCache; }
			public boolean isOdditySpawnEnabled(EntityType<?> type){ return isOdditySpawnEnabled(type.getRegistryName()); }
			public boolean isOdditySpawnEnabled(ResourceLocation registry){ return isOdditySpawnEnabled(registry.getPath()); }
			public boolean isOdditySpawnEnabled(String mobName){ return (naturalSpawnsCache ? odditySpawnsCache.containsKey(mobName) && odditySpawnsCache.get(mobName) : false);	}
		}
		
		public static class TypeSettings
		{
			private ForgeConfigSpec.BooleanValue typesMatter;
			
			private Map<EnumCreatureType, ForgeConfigSpec.ConfigValue<String>> mobTypes = new HashMap<>();
			private Map<EnumCreatureType, ForgeConfigSpec.ConfigValue<String>> playerTypes = new HashMap<>();
			
			private boolean typesActive = true;
			
			public TypeSettings(ForgeConfigSpec.Builder builder)
			{
				builder.push("types");
				
				typesMatter = builder.comment("Setting this to FALSE will disable the effects of all types").define("Types Matter", true);
				
				builder.push("Mobs");
			        for(EnumCreatureType type : EnumCreatureType.values())
		        		mobTypes.put(type, builder.define(type.name(), CreatureTypeDefaults.getMobDefaults(type)));
		        builder.pop();
				
		        builder.push("Players");
			        for(EnumCreatureType type : EnumCreatureType.values())
			        	playerTypes.put(type, builder.define(type.name(), CreatureTypeDefaults.getPlayerDefaults(type)));
		        builder.pop();
		        
				builder.pop();
			}
			
			public void updateCache()
			{
				if(typesMatter != null)
					typesActive = typesMatter.get();
			}
			
			public Map<EnumCreatureType, String[]> getMobTypes()
			{
				Map<EnumCreatureType, String[]> types = new HashMap<>();
				for(EnumCreatureType type : mobTypes.keySet())
					types.put(type, mobTypes.get(type).get().split(","));
				
				return types;
			}
			
			public Map<EnumCreatureType, String[]> getPlayerTypes()
			{
				Map<EnumCreatureType, String[]> types = new HashMap<>();
				for(EnumCreatureType type : playerTypes.keySet())
					types.put(type, playerTypes.get(type).get().split(","));
				
				return types;
			}
			
			public boolean typesMatter(){ return typesActive; }
		}
		
		public static class FactionSettings
		{
			private final ForgeConfigSpec.BooleanValue reputationChanges;
			private final ForgeConfigSpec.ConfigValue<String> factionDefaults;
			
			private boolean repChanges = true;
			
			public FactionSettings(ForgeConfigSpec.Builder builder)
			{
				builder.push("factions");
				reputationChanges = builder.define("reputationChanges", true);
				factionDefaults = builder.define("defaults", FactionManager.defaultsToString());
				builder.pop();
			}
			
			public void updateCache()
			{
				if(reputationChanges != null)
					repChanges = reputationChanges.get();
			}
			
			public boolean factionsInConfig()
			{
				return factionString() != null && factionString().length() > 0;
			}
			
			public boolean repChanges(){ return repChanges; }
			public String factionString(){ return factionDefaults.get(); }
		}
	}
	
	public static final ForgeConfigSpec spec = BUILDER.build();
}
