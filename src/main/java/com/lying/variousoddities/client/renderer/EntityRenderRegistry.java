package com.lying.variousoddities.client.renderer;

import java.util.Map;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.client.renderer.entity.EntityCrabRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityGhastlingRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityGoblinRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityKoboldRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityMarimoRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityRatRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityScorpionRenderer;
import com.lying.variousoddities.client.renderer.entity.EntitySpellRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityWargRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityWorgRenderer;
import com.lying.variousoddities.client.renderer.entity.layer.LayerGhastlingShoulder;
import com.lying.variousoddities.client.renderer.entity.layer.LayerFoxAccessories;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.FoxModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

@OnlyIn(Dist.CLIENT)
public class EntityRenderRegistry
{
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void registerEntityRenderers()
	{
		VariousOddities.log.info("Registering renderers");
		
		// First release
		registerRenderer(VOEntities.SPELL, new EntitySpellRenderer.RenderFactory());
		registerRenderer(VOEntities.MARIMO, new EntityMarimoRenderer.RenderFactory());
		registerRenderer(VOEntities.KOBOLD, new EntityKoboldRenderer.RenderFactory());
		registerRenderer(VOEntities.GOBLIN, new EntityGoblinRenderer.RenderFactory());
		registerRenderer(VOEntities.RAT, new EntityRatRenderer.RenderFactorySmall());
		registerRenderer(VOEntities.RAT_GIANT, new EntityRatRenderer.RenderFactoryLarge());
		registerRenderer(VOEntities.SCORPION, new EntityScorpionRenderer.RenderFactorySmall());
		registerRenderer(VOEntities.SCORPION_GIANT, new EntityScorpionRenderer.RenderFactoryLarge());
		
		// Second release
		registerRenderer(VOEntities.CRAB, new EntityCrabRenderer.RenderFactorySmall());
		registerRenderer(VOEntities.CRAB_GIANT, new EntityCrabRenderer.RenderFactoryLarge());
		registerRenderer(VOEntities.WORG, new EntityWorgRenderer.RenderFactory());
		registerRenderer(VOEntities.WARG, new EntityWargRenderer.RenderFactory());
		registerRenderer(VOEntities.GHASTLING, new EntityGhastlingRenderer.RenderFactory());
		
		LivingRenderer foxRenderer = (LivingRenderer<FoxEntity, FoxModel<FoxEntity>>)Minecraft.getInstance().getRenderManager().renderers.get(EntityType.FOX);
		foxRenderer.addLayer(new LayerFoxAccessories(foxRenderer));
		VariousOddities.log.info("  -Registered fox accessories layer");
		
		Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();
		for(String entry : skinMap.keySet())
		{
			PlayerRenderer renderer = skinMap.get(entry);
			renderer.addLayer(new LayerGhastlingShoulder(renderer));
			VariousOddities.log.info("  -Registered ghastling shoulder layer for "+entry);
		}
	}
	
	private static <T extends Entity> void registerRenderer(EntityType<T> entityClass, IRenderFactory<? super T> renderFactory)
	{
		if(renderFactory == null)
			VariousOddities.log.error("  -# Tried to register null renderer for "+entityClass.getRegistryName()+" #");
		else
		{
			RenderingRegistry.registerEntityRenderingHandler(entityClass, renderFactory);
			VariousOddities.log.info("  -Registered "+entityClass.getRegistryName()+" renderer");
		}
	}
}
