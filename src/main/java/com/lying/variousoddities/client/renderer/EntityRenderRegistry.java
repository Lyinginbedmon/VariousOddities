package com.lying.variousoddities.client.renderer;

import java.util.Map;

import javax.annotation.Nonnull;

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
import com.lying.variousoddities.entity.hostile.EntityCrabGiant;
import com.lying.variousoddities.entity.hostile.EntityRatGiant;
import com.lying.variousoddities.entity.hostile.EntityScorpionGiant;
import com.lying.variousoddities.entity.passive.EntityCrab;
import com.lying.variousoddities.entity.passive.EntityRat;
import com.lying.variousoddities.entity.passive.EntityScorpion;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

@OnlyIn(Dist.CLIENT)
public class EntityRenderRegistry
{
	public static void registerEntityRenderers()
	{
		if(ConfigVO.GENERAL.verboseLogs())
			VariousOddities.log.info("Registering renderers");
		
		// First release
		registerRenderer(VOEntities.SPELL.get(), EntitySpellRenderer::new);
		registerRenderer(VOEntities.MARIMO.get(), EntityMarimoRenderer::new);
		registerRenderer(VOEntities.KOBOLD.get(), EntityKoboldRenderer::new);
		registerRenderer(VOEntities.GOBLIN.get(), EntityGoblinRenderer::new);
		registerRenderer(VOEntities.RAT.get(), (manager) -> { return new EntityRatRenderer<EntityRat>(manager, 0.6F); });
		registerRenderer(VOEntities.RAT_GIANT.get(), (manager) -> { return new EntityRatRenderer<EntityRatGiant>(manager, 1.6F); });
		registerRenderer(VOEntities.SCORPION.get(), (manager) -> { return new EntityScorpionRenderer<EntityScorpion>(manager, 0.6F); });
		registerRenderer(VOEntities.SCORPION_GIANT.get(), (manager) -> { return new EntityScorpionRenderer<EntityScorpionGiant>(manager, 1.6F); });
		
		// Second release
		registerRenderer(VOEntities.CORPSE.get(), EntityCorpseRenderer::new);
		registerRenderer(VOEntities.BODY.get(), EntityBodyRenderer::new);
		registerRenderer(VOEntities.CRAB.get(), (manager) -> { return new EntityCrabRenderer<EntityCrab>(manager, 0.5F); });
		registerRenderer(VOEntities.CRAB_GIANT.get(), (manager) -> { return new EntityCrabRenderer<EntityCrabGiant>(manager, 1.5F); });
		registerRenderer(VOEntities.WORG.get(), EntityWorgRenderer::new);
		registerRenderer(VOEntities.WARG.get(), EntityWargRenderer::new);
		registerRenderer(VOEntities.GHASTLING.get(), EntityGhastlingRenderer::new);
		
		// WIP mobs to be fleshed out at a later date
		registerRenderer(VOEntities.PATRON_KIRIN.get(), EntityPatronKirinRenderer::new);
		registerRenderer(VOEntities.PATRON_WITCH.get(), EntityPatronWitchRenderer::new);
		registerRenderer(VOEntities.CHANGELING.get(), EntityChangelingRenderer::new);
		registerRenderer(VOEntities.MIND_FLAYER.get(), EntityMindFlayerRenderer::new);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void appendRenderers(EntityRenderDispatcher renderManager)
	{
		if(ConfigVO.GENERAL.verboseLogs())
			VariousOddities.log.info("Appending layer renderers");
		
		EntityModelSet models = Minecraft.getInstance().getEntityModels();
		LivingEntityRenderer foxRenderer = (LivingEntityRenderer<Fox, FoxModel<Fox>>)renderManager.renderers.get(EntityType.FOX);
		foxRenderer.addLayer(new LayerFoxAccessories(foxRenderer, models));
		if(ConfigVO.GENERAL.verboseLogs())
			VariousOddities.log.info("  -Registered fox accessories layer");
		
		Map<String, EntityRenderer<? extends Player>> skinMap = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap();
		for(String entry : skinMap.keySet())
		{
			PlayerRenderer renderer = (PlayerRenderer)skinMap.get(entry);
			renderer.addLayer(new LayerGhastlingShoulder(renderer, models));
			if(ConfigVO.GENERAL.verboseLogs())
				VariousOddities.log.info("  -Registered ghastling shoulder layer for "+entry);
			renderer.addLayer(new LayerDazed(renderer, models));
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
				
				EntityModel model = livingRenderer.getModel();
				if(
						model instanceof HumanoidModel ||
						model instanceof VillagerModel ||
						livingRenderer instanceof IllagerRenderer)
					livingRenderer.addLayer(new LayerDazed(livingRenderer, models));
			}
		});
	}
	
	private static <T extends Entity> void registerRenderer(@Nonnull EntityType<T> entityClass, EntityRendererProvider<T> renderFactory)
	{
		ResourceLocation className = ForgeRegistries.ENTITY_TYPES.getKey(entityClass);
		if(renderFactory == null)
		{
			if(ConfigVO.GENERAL.verboseLogs())
				VariousOddities.log.error("  -# Tried to register null renderer for "+className+" #");
		}
		else
		{
			EntityRenderers.register(entityClass, renderFactory);
			if(ConfigVO.GENERAL.verboseLogs())
				VariousOddities.log.info("  -Registered "+className+" renderer");
		}
	}
}
