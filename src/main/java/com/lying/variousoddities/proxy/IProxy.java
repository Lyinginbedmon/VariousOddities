package com.lying.variousoddities.proxy;

import java.util.HashMap;
import java.util.Map;

import com.lying.variousoddities.world.savedata.SettlementManager;
import com.lying.variousoddities.world.savedata.SpellManager;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public interface IProxy 
{
	public default void registerHandlers(){ }
	
	public default void onLoadComplete(FMLLoadCompleteEvent event){ }
	
	public TypesManager getTypesManager();
	
	public default Map<String, Integer> getReputation(){ return new HashMap<String, Integer>(); }
	public default void setReputation(Map<String, Integer> repIn){ }
	public default void setReputation(CompoundNBT compound)
	{
		Map<String, Integer> reputation = new HashMap<>();
		if(compound != null)
			for(String faction : compound.keySet())
				if(compound.contains(faction, 3))
					reputation.put(faction, compound.getInt(faction));
		setReputation(reputation);
	}
	
	public default SettlementManager getSettlementManager(World worldIn){ return null; }
	public default SpellManager getSpells(){ return null; }
}