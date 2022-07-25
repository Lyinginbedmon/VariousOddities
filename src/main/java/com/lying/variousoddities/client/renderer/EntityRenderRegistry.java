package com.lying.variousoddities.client.renderer;

import java.util.Map;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.client.renderer.entity.EntityBodyRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityChangelingRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityCorpseRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityCrabRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityGhastlingRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityGoblinRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityKoboldRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityMarimoRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityMindFlayerRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityPatronKirinRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityPatronWitchRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityRatRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityScorpionRenderer;
import com.lying.variousoddities.client.renderer.entity.EntitySpellRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityWargRenderer;
import com.lying.variousoddities.client.renderer.entity.EntityWorgRenderer;
import com.lying.variousoddities.client.renderer.entity.layer.LayerDazed;
import com.lying.variousoddities.client.renderer.entity.layer.LayerEntangled;
import com.lying.variousoddities.client.renderer.entity.layer.LayerFoxAccessories;
import com.lying.variousoddities.client.renderer.entity.layer.LayerGhastlingShoulder;
import com.lying.variousoddities.client.renderer.entity.layer.LayerPetrified;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Fox;
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
		registerRenderer(VOEntities.BODY, new EntityBodyRenderer.RenderFactory());
		registerRenderer(VOEntities.CRAB, new EntityCrabRenderer.RenderFactorySmall());
		registerRenderer(VOEntities.CRAB_GIANT, new EntityCrabRenderer.RenderFactoryLarge());
		registerRenderer(VOEntities.WORG, new EntityWorgRenderer.RenderFactory());
		registerRenderer(VOEntities.WARG, new EntityWargRenderer.RenderFactory());
		registerRenderer(VOEntities.GHASTLING, new EntityGhastlingRenderer.RenderFactory());
		
		// WIP mobs to be fleshed out at a later date
		registerRenderer(VOEntities.PATRON_KIRIN, new EntityPatronKirinRenderer.RenderFactory());
		registerRenderer(VOEntities.PATRON_WITCH, new EntityPatronWitchRenderer.RenderFactory());
		registerRenderer(VOEntities.CHANGELING, new EntityChangelingRenderer.RenderFactory());
		registerRenderer(VOEntities.MIND_FLAYER, new EntityMindFlayerRenderer.RenderFactory());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void appendRenderers(EntityRenderDispatcher renderManager)
	{
		if(ConfigVO.GENERAL.verboseLogs())
			VariousOddities.log.info("Appending layer renderers");
		
		LivingEntityRenderer foxRenderer = (LivingEntityRenderer<Fox, FoxModel<Fox>>)renderManager.renderers.get(EntityType.FOX);
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
			if(renderer instanceof LivingEntityRenderer)
			{
				LivingEntityRenderer livingRenderer = (LivingEntityRenderer)renderer;
				
				livingRenderer.addLayer(new LayerEntangled(livingRenderer));
				livingRenderer.addLayer(new LayerPetrified(livingRenderer));
				
				if(
						livingRenderer.getEntityModel() instanceof HumanoidModel ||
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
