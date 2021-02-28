package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.model.entity.ModelScorpion;
import com.lying.variousoddities.client.renderer.entity.layer.LayerScorpionBabies;
import com.lying.variousoddities.entity.AbstractScorpion;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityScorpionRenderer extends MobRenderer<AbstractScorpion, ModelScorpion>
{
	private final float scale;
	
	public static final String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/scorpion/scorpion_";
	public static final ResourceLocation resourceBaby = new ResourceLocation(resourceBase+"child.png");
	
	public EntityScorpionRenderer(EntityRendererManager manager, float renderScale) 
	{
		super(manager, new ModelScorpion(), 0.5F * (renderScale / 1.5F));
		scale = renderScale;
	}
	
	public EntityScorpionRenderer(EntityRendererManager manager)
	{
		this(manager, 0.6F);
		addLayer(new LayerScorpionBabies(this));
	}
	
	public ResourceLocation getEntityTexture(AbstractScorpion entity) 
	{
		return entity.getGrowingAge() < 0 ? resourceBaby : entity.getScorpionType().getTexture();
	}
	
    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void preRenderCallback(AbstractScorpion scorpionIn, MatrixStack matrixStackIn, float partialTickTime)
    {
    	if(scorpionIn.isChild())
    	{
    		int growingAge = Math.abs(scorpionIn.getGrowingAge());
    		float age = 1 - ((float)growingAge / 2400F);
    		float childScale = Math.max(0.6F, Math.min(1.0F, age)) * this.scale;
        	matrixStackIn.scale(childScale, childScale, childScale);
    	}
    	else
    		matrixStackIn.scale(scale, scale, scale);
    }
	
	public static class RenderFactorySmall implements IRenderFactory<AbstractScorpion>
	{
		public EntityRenderer<? super AbstractScorpion> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityScorpionRenderer(manager);
		}
	}
	
	public static class RenderFactoryLarge implements IRenderFactory<AbstractScorpion>
	{
		public EntityRenderer<? super AbstractScorpion> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityScorpionRenderer(manager, 1.6F);
		}
	}
}
