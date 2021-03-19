package com.lying.variousoddities.client;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.client.renderer.tileentity.TileEntityDraftingTableRenderer;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.init.VOTileEntities;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

@OnlyIn(Dist.CLIENT)
public class RendererHandler
{
	private static boolean registered = false;
	
	public static void registerTileRenderers(ModelRegistryEvent event)
	{
		if(ConfigVO.GENERAL.verboseLogs())
			VariousOddities.log.info("Registering tile entity renderers");
		
		if(!registered)
			registered = true;
		
		ClientRegistry.bindTileEntityRenderer(VOTileEntities.TABLE_DRAFTING, TileEntityDraftingTableRenderer::new);
	}
}
