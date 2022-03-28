package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.model.entity.ModelMindFlayer;
import com.lying.variousoddities.entity.hostile.EntityMindFlayer;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityMindFlayerRenderer extends BipedRenderer<EntityMindFlayer, ModelMindFlayer>
{
	public EntityMindFlayerRenderer(EntityRendererManager rendererManager)
	{
		super(rendererManager, new ModelMindFlayer(0F), 0.5F);
		this.addLayer(new BipedArmorLayer<>(this, new BipedModel<EntityMindFlayer>(0.5F), new BipedModel<EntityMindFlayer>(1F)));
	}
	
	public ResourceLocation getEntityTexture(EntityMindFlayer entity)
	{
		return new ResourceLocation(Reference.ModInfo.MOD_ID,"textures/entity/mind_flayer/mind_flayer.png");
	}
	
	public static class RenderFactory implements IRenderFactory<EntityMindFlayer>
	{
		public EntityRenderer<? super EntityMindFlayer> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityMindFlayerRenderer(manager);
		}
	}
}
