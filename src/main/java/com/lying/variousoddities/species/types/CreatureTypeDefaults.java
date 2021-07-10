package com.lying.variousoddities.species.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.entity.EntityType;

public class CreatureTypeDefaults
{
	private static final Map<EnumCreatureType, String> typeToMobDefaults = new HashMap<>();
	private static final Map<EnumCreatureType, List<String>> patronTypes = new HashMap<>();
	
	public static boolean isTypedPatron(String name)
	{
		for(EnumCreatureType type : patronTypes.keySet())
			for(String player : patronTypes.get(type))
				if(player.equals(name))
					return true;
		return false;
	}
	
	public static List<EnumCreatureType> getPatronTypes(String name)
	{
		List<EnumCreatureType> types = Lists.newArrayList();
		for(EnumCreatureType type : patronTypes.keySet())
			for(String entry : patronTypes.get(type))
				if(entry.equalsIgnoreCase(name))
				{
					types.add(type);
					break;
				}
		return types;
	}
	
	public static String getMobDefaults(EnumCreatureType type)
	{
		return typeToMobDefaults.getOrDefault(type, "");
	}
	
	public static void addMobToTypeDefaults(EntityType<?> classIn, EnumCreatureType... groups)
	{
		String className = classIn.getRegistryName().toString();
		for(EnumCreatureType type : groups)
		{
			String entry = typeToMobDefaults.containsKey(type) ? typeToMobDefaults.get(type) : "";
			entry += (entry.length() > 0 ? "," : "") + className;
			typeToMobDefaults.put(type, entry);
		}
	}
	
	private static void addPatron(String playerName, EnumCreatureType... groups)
	{
		for(EnumCreatureType type : groups)
		{
			List<String> entries = patronTypes.get(type);
			if(entries == null)
				entries = Lists.newArrayList();
			
			if(!entries.contains(playerName))
				entries.add(playerName);
			patronTypes.put(type, entries);
		}
	}
	
