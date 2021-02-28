package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.model.entity.ModelCrab;
import com.lying.variousoddities.client.renderer.entity.layer.LayerCrabBarnacles;
import com.lying.variousoddities.entity.AbstractCrab;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityCrabRenderer extends MobRenderer<AbstractCrab, ModelCrab>
{
	private final float scale;
	
	public static final String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/crab/crab_";
	private static final ResourceLocation TEXTURE_RED = new ResourceLocation(Reference.ModInfo.MOD_ID, resourceBase+"red.png");
	private static final ResourceLocation TEXTURE_GREEN = new ResourceLocation(Reference.ModInfo.MOD_ID, resourceBase+"green.png");
	private static final ResourceLocation TEXTURE_BLUE = new ResourceLocation(Reference.ModInfo.MOD_ID, resourceBase+"blue.png");
	
	public EntityCrabRenderer(EntityRendererManager manager, float renderScale) 
	{
		super(manager, new ModelCrab(), 0.5F * (renderScale / 1.5F));
		scale = renderScale;
		addLayer(new LayerCrabBarnacles(this));
	}
	
	public EntityCrabRenderer(EntityRendererManager manager)
	{
		this(manager, 0.5F);
	}
	
	public ResourceLocation getEntityTexture(AbstractCrab entity) 
	{
		switch(entity.getColor())
		{
			case 1: 	return TEXTURE_GREEN;
			case 2: 	return TEXTURE_BLUE;
			default:	return TEXTURE_RED;
		}
	}
	
    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void preRenderCallback(AbstractCrab ratIn, MatrixStack matrixStackIn, float partialTickTime)
    {
    	matrixStackIn.scale(scale, scale, scale);
    }
	
	public static class RenderFactorySmall implements IRenderFactory<AbstractCrab>
	{
		public EntityRenderer<? super AbstractCrab> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityCrabRenderer(manager);
		}
	}
	
	public static class RenderFactoryLarge implements IRenderFactory<AbstractCrab>
	{
		public EntityRenderer<? super AbstractCrab> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityCrabRenderer(manager, 1.5F);
		}
	}
}
