package com.lying.variousoddities.proxy;

import java.util.HashMap;
import java.util.Map;

import com.lying.variousoddities.client.RendererHandler;
import com.lying.variousoddities.client.SettlementManagerClient;
import com.lying.variousoddities.client.SpellManagerClient;
import com.lying.variousoddities.client.gui.GuiHandler;
import com.lying.variousoddities.client.gui.ScreenSelectSpecies;
import com.lying.variousoddities.client.renderer.ColorHandler;
import com.lying.variousoddities.client.renderer.EntityRenderRegistry;
import com.lying.variousoddities.world.savedata.ScentsManager;
import com.lying.variousoddities.world.savedata.SettlementManager;
import com.lying.variousoddities.world.savedata.SpellManager;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkEvent;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy
{
	private static final Minecraft mc = Minecraft.getInstance();
	private SettlementManager settlements = new SettlementManagerClient();
	private SpellManager spells = new SpellManagerClient();
	private Map<RegistryKey<World>, ScentsManager> scentManagers = new HashMap<>();
	
	public static TypesManager localTypesData = new TypesManager();
	public static Map<String, Integer> localReputation = new HashMap<>();
	
	public TypesManager getTypesManager(){ return localTypesData; }
	public Map<String, Integer> getReputation(){ return localReputation; }
	public void setReputation(Map<String, Integer> repIn){ localReputation = repIn; }
	
	public void registerHandlers()
	{
		IEventBus busMod = FMLJavaModLoadingContext.get().getModEventBus();
		busMod.addListener(RendererHandler::registerTileRenderers);
        
		IEventBus busForge = MinecraftForge.EVENT_BUS;
		busForge.addListener(GuiHandler::renderAbilityOverlay);
		busForge.addListener(GuiHandler::curtailHUDWhenAbnormal);
		busForge.addListener(GuiHandler::renderBludgeoning);
	}
	
	public void onLoadComplete(FMLLoadCompleteEvent event)
	{
		event.enqueueWork(() -> { ColorHandler.registerColorHandlers(); });
		EntityRenderRegistry.appendRenderers(mc.getRenderManager());
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
	
	public void clearSettlements(){ settlements = null; }
	
	public ScentsManager getScentsManager(World worldIn)
	{
		RegistryKey<World> dim = worldIn.getDimensionKey();
		if(!scentManagers.containsKey(dim))
			scentManagers.put(dim, new ScentsManager(worldIn));
		return scentManagers.get(worldIn.getDimensionKey());
	}
	
	public SpellManager getSpells()
	{
		return spells;
	}
	
	public void openSpeciesSelectScreen(PlayerEntity entity, int power, boolean random)
	{
		if(Minecraft.getInstance().currentScreen == null)
			Minecraft.getInstance().displayGuiScreen(new ScreenSelectSpecies(entity, power, random));
	}
}
