package com.lying.variousoddities.world.savedata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketTypesData;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.types.EnumCreatureType;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class TypesManager extends WorldSavedData
{
	protected static final String DATA_NAME = Reference.ModInfo.MOD_ID+"_types";
	
	private Map<EnumCreatureType, List<ResourceLocation>> typeToMob = new HashMap<>();
	private Map<EnumCreatureType, List<String>> typeToPlayer = new HashMap<>();
	
	private ServerWorld world = null;
	
	public TypesManager()
	{
		this(DATA_NAME);
	}
	
	public TypesManager(String nameIn)
	{
		super(nameIn);
	}
	
	public void read(CompoundNBT compound)
	{
		System.out.println((world == null ? "[CLIENT]" : "[SERVER]")+" Reading types from memory: "+compound);
		ListNBT mobs = compound.getList("Mobs", 10);
		for(int i=0; i<mobs.size(); i++)
		{
			CompoundNBT typ = mobs.getCompound(i);
			EnumCreatureType type = EnumCreatureType.fromName(typ.getString("Type"));
			
			ListNBT entr = typ.getList("Entries", 8);
			List<ResourceLocation> entries = new ArrayList<>();
			for(int j=0; j<entr.size(); j++)
				entries.add(new ResourceLocation(entr.getString(j)));
			
			typeToMob.put(type, entries);
		}
		
		ListNBT players = compound.getList("Players", 10);
		for(int i=0; i<players.size(); i++)
		{
			CompoundNBT typ = players.getCompound(i);
			EnumCreatureType type = EnumCreatureType.fromName(typ.getString("Type"));
			
			ListNBT entr = typ.getList("Entries", 8);
			List<String> entries = new ArrayList<String>();
			for(int j=0; j<entr.size(); j++)
				entries.add(entr.getString(j));
			
			typeToPlayer.put(type, entries);
		}
	}
	
	public CompoundNBT write(CompoundNBT compound)
	{
		ListNBT mobs = new ListNBT();
		for(EnumCreatureType type : typeToMob.keySet())
		{
			CompoundNBT typ = new CompoundNBT();
			typ.putString("Type", type.getSimpleName());
			ListNBT entries = new ListNBT();
			for(ResourceLocation entry : typeToMob.get(type))
				entries.add(StringNBT.valueOf(entry.toString()));
			typ.put("Entries", entries);
			
			mobs.add(typ);
		}
		compound.put("Mobs", mobs);
		
		ListNBT players = new ListNBT();
		for(EnumCreatureType type : typeToPlayer.keySet())
		{
			CompoundNBT typ = new CompoundNBT();
			typ.putString("Type", type.getSimpleName());
			ListNBT entries = new ListNBT();
			for(String entry : typeToPlayer.get(type))
				if(entry != null && entry.length() > 0)
					entries.add(StringNBT.valueOf(entry));
			typ.put("Entries", entries);
			players.add(typ);
		}
		compound.put("Players", players);
		return compound;
	}
	
	public static TypesManager get(World worldIn)
	{
		if(worldIn.isRemote)
			return VariousOddities.proxy.getTypesManager();
		else
		{
			ServerWorld world = (ServerWorld)worldIn;
			TypesManager manager = (TypesManager)world.getSavedData().get(TypesManager::new, DATA_NAME);
			if(manager == null)
			{
				manager = (TypesManager)world.getSavedData().getOrCreate(TypesManager::new, DATA_NAME);
				manager.resetMobs();
				manager.resetPlayers();
			}
			manager.world = world;
			return manager;
		}
	}
	
	public void notifyPlayer(PlayerEntity player)
	{
		PacketHandler.sendTo((ServerPlayerEntity)player, new PacketTypesData(write(new CompoundNBT())));
	}
	
	public void notifyPlayers(ServerWorld world)
	{
		PacketHandler.sendToAll(world, new PacketTypesData(write(new CompoundNBT())));
	}
	
	public void resetMobs()
	{
		typeToMob.clear();
		Map<EnumCreatureType, String[]> configuredMobs = ConfigVO.MOBS.typeSettings.getMobTypes();
		
		for(EnumCreatureType type : configuredMobs.keySet())
			for(String entry : configuredMobs.get(type))
				if(entry != null && entry.length() > 0 && entry.contains(":"))
					addToEntity(new ResourceLocation(entry), type, false);
		markDirty();
	}
	
	public void resetPlayers()
	{
		typeToPlayer.clear();
		Map<EnumCreatureType, String[]> configuredPlayers = ConfigVO.MOBS.typeSettings.getPlayerTypes();
		for(EnumCreatureType type : configuredPlayers.keySet())
			for(String entry : configuredPlayers.get(type))
				if(entry != null && entry.length() > 0)
					addToPlayer(entry, type, false);
		markDirty();
	}
	
	public List<String> getTypedPlayers()
	{
		List<String> players = new ArrayList<>();
		if(!typeToPlayer.isEmpty())
		{
			for(EnumCreatureType type : typeToPlayer.keySet())
			{
				List<String> members = new ArrayList<>();
				members.addAll(typeToPlayer.get(type));
				members.removeAll(players);
				players.addAll(members);
			}
			Collections.sort(players);
		}
		return players;
	}
	
	public List<ResourceLocation> mobsOfType(EnumCreatureType type)
	{
		List<ResourceLocation> mobs = typeToMob.getOrDefault(type, Collections.emptyList());
		Collections.sort(mobs);
		return mobs;
	}
	
	public List<String> playersOfType(EnumCreatureType type)
	{
		List<String> players = typeToPlayer.getOrDefault(type, Collections.emptyList());
		Collections.sort(players);
		return players;
	}
	
	public List<EnumCreatureType> getMobTypes(Entity entity)
	{
		return entity instanceof LivingEntity ? getMobTypes((LivingEntity)entity) : Collections.emptyList();
	}
	public List<EnumCreatureType> getMobTypes(LivingEntity entity)
	{
		return entity.getType() == EntityType.PLAYER ? getPlayerTypes((PlayerEntity)entity, false) : getMobTypes(entity.getType());
	}
	public List<EnumCreatureType> getMobTypes(EntityType<?> typeIn)
	{
		return getMobTypes(typeIn.getRegistryName());
	}
	public List<EnumCreatureType> getMobTypes(ResourceLocation registryName)
	{
		return getMobTypes(registryName.toString());
	}
	public List<EnumCreatureType> getMobTypes(String registryName)
	{
		List<EnumCreatureType> types = new ArrayList<>();
		
		for(EnumCreatureType type : typeToMob.keySet())
			for(ResourceLocation entry : typeToMob.getOrDefault(type, Collections.emptyList()))
				if(entry.toString().equalsIgnoreCase(registryName))
				{
					types.add(type);
					break;
				}
		
		return types;
	}
	
	public List<EnumCreatureType> getPlayerTypes(PlayerEntity player, boolean customOnly)
	{
		return player != null ? getPlayerTypes(player.getName().getUnformattedComponentText(), customOnly) : Collections.emptyList();
	}
	public List<EnumCreatureType> getPlayerTypes(String playerName, boolean customOnly)
	{
		List<EnumCreatureType> types = new ArrayList<>();
		
		for(EnumCreatureType type : typeToPlayer.keySet())
			for(String entry : typeToPlayer.get(type))
				if(entry.equalsIgnoreCase(playerName))
				{
					types.add(type);
					break;
				}
		
		if(types.isEmpty() && !customOnly)
			types.addAll(getMobTypes(EntityType.PLAYER));
		
		return types;
	}
	
	public boolean isEvil(LivingEntity input)	{ return isMobOfType(input, EnumCreatureType.EVIL); }
	public boolean isGolem(LivingEntity input)	{ return isMobOfType(input, EnumCreatureType.CONSTRUCT); }
	public boolean isHoly(LivingEntity input)	{ return isMobOfType(input, EnumCreatureType.HOLY); }
	public boolean isUndead(LivingEntity input)	{ return isMobOfType(input, EnumCreatureType.UNDEAD); }
	
	public boolean hasCustomAttributes(LivingEntity input)
	{
		return !getCreatureAttributes(input).isEmpty();
	}
	
	public boolean hasCustomTypes(LivingEntity input)
	{
		return !getMobTypes(input).isEmpty();
	}
	
	public boolean hasPlayerCustomTypes(PlayerEntity input)
	{
		return !getPlayerTypes(input, true).isEmpty();
	}
	
	public boolean isMobOfType(ResourceLocation input, EnumCreatureType group)
	{
		if(group == null) return false;
		return getMobTypes(input).contains(group);
	}
	
	public boolean isMobOfType(LivingEntity input, EnumCreatureType group)
	{
		if(group == null) return false;
		return getMobTypes(input).contains(group);
	}
	
	/**
	 * Returns true if the given player is considered of the given type.<br>
	 * Only checks their custom types, not those specified for minecraft:player
	 */
	public boolean isPlayerOfType(PlayerEntity input, EnumCreatureType group)
	{
		return isPlayerOfType(input.getName().getUnformattedComponentText(), group);
	}
	
	public boolean isPlayerOfType(String name, EnumCreatureType group)
	{
		if(group == null || name == null || name.length() == 0) return false;
		return getPlayerTypes(name, true).contains(group);
	}
	
	/** Returns all vanilla types applicable to the given entity */
	public List<CreatureAttribute> getCreatureAttributes(LivingEntity mobIn)
	{
		List<CreatureAttribute> attributes = new ArrayList<CreatureAttribute>();
		for(EnumCreatureType type : getMobTypes(mobIn))
			if(type.hasParentAttribute())
				attributes.add(type.getParentAttribute());
		return attributes;
	}
	
	public void addToEntity(ResourceLocation entity, EnumCreatureType type, boolean notify)
	{
		if(entity == null || type == null || isMobOfType(entity, type))
			return;
		
		List<ResourceLocation> entries = new ArrayList<ResourceLocation>();
			entries.addAll(mobsOfType(type));
			entries.add(entity);
		typeToMob.put(type, entries);
		
		if(notify)
			markDirty();
	}
	
	public void removeFromEntity(ResourceLocation entity, EnumCreatureType type, boolean notify)
	{
		if(entity == null || type == null || !isMobOfType(entity, type))
			return;
		List<ResourceLocation> entries = new ArrayList<ResourceLocation>();
			entries.addAll(typeToMob.getOrDefault(type, Collections.emptyList()));
			entries.remove(entity);
		typeToMob.put(type, entries);
		
		if(notify)
			markDirty();
	}
	
	public void addToPlayer(String player, EnumCreatureType type, boolean notify)
	{
		if(player == null || player.length() == 0 || isPlayerOfType(player, type))
			return;
		
		List<String> entries = new ArrayList<String>();
			entries.addAll(typeToPlayer.getOrDefault(type, Collections.emptyList()));
			entries.add(player);
		typeToPlayer.put(type, entries);
		
		if(notify)
			markDirty();
	}
	
	public void removeFromPlayer(String player, EnumCreatureType type, boolean notify)
	{
		if(player == null || player.length() == 0 || !isPlayerOfType(player, type))
			return;
		
		List<String> entries = new ArrayList<String>();
			entries.addAll(typeToPlayer.getOrDefault(type, Collections.emptyList()));
			entries.remove(player);
		typeToPlayer.put(type, entries);
		
		if(notify)
			markDirty();
	}
	
	public void markDirty()
	{
		super.markDirty();
		if(this.world != null)
			notifyPlayers(this.world);
	}
}