	static
	{
		// Default mob settings
			// Vanilla mobs
		addMobToTypeDefaults(EntityType.CREEPER,			EnumCreatureType.PLANT, EnumCreatureType.EVIL);
		addMobToTypeDefaults(EntityType.ENDER_DRAGON,		EnumCreatureType.DRAGON, EnumCreatureType.EXTRAPLANAR);
		addMobToTypeDefaults(EntityType.WITHER,				EnumCreatureType.UNDEAD);
		addMobToTypeDefaults(EntityType.ELDER_GUARDIAN,		EnumCreatureType.ABERRATION, EnumCreatureType.AQUATIC, EnumCreatureType.AMPHIBIOUS);
		addMobToTypeDefaults(EntityType.GUARDIAN,			EnumCreatureType.ABERRATION, EnumCreatureType.AQUATIC, EnumCreatureType.AMPHIBIOUS);
		addMobToTypeDefaults(EntityType.ENDERMAN,			EnumCreatureType.OUTSIDER, EnumCreatureType.EXTRAPLANAR);
		addMobToTypeDefaults(EntityType.GHAST,				EnumCreatureType.OUTSIDER, EnumCreatureType.EXTRAPLANAR);
		addMobToTypeDefaults(EntityType.HUSK,				EnumCreatureType.UNDEAD);
		addMobToTypeDefaults(EntityType.DROWNED,			EnumCreatureType.UNDEAD, EnumCreatureType.AQUATIC);
		addMobToTypeDefaults(EntityType.ZOMBIE,				EnumCreatureType.UNDEAD);
		addMobToTypeDefaults(EntityType.ZOMBIE_HORSE,		EnumCreatureType.UNDEAD);
		addMobToTypeDefaults(EntityType.ZOMBIE_VILLAGER,	EnumCreatureType.UNDEAD);
		addMobToTypeDefaults(EntityType.ZOMBIFIED_PIGLIN,	EnumCreatureType.UNDEAD);
		addMobToTypeDefaults(EntityType.ZOGLIN,				EnumCreatureType.UNDEAD);
		addMobToTypeDefaults(EntityType.SKELETON,			EnumCreatureType.UNDEAD);
		addMobToTypeDefaults(EntityType.WITHER_SKELETON,	EnumCreatureType.UNDEAD);
		addMobToTypeDefaults(EntityType.STRAY,				EnumCreatureType.UNDEAD);
		addMobToTypeDefaults(EntityType.SKELETON_HORSE,		EnumCreatureType.UNDEAD);
		addMobToTypeDefaults(EntityType.BEE,				EnumCreatureType.VERMIN);
		addMobToTypeDefaults(EntityType.SPIDER,				EnumCreatureType.VERMIN);
		addMobToTypeDefaults(EntityType.CAVE_SPIDER,		EnumCreatureType.VERMIN);
		addMobToTypeDefaults(EntityType.SILVERFISH,			EnumCreatureType.VERMIN);
		addMobToTypeDefaults(EntityType.ENDERMITE,			EnumCreatureType.VERMIN, EnumCreatureType.EXTRAPLANAR);
		addMobToTypeDefaults(EntityType.SNOW_GOLEM,			EnumCreatureType.CONSTRUCT, EnumCreatureType.COLD);
		addMobToTypeDefaults(EntityType.IRON_GOLEM,			EnumCreatureType.CONSTRUCT);
		addMobToTypeDefaults(EntityType.SHULKER,			EnumCreatureType.CONSTRUCT, EnumCreatureType.EXTRAPLANAR);
		addMobToTypeDefaults(EntityType.BLAZE,				EnumCreatureType.ELEMENTAL, EnumCreatureType.FIRE, EnumCreatureType.EXTRAPLANAR);
		addMobToTypeDefaults(EntityType.VEX,				EnumCreatureType.FEY);
		addMobToTypeDefaults(EntityType.SLIME,				EnumCreatureType.OOZE);
		addMobToTypeDefaults(EntityType.MAGMA_CUBE,			EnumCreatureType.OOZE, EnumCreatureType.FIRE, EnumCreatureType.EXTRAPLANAR);
		addMobToTypeDefaults(EntityType.STRIDER,			EnumCreatureType.MAGICAL_BEAST, EnumCreatureType.FIRE, EnumCreatureType.EXTRAPLANAR);
		addMobToTypeDefaults(EntityType.PLAYER,				EnumCreatureType.HUMANOID);
		addMobToTypeDefaults(EntityType.WITCH,				EnumCreatureType.HUMANOID);
		addMobToTypeDefaults(EntityType.VILLAGER,			EnumCreatureType.HUMANOID);
		addMobToTypeDefaults(EntityType.WANDERING_TRADER,	EnumCreatureType.HUMANOID);
		addMobToTypeDefaults(EntityType.PILLAGER,			EnumCreatureType.HUMANOID);
		addMobToTypeDefaults(EntityType.VINDICATOR,			EnumCreatureType.HUMANOID);
		addMobToTypeDefaults(EntityType.EVOKER,				EnumCreatureType.HUMANOID);
		addMobToTypeDefaults(EntityType.PIGLIN,				EnumCreatureType.HUMANOID, EnumCreatureType.EXTRAPLANAR);
		addMobToTypeDefaults(EntityType.field_242287_aj,	EnumCreatureType.HUMANOID, EnumCreatureType.EXTRAPLANAR);
		addMobToTypeDefaults(EntityType.RAVAGER,			EnumCreatureType.MAGICAL_BEAST);
		addMobToTypeDefaults(EntityType.SQUID,				EnumCreatureType.ANIMAL,EnumCreatureType.AQUATIC);
		addMobToTypeDefaults(EntityType.COW,				EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.PIG,				EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.SHEEP,				EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.LLAMA,				EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.HORSE,				EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.DONKEY,				EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.CAT,				EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.OCELOT,				EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.FOX,				EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.WOLF,				EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.CHICKEN,			EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.BAT,				EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.PARROT,				EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.RABBIT,				EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.POLAR_BEAR,			EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.PANDA,				EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.TURTLE,				EnumCreatureType.ANIMAL);
		addMobToTypeDefaults(EntityType.DOLPHIN,			EnumCreatureType.ANIMAL, EnumCreatureType.AQUATIC);
		addMobToTypeDefaults(EntityType.HOGLIN,				EnumCreatureType.ANIMAL, EnumCreatureType.EXTRAPLANAR);
		addMobToTypeDefaults(EntityType.PHANTOM,			EnumCreatureType.UNDEAD, EnumCreatureType.EVIL);
			// Oddities
		addMobToTypeDefaults(VOEntities.GHASTLING,			EnumCreatureType.OUTSIDER, EnumCreatureType.EXTRAPLANAR);
		addMobToTypeDefaults(VOEntities.GOBLIN,				EnumCreatureType.HUMANOID, EnumCreatureType.GOBLIN);
		addMobToTypeDefaults(VOEntities.KOBOLD,				EnumCreatureType.HUMANOID, EnumCreatureType.REPTILE);
		addMobToTypeDefaults(VOEntities.RAT,				EnumCreatureType.VERMIN);
		addMobToTypeDefaults(VOEntities.RAT_GIANT,			EnumCreatureType.VERMIN);
		addMobToTypeDefaults(VOEntities.SCORPION,			EnumCreatureType.VERMIN);
		addMobToTypeDefaults(VOEntities.SCORPION_GIANT,		EnumCreatureType.VERMIN);
		
		// Individual player settings (added on first login)
		addPatron("_Lying", 					EnumCreatureType.FEY, EnumCreatureType.HOLY);
		addPatron("_Booked",					EnumCreatureType.FEY, EnumCreatureType.AQUATIC);
		addPatron("Alantor6616",				EnumCreatureType.CONSTRUCT, EnumCreatureType.EARTH, EnumCreatureType.FIRE);
		addPatron("chmobian",					EnumCreatureType.MONSTROUS_HUMANOID, EnumCreatureType.EARTH, EnumCreatureType.EXTRAPLANAR);
		addPatron("Dusty21134",					EnumCreatureType.DRAGON, EnumCreatureType.AIR, EnumCreatureType.EXTRAPLANAR);
		addPatron("Kurloz_M",					EnumCreatureType.PLANT, EnumCreatureType.EARTH);
		addPatron("Kyofushin",					EnumCreatureType.UNDEAD, EnumCreatureType.FEY);
		addPatron("nikmat97",					EnumCreatureType.MAGICAL_BEAST, EnumCreatureType.SHAPECHANGER);
		addPatron("Princessfirefly9",			EnumCreatureType.FEY, EnumCreatureType.AIR);
		addPatron("Pyrodance",					EnumCreatureType.FEY, EnumCreatureType.SHAPECHANGER);
		addPatron("SakuraWolfe",				EnumCreatureType.MAGICAL_BEAST, EnumCreatureType.AIR, EnumCreatureType.EXTRAPLANAR);
		addPatron("sawtooth44",					EnumCreatureType.ELEMENTAL, EnumCreatureType.AIR, EnumCreatureType.FIRE, EnumCreatureType.EXTRAPLANAR);
		addPatron("thefeywilds",				EnumCreatureType.OUTSIDER, EnumCreatureType.AIR, EnumCreatureType.EXTRAPLANAR, EnumCreatureType.WATER);
		addPatron("Wolframstein",				EnumCreatureType.MONSTROUS_HUMANOID, EnumCreatureType.AUGMENTED);
	}
}