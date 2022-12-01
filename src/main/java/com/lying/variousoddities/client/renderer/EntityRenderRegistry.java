package com.lying.variousoddities.client.renderer;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelChangeling;
import com.lying.variousoddities.client.model.entity.ModelCrab;
import com.lying.variousoddities.client.model.entity.ModelCrabBarnacles;
import com.lying.variousoddities.client.model.entity.ModelDazed;
import com.lying.variousoddities.client.model.entity.ModelFoxAccessories;
import com.lying.variousoddities.client.model.entity.ModelGhastling;
import com.lying.variousoddities.client.model.entity.ModelGoblin;
import com.lying.variousoddities.client.model.entity.ModelKobold;
import com.lying.variousoddities.client.model.entity.ModelMarimo;
import com.lying.variousoddities.client.model.entity.ModelMindFlayer;
import com.lying.variousoddities.client.model.entity.ModelPatronKirin;
import com.lying.variousoddities.client.model.entity.ModelPatronKirinHorns;
import com.lying.variousoddities.client.model.entity.ModelPatronWitchChangeling;
import com.lying.variousoddities.client.model.entity.ModelPatronWitchCrone;
import com.lying.variousoddities.client.model.entity.ModelPatronWitchElf;
import com.lying.variousoddities.client.model.entity.ModelPatronWitchHuman;
import com.lying.variousoddities.client.model.entity.ModelRat;
import com.lying.variousoddities.client.model.entity.ModelScorpion;
import com.lying.variousoddities.client.model.entity.ModelScorpionBabies;
import com.lying.variousoddities.client.model.entity.ModelWarg;
import com.lying.variousoddities.client.model.entity.ModelWargChest;
import com.lying.variousoddities.client.model.entity.ModelWargSaddle;
import com.lying.variousoddities.client.model.entity.ModelWorg;
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
import com.lying.variousoddities.entity.hostile.EntityCrabGiant;
import com.lying.variousoddities.entity.hostile.EntityRatGiant;
import com.lying.variousoddities.entity.hostile.EntityScorpionGiant;
import com.lying.variousoddities.entity.passive.EntityCrab;
import com.lying.variousoddities.entity.passive.EntityRat;
import com.lying.variousoddities.entity.passive.EntityScorpion;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityRenderRegistry
{
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
//		if(ConfigVO.GENERAL.verboseLogs())
			VariousOddities.log.info("Registering renderers");
		
		// First release
		event.registerEntityRenderer(VOEntities.SPELL.get(), EntitySpellRenderer::new);
		event.registerEntityRenderer(VOEntities.MARIMO.get(), EntityMarimoRenderer::new);
		event.registerEntityRenderer(VOEntities.KOBOLD.get(), EntityKoboldRenderer::new);
		event.registerEntityRenderer(VOEntities.GOBLIN.get(), EntityGoblinRenderer::new);
		event.registerEntityRenderer(VOEntities.RAT.get(), (manager) -> { return new EntityRatRenderer<EntityRat>(manager, 0.6F); });
		event.registerEntityRenderer(VOEntities.RAT_GIANT.get(), (manager) -> { return new EntityRatRenderer<EntityRatGiant>(manager, 1.6F); });
		event.registerEntityRenderer(VOEntities.SCORPION.get(), (manager) -> { return new EntityScorpionRenderer<EntityScorpion>(manager, 0.6F); });
		event.registerEntityRenderer(VOEntities.SCORPION_GIANT.get(), (manager) -> { return new EntityScorpionRenderer<EntityScorpionGiant>(manager, 1.6F); });
		
		// Second release
		event.registerEntityRenderer(VOEntities.CORPSE.get(), EntityCorpseRenderer::new);
		event.registerEntityRenderer(VOEntities.BODY.get(), EntityBodyRenderer::new);
		event.registerEntityRenderer(VOEntities.CRAB.get(), (manager) -> { return new EntityCrabRenderer<EntityCrab>(manager, 0.5F); });
		event.registerEntityRenderer(VOEntities.CRAB_GIANT.get(), (manager) -> { return new EntityCrabRenderer<EntityCrabGiant>(manager, 1.5F); });
		event.registerEntityRenderer(VOEntities.WORG.get(), EntityWorgRenderer::new);
		event.registerEntityRenderer(VOEntities.WARG.get(), EntityWargRenderer::new);
		event.registerEntityRenderer(VOEntities.GHASTLING.get(), EntityGhastlingRenderer::new);
		
		// WIP mobs to be fleshed out at a later date
		event.registerEntityRenderer(VOEntities.PATRON_KIRIN.get(), EntityPatronKirinRenderer::new);
		event.registerEntityRenderer(VOEntities.PATRON_WITCH.get(), EntityPatronWitchRenderer::new);
		event.registerEntityRenderer(VOEntities.CHANGELING.get(), EntityChangelingRenderer::new);
		event.registerEntityRenderer(VOEntities.MIND_FLAYER.get(), EntityMindFlayerRenderer::new);
	}

	@SubscribeEvent
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void addLayers(EntityRenderersEvent.AddLayers event)
	{
//		if(ConfigVO.GENERAL.verboseLogs())
			VariousOddities.log.info("Appending layer renderers");
		EntityModelSet models = event.getEntityModels();
		
		LivingEntityRenderer foxRenderer = event.getRenderer(EntityType.FOX);
		event.getRenderer(EntityType.FOX).addLayer(new LayerFoxAccessories(foxRenderer, models));
//		if(ConfigVO.GENERAL.verboseLogs())
			VariousOddities.log.info("  -Registered fox accessories layer");
		
		for(String entry : event.getSkins())
		{
			PlayerRenderer renderer = (PlayerRenderer)event.getSkin(entry);
			renderer.addLayer(new LayerGhastlingShoulder(renderer, models));
//			if(ConfigVO.GENERAL.verboseLogs())
				VariousOddities.log.info("  -Registered ghastling shoulder layer for "+entry);
			renderer.addLayer(new LayerDazed(renderer, models));
			renderer.addLayer(new LayerEntangled(renderer));
			renderer.addLayer(new LayerPetrified(renderer));
		}
		
	}
	
