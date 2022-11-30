package com.lying.variousoddities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lying.variousoddities.client.KeyBindings;
import com.lying.variousoddities.client.renderer.EntityRenderRegistry;
import com.lying.variousoddities.client.special.BlindRender;
import com.lying.variousoddities.client.special.ScentRender;
import com.lying.variousoddities.client.special.SettlementRender;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.data.VODataGenerators;
import com.lying.variousoddities.entity.ai.group.GroupHandler;
import com.lying.variousoddities.faction.FactionBus;
import com.lying.variousoddities.init.VOBlockEntities;
import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.init.VOCapabilities;
import com.lying.variousoddities.init.VOCommands;
import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.init.VOEnchantments;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.init.VOItems;
import com.lying.variousoddities.init.VOMobEffects;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.proxy.ClientProxy;
import com.lying.variousoddities.proxy.IProxy;
import com.lying.variousoddities.proxy.ServerProxy;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.TypeBus;
import com.lying.variousoddities.utility.VOBusClient;
import com.lying.variousoddities.utility.VOBusServer;
import com.lying.variousoddities.world.savedata.ScentsManager;
import com.lying.variousoddities.world.settlement.SettlementManagerServer;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Reference.ModInfo.MOD_ID)
public class VariousOddities
{
	public static final Logger log = LogManager.getLogger(Reference.ModInfo.MOD_ID);
	
	@SuppressWarnings("deprecation")
	public static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);
	
	public VariousOddities()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
        VOBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        VOBlocks.BLOCKS.register(modEventBus);
        VOEnchantments.ENCHANTMENTS.register(modEventBus);
        VOEntities.ENTITIES.register(modEventBus);
        VOItems.ITEMS.register(modEventBus);
        VOItems.CONTAINERS.register(modEventBus);
        VOMobEffects.EFFECTS.register(modEventBus);
        
        modEventBus.addListener(this::doCommonSetup);
        modEventBus.addListener(this::doClientSetup);
        modEventBus.addListener(this::doLoadComplete);
        modEventBus.addListener(VODataGenerators::onGatherData);
        modEventBus.addListener(VOCapabilities::onRegisterCapabilities);
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigVO.server_spec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigVO.client_spec);
        modEventBus.addListener(this::onConfigEvent);
		proxy.registerHandlers();
        MinecraftForge.EVENT_BUS.addListener(VODamageSource::livingHurtEvent);
        MinecraftForge.EVENT_BUS.register(this);
	}
	
    private void doCommonSetup(final FMLCommonSetupEvent event)
    {
    	PacketHandler.init();
    	MinecraftForge.EVENT_BUS.register(VOBusServer.class);
    	MinecraftForge.EVENT_BUS.register(SettlementManagerServer.class);
    	MinecraftForge.EVENT_BUS.register(ScentsManager.class);
    	MinecraftForge.EVENT_BUS.register(GroupHandler.class);
    	MinecraftForge.EVENT_BUS.register(TypeBus.class);
    	MinecraftForge.EVENT_BUS.register(FactionBus.class);
    }
    
    @SuppressWarnings("removal")
	private void doClientSetup(final FMLClientSetupEvent event)
    {
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(KeyBindings::registerKeybinds);
        EntityRenderRegistry.registerEntityRenderers();
        ItemBlockRenderTypes.setRenderLayer(VOBlocks.LAYER_SCALE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(VOBlocks.MOSS_BLOCK.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(VOBlocks.TABLE_DRAFTING.get(), RenderType.cutout());
        MinecraftForge.EVENT_BUS.register(VOBusClient.class);
        MinecraftForge.EVENT_BUS.register(SettlementRender.class);
        MinecraftForge.EVENT_BUS.register(BlindRender.class);
        MinecraftForge.EVENT_BUS.register(ScentRender.class);
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
    public void onServerStarting(ServerStartingEvent event)
    {
    	
    }
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event){ VOCommands.init(event); }
	
    @SubscribeEvent
	public void onReloadListenersEvent(AddReloadListenerEvent event)
	{
//		event.addListener(SpeciesRegistry.getInstance());
//		event.addListener(TemplateRegistry.getInstance());
	}
}
