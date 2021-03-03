package com.lying.variousoddities.faction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.entity.IFactionMob;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.faction.FactionReputation.EnumAttitude;
import com.mojang.brigadier.StringReader;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;

public class Factions
{
	private static final List<Faction> DEFAULT_FACTIONS = new ArrayList<>();
	public static final List<Faction> FACTIONS = new ArrayList<>();
	
	public static Faction get(String name)
	{
		if(FACTIONS.isEmpty() && ConfigVO.MOBS.factionSettings.factionsInConfig())
			stringToFactions(ConfigVO.MOBS.factionSettings.factionString());
		
		for(Faction faction : FACTIONS)
			if(faction.name.equals(name))
				return faction;
		return null;
	}
	
	public static Faction getFaction(LivingEntity entity)
	{
		if(entity instanceof IFactionMob)
			return get(((IFactionMob)entity).getFactionName());
		else if(entity.getType() == EntityType.PLAYER)
			return get(PlayerData.forPlayer((PlayerEntity)entity).reputation.factionName());
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
	
	public static void stringToFactions(String par1String)
	{
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
					FACTIONS.add(faction);
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
