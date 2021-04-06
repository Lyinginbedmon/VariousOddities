package com.lying.variousoddities.proxy;

import java.util.HashMap;
import java.util.Map;

import com.lying.variousoddities.client.RendererHandler;
import com.lying.variousoddities.client.SettlementManagerClient;
import com.lying.variousoddities.client.SpellManagerClient;
import com.lying.variousoddities.client.renderer.ColorHandler;
import com.lying.variousoddities.world.savedata.SettlementManager;
import com.lying.variousoddities.world.savedata.SpellManager;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkEvent;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy
{
	private static final Minecraft mc = Minecraft.getInstance();
	private SettlementManager settlements = new SettlementManagerClient();
	private SpellManager spells = new SpellManagerClient();
	
	public static TypesManager localTypesData = new TypesManager();
	public static Map<String, Integer> localReputation = new HashMap<>();
	
	public TypesManager getTypesManager(){ return localTypesData; }
	public Map<String, Integer> getReputation(){ return localReputation; }
	public void setReputation(Map<String, Integer> repIn){ localReputation = repIn; }
	
	public void registerHandlers()
	{
        FMLJavaModLoadingContext.get().getModEventBus().addListener(RendererHandler::registerTileRenderers);
	}
	
	public void onLoadComplete(FMLLoadCompleteEvent event)
	{
		event.enqueueWork(() -> { ColorHandler.registerColorHandlers(); });
	}
	
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
