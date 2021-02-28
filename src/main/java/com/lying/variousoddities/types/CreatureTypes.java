package com.lying.variousoddities.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class CreatureTypes
{
	public static Map<EnumCreatureType, String> typeToMobDefaults = new HashMap<>();
	public static Map<EnumCreatureType, String> typeToPlayerDefaults = new HashMap<>();
	
	public static List<ResourceLocation> mobsOfType(EnumCreatureType type)
	{
		List<ResourceLocation> mobs = new ArrayList<>();
		String[] mobNames = ConfigVO.MOBS.typeSettings.getMobTypes().get(type);
		for(String str : mobNames)
			if(str.length() > 0)
				mobs.add(new ResourceLocation(str));
		return mobs;
	}
	
	public static List<String> playersOfType(EnumCreatureType type)
	{
		List<String> players = new ArrayList<>();
		String[] playerNames = ConfigVO.MOBS.typeSettings.getPlayerTypes().get(type);
		for(String str : playerNames)
			if(str.length() > 0)
				players.add(str);
		return players;
	}
	
	public static List<EnumCreatureType> getMobTypes(Entity entity)
	{
		return entity instanceof LivingEntity ? getMobTypes((LivingEntity)entity) : Collections.emptyList();
	}
	public static List<EnumCreatureType> getMobTypes(LivingEntity entity)
	{
		return entity instanceof PlayerEntity ? getPlayerTypes((PlayerEntity)entity, false) : getMobTypes(entity.getType());
	}
	public static List<EnumCreatureType> getMobTypes(EntityType<?> typeIn)
	{
		return getMobTypes(typeIn.getRegistryName());
	}
	public static List<EnumCreatureType> getMobTypes(ResourceLocation registryName)
	{
		return getMobTypes(registryName.toString());
	}
	public static List<EnumCreatureType> getMobTypes(String registryName)
	{
		List<EnumCreatureType> types = new ArrayList<>();
		
		Map<EnumCreatureType, String[]> configured = ConfigVO.MOBS.typeSettings.getMobTypes();
		for(EnumCreatureType type : configured.keySet())
			for(String entry : configured.get(type))
				if(entry.equalsIgnoreCase(registryName))
				{
					types.add(type);
					break;
				}
		
		return types;
	}
	
	public static List<EnumCreatureType> getPlayerTypes(PlayerEntity player, boolean customOnly)
	{
		return getPlayerTypes(player.getName().getUnformattedComponentText(), customOnly);
	}
	public static List<EnumCreatureType> getPlayerTypes(String playerName, boolean customOnly)
	{
		List<EnumCreatureType> types = new ArrayList<>();
		
		Map<EnumCreatureType, String[]> configured = ConfigVO.MOBS.typeSettings.getPlayerTypes();
		for(EnumCreatureType type : configured.keySet())
			for(String entry : configured.get(type))
				if(entry.equalsIgnoreCase(playerName))
				{
					types.add(type);
					break;
				}
		
		if(types.isEmpty() && !customOnly)
			types.addAll(getMobTypes(EntityType.PLAYER));
		
		return types;
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
	
	public static boolean isEvil(LivingEntity input)	{ return isMobOfType(input, EnumCreatureType.EVIL); }
	public static boolean isGolem(LivingEntity input)	{ return isMobOfType(input, EnumCreatureType.CONSTRUCT); }
	public static boolean isHoly(LivingEntity input)	{ return isMobOfType(input, EnumCreatureType.HOLY); }
	public static boolean isUndead(LivingEntity input)	{ return isMobOfType(input, EnumCreatureType.UNDEAD); }
	
	public static boolean hasCustomAttributes(LivingEntity input)
	{
		return !getCreatureAttributes(input).isEmpty();
	}
	
	public static boolean hasCustomTypes(LivingEntity input)
	{
		return !getMobTypes(input).isEmpty();
	}
	
	public static boolean hasPlayerCustomTypes(PlayerEntity input)
	{
		return !getPlayerTypes(input, true).isEmpty();
	}
	
	public static boolean isMobOfType(ResourceLocation input, EnumCreatureType group)
	{
		if(group == null) return false;
		return getMobTypes(input).contains(group);
	}
	
	public static boolean isMobOfType(LivingEntity input, EnumCreatureType group)
	{
		if(group == null) return false;
		return getMobTypes(input).contains(group);
	}
	
	/**
	 * Returns true if the given player is considered of the given type.<br>
	 * Only checks their custom types, not those specified for minecraft:player
	 */
	public static boolean isPlayerOfType(PlayerEntity input, EnumCreatureType group)
	{
		if(group == null) return false;
		return getPlayerTypes(input, true).contains(group);
	}
	
	/** Returns all vanilla types applicable to the given entity */
	public static List<CreatureAttribute> getCreatureAttributes(LivingEntity mobIn)
	{
		List<CreatureAttribute> attributes = new ArrayList<CreatureAttribute>();
		for(EnumCreatureType type : getMobTypes(mobIn))
			if(type.hasParentAttribute())
				attributes.add(type.getParentAttribute());
		return attributes;
	}
	
	static
	{
		// Default mob settings
			// Vanilla mobs
		addMobToTypeDefaults(EntityType.CREEPER,			EnumCreatureType.PLANT, EnumCreatureType.EVIL);
		addMobToTypeDefaults(EntityType.ENDER_DRAGON,		EnumCreatureType.DRAGON, EnumCreatureType.EXTRAPLANAR);
		addMobToTypeDefaults(EntityType.WITHER,				EnumCreatureType.UNDEAD);
		addMobToTypeDefaults(EntityType.ELDER_GUARDIAN,		EnumCreatureType.ABERRATION, EnumCreatureType.AQUATIC);
		addMobToTypeDefaults(EntityType.GUARDIAN,			EnumCreatureType.ABERRATION, EnumCreatureType.AQUATIC);
		addMobToTypeDefaults(EntityType.ENDERMAN,			EnumCreatureType.OUTSIDER, EnumCreatureType.EXTRAPLANAR);
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
		addMobToTypeDefaults(EntityType.RAVAGER,			EnumCreatureType.MAGICAL_BEAST);
			// Oddities
		addMobToTypeDefaults(VOEntities.GOBLIN,				EnumCreatureType.HUMANOID, EnumCreatureType.GOBLIN);
		addMobToTypeDefaults(VOEntities.KOBOLD,				EnumCreatureType.HUMANOID, EnumCreatureType.REPTILE);
		addMobToTypeDefaults(VOEntities.RAT,				EnumCreatureType.VERMIN);
		addMobToTypeDefaults(VOEntities.RAT_GIANT,			EnumCreatureType.VERMIN);
		addMobToTypeDefaults(VOEntities.SCORPION,			EnumCreatureType.VERMIN);
		addMobToTypeDefaults(VOEntities.SCORPION_GIANT,		EnumCreatureType.VERMIN);
		
		// Individual player settings (override defaults)
		addPlayerToTypeDefaults("_Lying", 					EnumCreatureType.HOLY, EnumCreatureType.FEY);
	}
}