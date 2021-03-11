package com.lying.variousoddities.capabilities;

import java.util.HashMap;
import java.util.Map;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerData implements ICapabilitySerializable<CompoundNBT>
{
	@CapabilityInject(PlayerData.class)
	public static final Capability<PlayerData> CAPABILITY = null;
	public static final ResourceLocation IDENTIFIER = new ResourceLocation(Reference.ModInfo.MOD_ID, "player_data");
	
	private final LazyOptional<PlayerData> handler;
	
	public Reputation reputation = new Reputation();
	
	public PlayerData()
	{
		this.handler = LazyOptional.of(() -> this);
	}
	
	public static void register()
	{
		CapabilityManager.INSTANCE.register(PlayerData.class, new PlayerData.Storage(), () -> null);
		if(ConfigVO.GENERAL.verboseLogs())
			VariousOddities.log.info("Registered player data capability");
	}
	
	public static PlayerData forPlayer(PlayerEntity player) throws RuntimeException
	{
		return player.getCapability(CAPABILITY).orElseThrow(() -> new RuntimeException("No player data found for "+player.getName()));
	}
	
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		return CAPABILITY.orEmpty(cap, this.handler);
	}
	
	public CompoundNBT serializeNBT()
	{
		CompoundNBT compound = new CompoundNBT();
		
		this.reputation.serializeNBT(compound);
		
		return compound;
	}
	
	public void deserializeNBT(CompoundNBT nbt)
	{
		if(nbt.contains("Reputation"))
			this.reputation.deserializeNBT(nbt.getCompound("Reputation"));
	}
	
	/**
	 * Faction reputation holder class
	 * @author Lying
	 */
	public static class Reputation
	{
		private final Map<String, Integer> reputation = new HashMap<>();
		private String faction = "";
		
		public CompoundNBT serializeNBT(CompoundNBT compound)
		{
			if(!reputation.isEmpty())
			{
				ListNBT list = new ListNBT();
				for(String faction : reputation.keySet())
				{
					CompoundNBT data = new CompoundNBT();
					data.putString("Faction", faction);
					data.putInt("Rep", reputation.get(faction));
					list.add(data);
				}
				compound.put("Reputation", list);
			}
			return compound;
		}
		
		public void deserializeNBT(CompoundNBT nbt)
		{
			ListNBT list = nbt.getList("Reputation", 10);
			for(int i=0; i<list.size(); i++)
			{
				CompoundNBT data = list.getCompound(i);
				reputation.put(data.getString("Faction"), data.getInt("Rep"));
			}
		}
		
		public String factionName(){ return (this.faction == null || this.faction.length() == 0) ? null : this.faction; }
		
		public void setFaction(String par1String){ this.faction = par1String; }
		
		/**
		 * Returns the players reputation with the given faction, or integer min value if they have none recorded.
		 * @param faction
		 * @return
		 */
		public int getReputation(String faction)
		{
			return reputation.containsKey(faction) ? reputation.get(faction) : Integer.MIN_VALUE;
		}
		
		public void setReputation(String faction, int rep)
		{
			if(ConfigVO.GENERAL.verboseLogs())
				VariousOddities.log.info("Set reputation with "+faction+" to "+rep);
			reputation.put(faction, MathHelper.clamp(rep, -100, 100));
		}
		
		public void addReputation(String faction, int rep)
		{
			int currentRep = getReputation(faction);
			setReputation(faction, currentRep == Integer.MIN_VALUE ? rep : currentRep + rep);
		}
	}
	
	public static class Storage implements Capability.IStorage<PlayerData>
	{
		public INBT writeNBT(Capability<PlayerData> capability, PlayerData instance, Direction side)
		{
			return instance.serializeNBT();
		}
		
		public void readNBT(Capability<PlayerData> capability, PlayerData instance, Direction side, INBT nbt)
		{
			if(nbt.getId() == 10)
				instance.deserializeNBT((CompoundNBT)nbt);
		}
	}
}
