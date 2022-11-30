package com.lying.variousoddities.client;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.client.renderer.tileentity.TileEntityDraftingTableRenderer;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.init.VOBlockEntities;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;

@OnlyIn(Dist.CLIENT)
public class RendererHandler
{
	private static boolean registered = false;
	
	public static void registerTileRenderers(ModelEvent.RegisterAdditional event)
	{
		if(ConfigVO.GENERAL.verboseLogs())
			VariousOddities.log.info("Registering tile entity renderers");
		
		if(!registered)
			registered = true;
		
		BlockEntityRenderers.register(VOBlockEntities.TABLE_DRAFTING.get(), TileEntityDraftingTableRenderer::new);
	}
}
