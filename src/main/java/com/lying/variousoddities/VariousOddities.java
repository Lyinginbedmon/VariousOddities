package com.lying.variousoddities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.client.KeyBindings;
import com.lying.variousoddities.client.SettlementRender;
import com.lying.variousoddities.client.renderer.EntityRenderRegistry;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.data.VODataGenerators;
import com.lying.variousoddities.entity.ai.group.GroupHandler;
import com.lying.variousoddities.faction.FactionBus;
import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.init.VOCommands;
import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.init.VORegistryHandler;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.proxy.ClientProxy;
import com.lying.variousoddities.proxy.IProxy;
import com.lying.variousoddities.proxy.ServerProxy;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.SpeciesRegistry;
import com.lying.variousoddities.species.TemplateRegistry;
import com.lying.variousoddities.species.types.TypeBus;
import com.lying.variousoddities.utility.VOBusClient;
import com.lying.variousoddities.utility.VOBusServer;
import com.lying.variousoddities.world.settlement.SettlementManagerServer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Reference.ModInfo.MOD_ID)
public class VariousOddities
{
	public static final Logger log = LogManager.getLogger(Reference.ModInfo.MOD_ID);
	
	public static IProxy proxy = new ServerProxy();
	
	private final VORegistryHandler registries;
	
	@SuppressWarnings("deprecation")
	public VariousOddities()
	{
		DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> proxy = new ClientProxy());
		proxy.registerHandlers();
		
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::doCommonSetup);
        bus.addListener(this::doClientSetup);
        bus.addListener(this::doLoadComplete);
        bus.addListener(VODataGenerators::onGatherData);
        MinecraftForge.EVENT_BUS.register(this);
        
        bus.register(registries = new VORegistryHandler());
        MinecraftForge.EVENT_BUS.register(registries);
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigVO.server_spec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigVO.client_spec);
        bus.addListener(this::onConfigEvent);
        MinecraftForge.EVENT_BUS.addListener(VODamageSource::livingHurtEvent);
	}
	
    private void doCommonSetup(final FMLCommonSetupEvent event)
    {
    	PacketHandler.init();
    	PlayerData.register();
    	LivingData.register();
    	event.enqueueWork(VOCommands::registerArguments);
    	MinecraftForge.EVENT_BUS.register(VOBusServer.class);
    	MinecraftForge.EVENT_BUS.register(SettlementManagerServer.class);
    	MinecraftForge.EVENT_BUS.register(GroupHandler.class);
    	MinecraftForge.EVENT_BUS.register(TypeBus.class);
    	MinecraftForge.EVENT_BUS.register(FactionBus.class);
    }
    
    private void doClientSetup(final FMLClientSetupEvent event)
    {
    	KeyBindings.register();
        EntityRenderRegistry.registerEntityRenderers();
        RenderTypeLookup.setRenderLayer(VOBlocks.LAYER_SCALE, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(VOBlocks.MOSS_BLOCK, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(VOBlocks.TABLE_DRAFTING, RenderType.getCutout());
        MinecraftForge.EVENT_BUS.register(VOBusClient.class);
        MinecraftForge.EVENT_BUS.register(SettlementRender.class);
    }
	
    private void doLoadComplete(final FMLLoadCompleteEvent event)
    {
    	proxy.onLoadComplete(event);
    }
    
    private void onConfigEvent(final ModConfigEvent event)
    {
    	switch(event.getConfig().getType())
    	{
			case CLIENT:
				break;
			case SERVER:
		    	ConfigVO.updateCache();
				break;
			default:
				break;
    	}
    }
    
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event)
    {
    	
    }
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event){ VOCommands.onCommandRegister(event); }
	
    @SubscribeEvent
	public void onReloadListenersEvent(AddReloadListenerEvent event)
	{
		event.addListener(SpeciesRegistry.getInstance());
		event.addListener(TemplateRegistry.getInstance());
	}
}
