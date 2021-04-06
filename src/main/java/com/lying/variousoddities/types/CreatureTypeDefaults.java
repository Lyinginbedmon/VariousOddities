package com.lying.variousoddities.types;

import java.util.HashMap;
import java.util.Map;

import com.lying.variousoddities.init.VOEntities;

import net.minecraft.entity.EntityType;

public class CreatureTypeDefaults
{
	private static final Map<EnumCreatureType, String> typeToMobDefaults = new HashMap<>();
	private static final Map<EnumCreatureType, String> typeToPlayerDefaults = new HashMap<>();
	
	public static boolean isTypedPatron(String name)
	{
		for(EnumCreatureType type : typeToPlayerDefaults.keySet())
			for(String player : typeToPlayerDefaults.get(type).split(","))
				if(player.equals(name))
					return true;
		return false;
	}
	
	public static String getMobDefaults(EnumCreatureType type)
	{
		return typeToMobDefaults.getOrDefault(type, "");
	}
	
	public static String getPlayerDefaults(EnumCreatureType type)
	{
		return typeToPlayerDefaults.getOrDefault(type, "");
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
	
	private static void addPlayerToTypeDefaults(String playerName, EnumCreatureType... groups)
	{
		for(EnumCreatureType type : groups)
		{
			String entry = typeToPlayerDefaults.containsKey(type) ? typeToPlayerDefaults.get(type) : "";
			entry += (entry.length() > 0 ? "," : "") + playerName;
			typeToPlayerDefaults.put(type, entry);
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
		
		// Individual player settings (override defaults)
		addPlayerToTypeDefaults("_Lying", 					EnumCreatureType.HOLY, EnumCreatureType.FEY);
		addPlayerToTypeDefaults("nikmat97",					EnumCreatureType.MAGICAL_BEAST, EnumCreatureType.SHAPECHANGER, EnumCreatureType.FIRE);
		addPlayerToTypeDefaults("SakuraWolfe",				EnumCreatureType.MAGICAL_BEAST, EnumCreatureType.EXTRAPLANAR);
		addPlayerToTypeDefaults("Dusty21134",				EnumCreatureType.OUTSIDER, EnumCreatureType.EXTRAPLANAR);
		addPlayerToTypeDefaults("Princessfirefly9",			EnumCreatureType.FEY, EnumCreatureType.AIR);
		addPlayerToTypeDefaults("sawtooth44",				EnumCreatureType.ELEMENTAL, EnumCreatureType.FIRE, EnumCreatureType.EXTRAPLANAR);
		addPlayerToTypeDefaults("Kyofushin",				EnumCreatureType.UNDEAD, EnumCreatureType.FEY);
		addPlayerToTypeDefaults("Kurloz_M",					EnumCreatureType.PLANT, EnumCreatureType.EARTH);
		addPlayerToTypeDefaults("_Booked",					EnumCreatureType.FEY, EnumCreatureType.OUTSIDER);
		addPlayerToTypeDefaults("Wolframstein",				EnumCreatureType.MONSTROUS_HUMANOID, EnumCreatureType.AUGMENTED);
		addPlayerToTypeDefaults("thefeywilds",				EnumCreatureType.OUTSIDER, EnumCreatureType.AQUATIC, EnumCreatureType.EXTRAPLANAR);
		addPlayerToTypeDefaults("Alantor6616",				EnumCreatureType.CONSTRUCT, EnumCreatureType.EARTH);
		addPlayerToTypeDefaults("Pyrodance",				EnumCreatureType.HUMANOID, EnumCreatureType.SHAPECHANGER);
	}
}