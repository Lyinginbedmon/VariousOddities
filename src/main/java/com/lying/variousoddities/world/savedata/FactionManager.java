package com.lying.variousoddities.world.savedata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.entity.IFactionMob;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.faction.FactionReputation.EnumAttitude;
import com.lying.variousoddities.reference.Reference;
import com.mojang.brigadier.StringReader;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class FactionManager extends SavedData
{
	protected static final String DATA_NAME = Reference.ModInfo.MOD_ID+"_factions";
	
	private static final Map<String, Faction> DEFAULT_FACTIONS = new HashMap<>();
	public final Map<String, Faction> factions = new HashMap<>();
	
	public FactionManager(){ }
	
	public static FactionManager get(Level worldIn)
	{
		if(worldIn.isClientSide)
			return null;
		else
		{
			ServerLevel world = (ServerLevel)worldIn;
			MinecraftServer server = world.getServer();
			ServerLevel overWorld = server.getLevel(Level.OVERWORLD);
			FactionManager manager = (FactionManager)overWorld.getDataStorage().get(FactionManager::fromNBT, DATA_NAME);
			if(manager == null)
			{
				manager = new FactionManager();
				if(ConfigVO.MOBS.factionSettings.factionsInConfig())
					manager.stringToFactions(ConfigVO.MOBS.factionSettings.factionString());
				overWorld.getDataStorage().set(DATA_NAME, manager);
			}
			return manager;
		}
	}
	
	public void add(Faction faction)
	{
		factions.put(faction.name, faction);
		setDirty();
	}
	
	public void remove(String name)
	{
		factions.remove(name);
		setDirty();
	}
	
	public void clear()
	{
		factions.clear();
		setDirty();
	}
	
	public boolean isEmpty()
	{
		return factions.isEmpty();
	}
	
	public int size()
	{
		return factions.size();
	}
	
	public Set<String> factionNames()
	{
		return factions.keySet();
	}
	
	public static Set<String> defaultFactions()
	{
		return DEFAULT_FACTIONS.keySet();
	}
	
	public Faction getFaction(String name)
	{
		if(factions.containsKey(name))
			return factions.get(name);
		return null;
	}
	
	public CompoundTag save(CompoundTag compound)
	{
		ListTag list = new ListTag();
		for(Faction faction : factions.values())
			list.add(faction.writeToNBT(new CompoundTag()));
		compound.put("Factions", list);
		return compound;
	}
	
	public void read(CompoundTag compound)
	{
		ListTag list = compound.getList("Factions", 10);
		for(int i=0; i<list.size(); i++)
		{
			Faction faction = Faction.readFromNBT(list.getCompound(i));
			if(faction != null)
				factions.put(faction.name, faction);
		}
	}
	
	public static FactionManager fromNBT(CompoundTag compound)
	{
		FactionManager manager = new FactionManager();
		manager.read(compound);
		return manager;
	}
	
	public Faction getFaction(LivingEntity entity)
	{
		if(entity instanceof IFactionMob)
			return getFaction(((IFactionMob)entity).getFactionName());
		else if(entity.getType() == EntityType.PLAYER && PlayerData.forPlayer((Player)entity) != null)
			return getFaction(PlayerData.forPlayer((Player)entity).reputation.factionName());
		return null;
	}
	
	public static String defaultsToString()
	{
		CompoundTag comp = new CompoundTag();
		ListTag list = new ListTag();
		for(Faction faction : DEFAULT_FACTIONS.values())
			list.add(faction.writeToNBT(new CompoundTag()));
		comp.put("Factions", list);
		return comp.toString();
	}
	
	public void stringToFactions(String par1String)
	{
		this.factions.clear();
		
		StringReader values = new StringReader(par1String);
		ListTag factionList = null;
		try
		{
			CompoundTag comp = TagParser.parseTag(values.readString());
			factionList = comp.getList("Factions", 10);
		}
		catch(Exception e){ VariousOddities.log.error("Config error: Malformed faction settings"); }
		
		if(factionList != null && !factionList.isEmpty())
		{
			for(int i=0; i<factionList.size(); i++)
			{
				Faction faction = null;
				try
				{
					faction = Faction.readFromNBT(factionList.getCompound(i));
				}
				catch(Exception e){ VariousOddities.log.error("Config error: Malformed faction entry"); }
				if(faction != null)
					add(faction);
			}
		}
	}
	
	public static class Faction
	{
		public final String name;
		public final int startingRep;
		private Map<String, Integer> relations = new HashMap<>();
		
		public Faction(String nameIn)
		{
			this(nameIn, 0);
		}
		
		public Faction(String nameIn, int rep)
		{
			name = nameIn;
			startingRep = rep;
		}
		
		public Faction addRelation(String name, int rep)
		{
			relations.put(name, rep);
			return this;
		}
		
		public Faction addRelation(Faction faction, int rep)
		{
			return addRelation(faction.name, rep);
		}
		
		public EnumAttitude relationWith(String name)
		{
			return relations.containsKey(name) ? EnumAttitude.fromRep(relations.get(name)) : EnumAttitude.fromRep(startingRep);
		}
		
		public Map<String, Integer> getRelations()
		{
			return relations;
		}
		
		public CompoundTag writeToNBT(CompoundTag compound)
		{
			compound.putString("Name", this.name);
			compound.putInt("InitialRep", this.startingRep);
			if(!relations.isEmpty())
			{
				ListTag relationMap = new ListTag();
				for(String faction : relations.keySet())
				{
					CompoundTag data = new CompoundTag();
						data.putString("Name", faction);
						data.putInt("Rep", relations.get(faction));
					relationMap.add(data);
				}
				compound.put("Relations", relationMap);
			}
			return compound;
		}
		
		public static Faction readFromNBT(CompoundTag compound)
		{
			String name = compound.getString("Name");
			int rep = compound.getInt("InitialRep");
			Faction faction = new Faction(name, rep);
			if(compound.contains("Relations", 9))
			{
				ListTag relationMap = compound.getList("Relations", 10);
				for(int i=0; i<relationMap.size(); i++)
				{
					CompoundTag data = relationMap.getCompound(i);
					faction.addRelation(data.getString("Name"), data.getInt("Rep"));
				}
			}
			return faction;
		}
	}
	
	public static void addDefaultFaction(Faction faction)
	{
		DEFAULT_FACTIONS.put(faction.name, faction);
	}
	
	static
	{
		addDefaultFaction(new Faction("kobold", 0).addRelation("goblin", -100));
		addDefaultFaction(new Faction("goblin", -30).addRelation("kobold", -100));
	}
}
