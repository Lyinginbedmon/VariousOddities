package com.lying.variousoddities.world.savedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.entity.IFactionMob;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.faction.FactionReputation.EnumAttitude;
import com.lying.variousoddities.reference.Reference;
import com.mojang.brigadier.StringReader;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class FactionManager extends WorldSavedData
{
	protected static final String DATA_NAME = Reference.ModInfo.MOD_ID+"_factions";
	
	private static final List<Faction> DEFAULT_FACTIONS = new ArrayList<>();
	public final Map<String, Faction> factions = new HashMap<>();
	
	public FactionManager()
	{
		this(DATA_NAME);
	}
	
	public FactionManager(String name)
	{
		super(name);
	}
	
	public static FactionManager get(World worldIn)
	{
		if(worldIn.isRemote)
			return null;
		else
		{
			ServerWorld world = (ServerWorld)worldIn;
			FactionManager manager = (FactionManager)world.getSavedData().get(FactionManager::new, DATA_NAME);
			if(manager == null)
			{
				manager = (FactionManager)world.getSavedData().getOrCreate(FactionManager::new, DATA_NAME);
				if(ConfigVO.MOBS.factionSettings.factionsInConfig())
					manager.stringToFactions(ConfigVO.MOBS.factionSettings.factionString());
			}
			return manager;
		}
	}
	
	public void add(Faction faction)
	{
		factions.put(faction.name, faction);
		markDirty();
	}
	
	public void remove(String name)
	{
		factions.remove(name);
		markDirty();
	}
	
	public void clear()
	{
		factions.clear();
		markDirty();
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
	
	public Faction getFaction(String name)
	{
		if(factions.containsKey(name))
			return factions.get(name);
		return null;
	}
	
	public CompoundNBT write(CompoundNBT compound)
	{
		ListNBT list = new ListNBT();
		for(Faction faction : factions.values())
			list.add(faction.writeToNBT(new CompoundNBT()));
		compound.put("Factions", list);
		return compound;
	}
	
	public void read(CompoundNBT compound)
	{
		ListNBT list = compound.getList("Factions", 10);
		for(int i=0; i<list.size(); i++)
		{
			Faction faction = Faction.readFromNBT(list.getCompound(i));
			if(faction != null)
				factions.put(faction.name, faction);
		}
	}
	
	public Faction getFaction(LivingEntity entity)
	{
		if(entity instanceof IFactionMob)
			return getFaction(((IFactionMob)entity).getFactionName());
		else if(entity.getType() == EntityType.PLAYER)
			return getFaction(PlayerData.forPlayer((PlayerEntity)entity).reputation.factionName());
		return null;
	}
	
	public static String defaultsToString()
	{
		CompoundNBT comp = new CompoundNBT();
		ListNBT list = new ListNBT();
		for(Faction faction : DEFAULT_FACTIONS)
			list.add(faction.writeToNBT(new CompoundNBT()));
		comp.put("Factions", list);
		return comp.toString();
	}
	
	public void stringToFactions(String par1String)
	{
		this.factions.clear();
		
		StringReader values = new StringReader(par1String);
		ListNBT factionList = null;
		try
		{
			CompoundNBT comp = new JsonToNBT(values).readStruct();
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
		
		public EnumAttitude relationWith(String name)
		{
			return relations.containsKey(name) ? EnumAttitude.fromRep(relations.get(name)) : EnumAttitude.fromRep(startingRep);
		}
		
		public Map<String, Integer> getRelations()
		{
			return relations;
		}
		
		public CompoundNBT writeToNBT(CompoundNBT compound)
		{
			compound.putString("Name", this.name);
			compound.putInt("InitialRep", this.startingRep);
			if(!relations.isEmpty())
			{
				ListNBT relationMap = new ListNBT();
				for(String faction : relations.keySet())
				{
					CompoundNBT data = new CompoundNBT();
						data.putString("Name", faction);
						data.putInt("Rep", relations.get(faction));
					relationMap.add(data);
				}
				compound.put("Relations", relationMap);
			}
			return compound;
		}
		
		public static Faction readFromNBT(CompoundNBT compound)
		{
			String name = compound.getString("Name");
			int rep = compound.getInt("InitialRep");
			Faction faction = new Faction(name, rep);
			if(compound.contains("Relations", 9))
			{
				ListNBT relationMap = compound.getList("Relations", 10);
				for(int i=0; i<relationMap.size(); i++)
				{
					CompoundNBT data = relationMap.getCompound(i);
					faction.addRelation(data.getString("Name"), data.getInt("Rep"));
				}
			}
			return faction;
		}
	}
	
	static
	{
		DEFAULT_FACTIONS.add(new Faction("kobold", 0).addRelation("goblin", -100));
		DEFAULT_FACTIONS.add(new Faction("goblin", -30).addRelation("kobold", -100));
	}
}
