package com.lying.variousoddities.proxy;

import java.util.HashMap;
import java.util.Map;

import com.lying.variousoddities.client.SettlementManagerClient;
import com.lying.variousoddities.client.SpellManagerClient;
import com.lying.variousoddities.world.savedata.SettlementManager;
import com.lying.variousoddities.world.savedata.SpellManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientProxy extends CommonProxy
{
	private static final Minecraft mc = Minecraft.getInstance();
	private SettlementManager settlements = new SettlementManagerClient();
	private SpellManager spells = new SpellManagerClient();
	
//	public static TypesData localTypesData = new TypesData();
	public static Map<String, Integer> localReputation = new HashMap<>();
	
//	public TypesData getTypesData(){ return localTypesData; }
	public Map<String, Integer> getReputation(){ return localReputation; }
	public void setReputation(Map<String, Integer> repIn){ localReputation = repIn; }
	
	public PlayerEntity getPlayerEntity(NetworkEvent.Context ctx){ return (ctx.getDirection().getReceptionSide().isClient() ? mc.player : super.getPlayerEntity(ctx)); }
	
	public SettlementManager getSettlementManager(World worldIn)
	{
		if(settlements == null || mc.world.getDimensionType() != settlements.getDim())
		{
			settlements = new SettlementManagerClient();
			settlements.setWorld(mc.world);
		}
		return settlements;
	}
	
	public SpellManager getSpells()
	{
		return spells;
	}
}
