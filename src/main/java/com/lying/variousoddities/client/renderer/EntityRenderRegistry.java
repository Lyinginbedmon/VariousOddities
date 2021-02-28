package com.lying.variousoddities.client.renderer;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.client.renderer.entity.*;
import com.lying.variousoddities.client.renderer.entity.layer.LayerFoxAccessories;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingRenderer;
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
//		registerRenderer(VOEntities.CRAB, new EntityCrabRenderer.RenderFactorySmall());
//		registerRenderer(VOEntities.CRAB_GIANT, new EntityCrabRenderer.RenderFactoryLarge());
//		registerRenderer(VOEntities.WORG, new EntityWorgRenderer.RenderFactory());
		
		LivingRenderer foxRenderer = ((LivingRenderer<FoxEntity, FoxModel<FoxEntity>>)Minecraft.getInstance().getRenderManager().renderers.get(EntityType.FOX));
		foxRenderer.addLayer(new LayerFoxAccessories(foxRenderer));
		VariousOddities.log.info("  -Registered fox accessories layer");
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
