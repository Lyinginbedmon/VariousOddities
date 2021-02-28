package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.model.entity.ModelGoblin;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityGoblinRenderer extends MobRenderer<EntityGoblin, ModelGoblin>
{
	private static final float SCALE = 0.8F;
	
	String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/goblin/goblin_";
	public final ResourceLocation textureBase = new ResourceLocation(resourceBase+"base.png");
	public final ResourceLocation textureBlue = new ResourceLocation(resourceBase+"flesh.png");
	public final ResourceLocation textureGreen = new ResourceLocation(resourceBase+"green.png");
	public final ResourceLocation textureOrange = new ResourceLocation(resourceBase+"red.png");
	
	public EntityGoblinRenderer(EntityRendererManager manager) 
	{
		super(manager, new ModelGoblin(), 0.5F);
		this.addLayer(new BipedArmorLayer<>(this, new ModelGoblin(0.5F), new ModelGoblin(1.0F)));
	    this.addLayer(new HeldItemLayer<>(this));
//        this.addLayer(new LayerArmorGoblin(this));
	}
	
	public ResourceLocation getEntityTexture(EntityGoblin entity) 
	{
		switch(entity.getColor())
		{
			case 0: return textureBlue;
			case 1: return textureGreen;
			case 2: return textureOrange;
			default: return textureBase;
		}
	}
	
    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void preRenderCallback(EntityGoblin goblinIn, MatrixStack matrixStackIn, float partialTickTime)
    {
    	float totalScale = SCALE;
    	if(goblinIn.isChild())
    	{
    		this.shadowSize = 0.25F;
        	totalScale = SCALE * (1F-(goblinIn.getGrowth()*goblinIn.getAgeProgress()));
    	}
    	else
    		this.shadowSize = 0.5F;
    	
    	matrixStackIn.scale(totalScale, totalScale, totalScale);
    }
	
	public static class RenderFactory implements IRenderFactory<EntityGoblin>
	{
		public EntityRenderer<? super EntityGoblin> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityGoblinRenderer(manager);
		}
		
	}
}
