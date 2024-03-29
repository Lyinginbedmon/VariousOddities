package com.lying.variousoddities.config;

import java.util.HashMap;
import java.util.Map;

import com.lying.variousoddities.entity.NaturalSpawns;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.CreatureTypeDefaults;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.world.savedata.FactionManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigVO
{
	public static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
	
	public static final Client CLIENT = new Client(CLIENT_BUILDER);
	
	public static final General GENERAL = new General(SERVER_BUILDER);
	public static final Mobs MOBS = new Mobs(SERVER_BUILDER);
	
	public static class Client
	{
		public final ForgeConfigSpec.BooleanValue hideAbilities;
		public final ForgeConfigSpec.BooleanValue announceCools;
		public final ForgeConfigSpec.EnumValue<EnumCorner> abilityCorner;
		public final ForgeConfigSpec.EnumValue<EnumNameDisplay> nameDisplay;
		
		public final ForgeConfigSpec.BooleanValue holdKeyForMenu;
		
		public final ForgeConfigSpec.BooleanValue eternalPride;
		
		public Client(ForgeConfigSpec.Builder builder)
		{
			builder.push("hud");
				hideAbilities = builder.define("hide_abilities", false);
				announceCools = builder.define("announce_cooldown_finish", true);
				abilityCorner = builder.defineEnum("HUD_corner", EnumCorner.TOP_LEFT);
				nameDisplay = builder.defineEnum("name_display_style", EnumNameDisplay.CROPPED);
				holdKeyForMenu = builder.define("hold_key_for_ability_menu", false);
			builder.pop();
			
			builder.push("misc");
				eternalPride = builder.define("year-round Pride visuals", false);
			builder.pop();
		}
		
		public static enum EnumNameDisplay
		{
			FULL,
			CROPPED,
			SNEAKING;
		}
		
		public static enum EnumCorner
		{
			TOP_LEFT(SideX.LEFT, SideY.TOP, SideX.RIGHT),
			TOP_RIGHT(SideX.RIGHT, SideY.TOP, SideX.LEFT),
			BOTTOM_LEFT(SideX.LEFT, SideY.BOTTOM, SideX.RIGHT),
			BOTTOM_RIGHT(SideX.RIGHT, SideY.TOP, SideX.LEFT);
			
			public final SideX directionX;
			public final SideY directionY;
			public final SideX textSide;
			
			private EnumCorner(SideX dirX, SideY dirY, SideX text)
			{
				directionX = dirX;
				directionY = dirY;
				textSide = text;
			}
			
			public static enum SideX
			{
				LEFT,
				RIGHT;
			}
			
			public static enum SideY
			{
				TOP,
				BOTTOM;
			}
		}
	}
	
	public static void updateCache()
	{
		GENERAL.updateCache();
		MOBS.updateCache();
	}
	
	public static class General
	{
		private final ForgeConfigSpec.BooleanValue verboseLog;
		private final ForgeConfigSpec.EnumValue<EnumCorpseRule> spawnCorpses;
//		private final ForgeConfigSpec.BooleanValue zombiesZombifyPlayers;
		
		private final ForgeConfigSpec.DoubleValue bludgeoningCap;
		private final ForgeConfigSpec.IntValue bludgeoningTime;
		
		private boolean loaded = false;
		
		private boolean zombifyPlayers;
		
		public General(ForgeConfigSpec.Builder builder)
		{
			builder.push("general");
				verboseLog = builder.define("verboseLog", false);
				spawnCorpses = builder.defineEnum("spawn_corpses", EnumCorpseRule.NEEDLED_ONLY);
//				zombiesZombifyPlayers = builder.define("zombies_zombify_players", false);
				bludgeoningTime = builder.defineInRange("bludgeoning_recovery_rate_seconds", 60, 0, Integer.MAX_VALUE);
				bludgeoningCap = builder.defineInRange("bludgeoning_cap_above_max_health", 10, 0, Double.MAX_VALUE);
			builder.pop();
		}
		
		public void updateCache()
		{
			loaded = true;
			
//			if(zombiesZombifyPlayers != null)
//				zombifyPlayers = zombiesZombifyPlayers.get();
		}
		
		public boolean verboseLogs()
		{
			return verboseLog.get();
		}
		
		public EnumCorpseRule corpseSpawnRule()
		{
			return spawnCorpses.get();
		}
		
		public static enum EnumCorpseRule
		{
			ALWAYS,
			PLAYERS_ONLY,
			PLAYERS_AND_NEEDLED,
			NEEDLED_ONLY,
			NEVER;
		}
		
		public boolean zombifyPlayers()
		{
			if(!loaded)
				updateCache();
			return zombifyPlayers;
		}
		
		public int bludgeoningRecoveryRate()
		{
			if(!loaded)
				updateCache();
			return bludgeoningTime.get() * Reference.Values.TICKS_PER_SECOND;
		}
		
		public float bludgeoningCap()
		{
			if(!loaded)
				updateCache();
			return bludgeoningCap.get().floatValue();
		}
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
		
		public final TypeSettings typeSettings;
		public final FactionSettings factionSettings;
		
		public final ForgeConfigSpec.IntValue powerLevel;
		public final ForgeConfigSpec.BooleanValue createCharacterOnLogin;
		public final ForgeConfigSpec.BooleanValue randomCharacters;
		public final ForgeConfigSpec.BooleanValue newCharacterOnDeath;
		
		public Mobs(ForgeConfigSpec.Builder builder)
		{
			builder.push("mobs");
	        
			aiSettings = new AISettings(builder);
			spawnSettings = new SpawnSettings(builder);
			
			typeSettings = new TypeSettings(builder);
			factionSettings = new FactionSettings(builder);
			
			powerLevel = builder.comment("How powerful players can make their characters at creation").defineInRange("powerLevel", 3, 0, Integer.MAX_VALUE);
			createCharacterOnLogin = builder.comment("Open character creation when players first log-in").define("selectCharacterOnLogin", true);
			randomCharacters = builder.comment("Create a random character at character creation, instead of letting players choose").define("randomCharacter", false);
			newCharacterOnDeath = builder.comment("Reopen character creation on respawn after death").define("newCharacterOnDeath", false);
			
			builder.pop();
		}
		
		public void updateCache()
		{
			if(aiSettings != null)
			{
				aiSettings.updateCache();
				spawnSettings.updateCache();
				
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
			
			public boolean isOddityAIEnabled(EntityType<?> type){ return isOddityAIEnabled(type.getDescriptionId()); }
			public boolean isOddityAIEnabled(ResourceLocation registry){ return isOddityAIEnabled(registry.getPath()); }
			public boolean isOddityAIEnabled(String mobName)
			{
				return (optionalAICache ? oddityAICache.containsKey(mobName) && oddityAICache.get(mobName) : false);
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
			public boolean isOdditySpawnEnabled(EntityType<?> type){ return isOdditySpawnEnabled(type.getDescriptionId()); }
			public boolean isOdditySpawnEnabled(ResourceLocation registry){ return isOdditySpawnEnabled(registry.getPath()); }
			public boolean isOdditySpawnEnabled(String mobName)
			{
				if(naturalSpawnsCache)
				{
					if(odditySpawnsCache.containsKey(mobName))
						return odditySpawnsCache.get(mobName);
					else if(odditySpawns.containsKey(mobName))
					{
						boolean enabled = odditySpawns.get(mobName).get();
						odditySpawnsCache.put(mobName, enabled);
						return enabled;
					}
				}
				return false;
			}
		}
		
		public static class TypeSettings
		{
			private ForgeConfigSpec.BooleanValue typesMatter;
			private ForgeConfigSpec.BooleanValue chooseTypes;
			
			private Map<EnumCreatureType, ForgeConfigSpec.ConfigValue<String>> mobTypes = new HashMap<>();
			
			private boolean typesActive = true;
			private boolean typesScreen = false;
			
			public TypeSettings(ForgeConfigSpec.Builder builder)
			{
				builder.push("types");
					typesMatter = builder.comment("Setting this to FALSE will disable the effects of all types").define("Types Matter", true);
					
					chooseTypes = builder.comment("Open the type selection screen when a player first logs in").define("Choose Types on login", false);
					
					builder.push("Mobs");
				        for(EnumCreatureType type : EnumCreatureType.values())
			        		mobTypes.put(type, builder.define(type.name(), CreatureTypeDefaults.getMobDefaults(type)));
			        builder.pop();
				builder.pop();
			}
			
			public void updateCache()
			{
				if(typesMatter != null)
					typesActive = typesMatter.get();
				if(chooseTypes != null)
					typesScreen = chooseTypes.get();
			}
			
			public Map<EnumCreatureType, String[]> getMobTypes()
			{
				Map<EnumCreatureType, String[]> types = new HashMap<>();
				for(EnumCreatureType type : mobTypes.keySet())
					types.put(type, mobTypes.get(type).get().split(","));
				
				return types;
			}
			
			public boolean typesMatter(){ return typesActive; }
			
			public boolean typesScreen(){ return typesScreen; }
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
	
	public static final ForgeConfigSpec client_spec = CLIENT_BUILDER.build();
	public static final ForgeConfigSpec server_spec = SERVER_BUILDER.build();
}
