package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.model.entity.ModelRat;
import com.lying.variousoddities.client.renderer.entity.layer.LayerGlowRat;
import com.lying.variousoddities.entity.AbstractRat;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityRatRenderer extends MobRenderer<AbstractRat, ModelRat>
{
	private final float scale;
	
	String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/rat/rat_";
	
	public EntityRatRenderer(EntityRendererManager manager, float renderScale) 
	{
		super(manager, new ModelRat(), 0.5F * (renderScale / 1.5F));
		scale = renderScale;
		
		addLayer(new LayerGlowRat(this));
	}
	
	public EntityRatRenderer(EntityRendererManager manager)
	{
		this(manager, 0.6F);
	}
	
	public ResourceLocation getEntityTexture(AbstractRat entity) 
	{
		return new ResourceLocation(resourceBase+entity.getRatBreed().getName()+".png");
	}
	
    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void preRenderCallback(AbstractRat ratIn, MatrixStack matrixStackIn, float partialTickTime)
    {
    	float fullScale = this.scale * ratIn.getRatBreed().getScale();
    	matrixStackIn.scale(fullScale, fullScale, fullScale);
    }
	
	public static class RenderFactorySmall implements IRenderFactory<AbstractRat>
	{
		public EntityRenderer<? super AbstractRat> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityRatRenderer(manager);
		}
	}
	
	public static class RenderFactoryLarge implements IRenderFactory<AbstractRat>
	{
		public EntityRenderer<? super AbstractRat> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityRatRenderer(manager, 1.6F);
		}
	}
}
