package com.lying.variousoddities.client.renderer;

import java.util.Map;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.client.renderer.entity.*;
import com.lying.variousoddities.client.renderer.entity.layer.LayerDazed;
import com.lying.variousoddities.client.renderer.entity.layer.LayerEntangled;
import com.lying.variousoddities.client.renderer.entity.layer.LayerFoxAccessories;
import com.lying.variousoddities.client.renderer.entity.layer.LayerGhastlingShoulder;
import com.lying.variousoddities.client.renderer.entity.layer.LayerPetrified;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.FoxModel;
import net.minecraft.client.renderer.entity.model.VillagerModel;
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
	public static void registerEntityRenderers()
	{
		if(ConfigVO.GENERAL.verboseLogs())
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
		registerRenderer(VOEntities.CORPSE, new EntityCorpseRenderer.RenderFactory());
		registerRenderer(VOEntities.CRAB, new EntityCrabRenderer.RenderFactorySmall());
		registerRenderer(VOEntities.CRAB_GIANT, new EntityCrabRenderer.RenderFactoryLarge());
		registerRenderer(VOEntities.WORG, new EntityWorgRenderer.RenderFactory());
		registerRenderer(VOEntities.WARG, new EntityWargRenderer.RenderFactory());
		registerRenderer(VOEntities.GHASTLING, new EntityGhastlingRenderer.RenderFactory());
		
		// WIP mobs to be fleshed out at a later date
		registerRenderer(VOEntities.PATRON_KIRIN, new EntityPatronKirinRenderer.RenderFactory());
		registerRenderer(VOEntities.PATRON_WITCH, new EntityPatronWitchRenderer.RenderFactory());
		registerRenderer(VOEntities.CHANGELING, new EntityChangelingRenderer.RenderFactory());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void appendRenderers(EntityRendererManager renderManager)
	{
		if(ConfigVO.GENERAL.verboseLogs())
			VariousOddities.log.info("Appending layer renderers");
		
		LivingRenderer foxRenderer = (LivingRenderer<FoxEntity, FoxModel<FoxEntity>>)renderManager.renderers.get(EntityType.FOX);
		foxRenderer.addLayer(new LayerFoxAccessories(foxRenderer));
		if(ConfigVO.GENERAL.verboseLogs())
			VariousOddities.log.info("  -Registered fox accessories layer");
		
		Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();
		for(String entry : skinMap.keySet())
		{
			PlayerRenderer renderer = skinMap.get(entry);
			renderer.addLayer(new LayerGhastlingShoulder(renderer));
			if(ConfigVO.GENERAL.verboseLogs())
				VariousOddities.log.info("  -Registered ghastling shoulder layer for "+entry);
			renderer.addLayer(new LayerDazed(renderer));
			renderer.addLayer(new LayerEntangled(renderer));
			renderer.addLayer(new LayerPetrified(renderer));
		}
		
		renderManager.renderers.forEach((type, renderer) -> 
		{
			if(renderer instanceof LivingRenderer)
			{
				LivingRenderer livingRenderer = (LivingRenderer)renderer;
				
				livingRenderer.addLayer(new LayerEntangled(livingRenderer));
				livingRenderer.addLayer(new LayerPetrified(livingRenderer));
				
				if(
						livingRenderer.getEntityModel() instanceof BipedModel ||
						livingRenderer.getEntityModel() instanceof VillagerModel ||
						livingRenderer instanceof IllagerRenderer)
					livingRenderer.addLayer(new LayerDazed(livingRenderer));
			}
		});
	}
	
	private static <T extends Entity> void registerRenderer(EntityType<T> entityClass, IRenderFactory<? super T> renderFactory)
	{
		if(renderFactory == null)
		{
			if(ConfigVO.GENERAL.verboseLogs())
				VariousOddities.log.error("  -# Tried to register null renderer for "+entityClass.getRegistryName()+" #");
		}
		else
		{
			RenderingRegistry.registerEntityRenderingHandler(entityClass, renderFactory);
			if(ConfigVO.GENERAL.verboseLogs())
				VariousOddities.log.info("  -Registered "+entityClass.getRegistryName()+" renderer");
		}
	}
}