//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	public static void appendRenderers(EntityRenderDispatcher renderManager)
//	{
//		renderManager.renderers.forEach((type, renderer) -> 
//		{
//			if(renderer instanceof LivingEntityRenderer)
//			{
//				LivingEntityRenderer livingRenderer = (LivingEntityRenderer)renderer;
//				
//				livingRenderer.addLayer(new LayerEntangled(livingRenderer));
//				livingRenderer.addLayer(new LayerPetrified(livingRenderer));
//				
//				EntityModel model = livingRenderer.getModel();
//				if(
//						model instanceof HumanoidModel ||
//						model instanceof VillagerModel ||
//						livingRenderer instanceof IllagerRenderer)
//					livingRenderer.addLayer(new LayerDazed(livingRenderer, models));
//			}
//		});
//	}

	@SubscribeEvent
	public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
	{
//		if(ConfigVO.GENERAL.verboseLogs())
			VariousOddities.log.info("Registering model layers");
		
		event.registerLayerDefinition(VOModelLayers.KOBOLD, () -> ModelKobold.createBodyLayer(CubeDeformation.NONE));
		event.registerLayerDefinition(VOModelLayers.KOBOLD_ARMOR_INNER, () -> ModelKobold.createBodyLayer(LayerDefinitions.INNER_ARMOR_DEFORMATION));
		event.registerLayerDefinition(VOModelLayers.KOBOLD_ARMOR_OUTER, () -> ModelKobold.createBodyLayer(LayerDefinitions.OUTER_ARMOR_DEFORMATION));
		
		event.registerLayerDefinition(VOModelLayers.GOBLIN, () -> ModelGoblin.createBodyLayer(CubeDeformation.NONE));
		event.registerLayerDefinition(VOModelLayers.GOBLIN_ARMOR_INNER, () -> ModelGoblin.createBodyLayer(LayerDefinitions.INNER_ARMOR_DEFORMATION));
		event.registerLayerDefinition(VOModelLayers.GOBLIN_ARMOR_OUTER, () -> ModelGoblin.createBodyLayer(LayerDefinitions.OUTER_ARMOR_DEFORMATION));
		
		event.registerLayerDefinition(VOModelLayers.MIND_FLAYER, () -> ModelMindFlayer.createBodyLayer(CubeDeformation.NONE, 0F));
		event.registerLayerDefinition(VOModelLayers.MIND_FLAYER_ARMOR_INNER, () -> ModelMindFlayer.createBodyLayer(LayerDefinitions.INNER_ARMOR_DEFORMATION, 0F));
		event.registerLayerDefinition(VOModelLayers.MIND_FLAYER_ARMOR_OUTER, () -> ModelMindFlayer.createBodyLayer(LayerDefinitions.OUTER_ARMOR_DEFORMATION, 0F));
		
		event.registerLayerDefinition(VOModelLayers.WARG, () -> ModelWarg.createBodyLayer(0F, CubeDeformation.NONE));
		event.registerLayerDefinition(VOModelLayers.WARG_ARMOR_INNER, () -> ModelWarg.createBodyLayer(0F, LayerDefinitions.INNER_ARMOR_DEFORMATION));
		event.registerLayerDefinition(VOModelLayers.WARG_ARMOR_OUTER, () -> ModelWarg.createBodyLayer(0F, LayerDefinitions.OUTER_ARMOR_DEFORMATION));
		event.registerLayerDefinition(VOModelLayers.WARG_CHEST, () -> ModelWargChest.createBodyLayer(CubeDeformation.NONE, 0F));
		event.registerLayerDefinition(VOModelLayers.WARG_DECOR, () -> ModelWarg.createBodyLayer(0F, CubeDeformation.NONE));
		event.registerLayerDefinition(VOModelLayers.WARG_SADDLE, () -> ModelWargSaddle.createBodyLayer(CubeDeformation.NONE, 0F));
		event.registerLayerDefinition(VOModelLayers.WORG, () -> ModelWorg.createBodyLayer(CubeDeformation.NONE));
		
		event.registerLayerDefinition(VOModelLayers.CRAB, () -> ModelCrab.createBodyLayer(CubeDeformation.NONE));
		event.registerLayerDefinition(VOModelLayers.CRAB_BARNACLES, () -> ModelCrabBarnacles.createBodyLayer(CubeDeformation.NONE));
		
		event.registerLayerDefinition(VOModelLayers.SCORPION, () -> ModelScorpion.createBodyLayer(CubeDeformation.NONE));
		event.registerLayerDefinition(VOModelLayers.SCORPION_BABIES, () -> ModelScorpionBabies.createBodyLayer(CubeDeformation.NONE));
		
		event.registerLayerDefinition(VOModelLayers.GHASTLING, () -> ModelGhastling.createBodyLayer(CubeDeformation.NONE));
		event.registerLayerDefinition(VOModelLayers.MARIMO, () -> ModelMarimo.createBodyLayer(CubeDeformation.NONE));
		event.registerLayerDefinition(VOModelLayers.RAT, () -> ModelRat.createBodyLayer(CubeDeformation.NONE));
		
		event.registerLayerDefinition(VOModelLayers.PATRON_KIRIN, () -> ModelPatronKirin.createBodyLayer(CubeDeformation.NONE));
		event.registerLayerDefinition(VOModelLayers.PATRON_KIRIN_HORNS, () -> ModelPatronKirinHorns.createBodyLayer(CubeDeformation.NONE));
		
		event.registerLayerDefinition(VOModelLayers.PATRON_WITCH_HUMAN, () -> ModelPatronWitchHuman.createBodyLayer(CubeDeformation.NONE));
		event.registerLayerDefinition(VOModelLayers.PATRON_WITCH_ELF, () -> ModelPatronWitchElf.createBodyLayer(CubeDeformation.NONE));
		event.registerLayerDefinition(VOModelLayers.PATRON_WITCH_CRONE, () -> ModelPatronWitchCrone.createBodyLayer(CubeDeformation.NONE));
		event.registerLayerDefinition(VOModelLayers.PATRON_WITCH_CHANGELING, () -> ModelPatronWitchChangeling.createBodyLayer(CubeDeformation.NONE));
		
		event.registerLayerDefinition(VOModelLayers.CHANGELING, () -> ModelChangeling.createBodyLayer(CubeDeformation.NONE));
//		event.registerLayerDefinition(VOModelLayers.CHANGELING_ELF, () -> ModelChangelingElf.createBodyLayer(CubeDeformation.NONE));
		
		event.registerLayerDefinition(VOModelLayers.DAZED, () -> ModelDazed.createBodyLayer(CubeDeformation.NONE));
		event.registerLayerDefinition(VOModelLayers.FOX_ACCESSORIES, () -> ModelFoxAccessories.createBodyLayer(CubeDeformation.NONE, 0F));
	}
}
