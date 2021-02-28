package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.model.entity.ModelKobold;
import com.lying.variousoddities.client.renderer.entity.layer.LayerHeldItemPride;
import com.lying.variousoddities.entity.passive.EntityKobold;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityKoboldRenderer extends MobRenderer<EntityKobold, ModelKobold>
{
	private static final float SCALE = 0.8F;
	
	String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/kobold/kobold_";
	public final ResourceLocation textureBase = new ResourceLocation(resourceBase+"base.png");
	public final ResourceLocation textureBlue = new ResourceLocation(resourceBase+"blue.png");
	public final ResourceLocation textureGreen = new ResourceLocation(resourceBase+"green.png");
	public final ResourceLocation textureOrange = new ResourceLocation(resourceBase+"orange.png");
	public final ResourceLocation textureZombie = new ResourceLocation(resourceBase+"zombie.png");
	
	public EntityKoboldRenderer(EntityRendererManager manager) 
	{
		super(manager, new ModelKobold(), 0.5F);
		this.addLayer(new BipedArmorLayer<>(this, new ModelKobold(0.5F), new ModelKobold(1.0F)));
		this.addLayer(new LayerHeldItemPride<>(this));
		
//        this.addLayer(new LayerArmorKobold(this));
	}
	
	public ResourceLocation getEntityTexture(EntityKobold entity) 
	{
		switch(entity.getColor())
		{
			case 0: return textureBlue;
			case 1: return textureGreen;
			case 2: return textureOrange;
			case 3: return textureZombie;
			default: return textureBase;
		}
	}
	
    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void preRenderCallback(EntityKobold koboldIn, MatrixStack matrixStackIn, float partialTickTime)
    {
    	float totalScale = SCALE;
    	if(koboldIn.isChild())
    	{
    		this.shadowSize = 0.25F;
        	totalScale = SCALE * (1F-(koboldIn.getGrowth()*koboldIn.getAgeProgress()));
    	}
    	else
    		this.shadowSize = 0.5F;
    	
    	matrixStackIn.scale(totalScale, totalScale, totalScale);
    }
	
	public static class RenderFactory implements IRenderFactory<EntityKobold>
	{
		public EntityRenderer<? super EntityKobold> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityKoboldRenderer(manager);
		}
	}
}
