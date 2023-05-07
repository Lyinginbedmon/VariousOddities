package com.lying.variousoddities.proxy;

import java.util.HashMap;
import java.util.Map;

import com.lying.variousoddities.client.KeyBindings;
import com.lying.variousoddities.client.RendererHandler;
import com.lying.variousoddities.client.SettlementManagerClient;
import com.lying.variousoddities.client.SpellManagerClient;
import com.lying.variousoddities.client.gui.GuiHandler;
import com.lying.variousoddities.client.gui.screen.ScreenSelectSpecies;
import com.lying.variousoddities.client.renderer.ColorHandler;
import com.lying.variousoddities.world.savedata.ScentsManager;
import com.lying.variousoddities.world.savedata.SettlementManager;
import com.lying.variousoddities.world.savedata.SpellManager;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy
{
	private static final Minecraft mc = Minecraft.getInstance();
	private SettlementManager settlements = new SettlementManagerClient(new CompoundTag());
	private SpellManager spells = new SpellManagerClient();
	private Map<ResourceKey<DimensionType>, ScentsManager> scentManagers = new HashMap<>();
	
	public static TypesManager localTypesData = new TypesManager();
	public static Map<String, Integer> localReputation = new HashMap<>();
	
	public TypesManager getTypesManager(){ return localTypesData; }
	public Map<String, Integer> getReputation(){ return localReputation; }
	public void setReputation(Map<String, Integer> repIn){ localReputation = repIn; }
	
	public static void registerKeyMappings(RegisterKeyMappingsEvent event)
	{
		KeyBindings.registerKeybinds(event::register);
	}
	
	public void registerHandlers()
	{
		IEventBus busMod = FMLJavaModLoadingContext.get().getModEventBus();
		busMod.addListener(EventPriority.NORMAL, RendererHandler::registerTileRenderers);
        
		IEventBus busForge = MinecraftForge.EVENT_BUS;
		busForge.addListener(EventPriority.NORMAL, GuiHandler::renderAbilityOverlay);
		busForge.addListener(EventPriority.NORMAL, GuiHandler::curtailHUDWhenAbnormal);
		busForge.addListener(EventPriority.NORMAL, GuiHandler::renderBludgeoning);
	}
	
	public void onLoadComplete(FMLLoadCompleteEvent event)
	{
		event.enqueueWork(() -> ColorHandler.registerColorHandlers());
	}
	
	public Player getPlayerEntity(NetworkEvent.Context ctx){ return (ctx.getDirection().getReceptionSide().isClient() ? mc.player : super.getPlayerEntity(ctx)); }
	
	public SettlementManager getSettlementManager(Level worldIn)
	{
		if(settlements == null || mc.level.dimensionType() != settlements.getDim())
		{
			settlements = new SettlementManagerClient(new CompoundTag());
			settlements.setWorld(mc.level);
		}
		return settlements;
	}
	
	public void clearSettlements(){ settlements = null; }
	
	public ScentsManager getScentsManager(Level worldIn)
	{
		ResourceKey<DimensionType> dim = worldIn.dimensionTypeId();
		if(!scentManagers.containsKey(dim))
			scentManagers.put(dim, new ScentsManager(worldIn));
		return scentManagers.get(dim);
	}
	
	public SpellManager getSpells()
	{
		return spells;
	}
	
	public void openSpeciesSelectScreen(Player entity, int power, boolean random)
	{
		if(Minecraft.getInstance().screen == null)
			Minecraft.getInstance().setScreen(new ScreenSelectSpecies(entity, power, random));
	}
}
