package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelCrab;
import com.lying.variousoddities.client.renderer.entity.layer.LayerCrabBarnacles;
import com.lying.variousoddities.entity.AbstractCrab;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityCrabRenderer<T extends AbstractCrab> extends MobRenderer<T, ModelCrab<T>>
{
	private final float scale;
	
	public static final String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/crab/crab_";
	private static final ResourceLocation TEXTURE_RED = new ResourceLocation(resourceBase+"red.png");
	private static final ResourceLocation TEXTURE_GREEN = new ResourceLocation(resourceBase+"green.png");
	private static final ResourceLocation TEXTURE_BLUE = new ResourceLocation(resourceBase+"blue.png");
	
	public EntityCrabRenderer(EntityRendererProvider.Context manager, float renderScale) 
	{
		super(manager, new ModelCrab<T>(manager.bakeLayer(VOModelLayers.CRAB)), 0.5F * (renderScale / 1.5F));
		scale = renderScale;
		addLayer(new LayerCrabBarnacles<T>(this, manager.getModelSet()));
	}
	
	public EntityCrabRenderer(EntityRendererProvider.Context manager)
	{
		this(manager, 0.5F);
	}
	
	public ResourceLocation getTextureLocation(T entity) 
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
    protected void scale(T ratIn, PoseStack matrixStackIn, float partialTickTime)
    {
    	matrixStackIn.scale(scale, scale, scale);
    }
}
