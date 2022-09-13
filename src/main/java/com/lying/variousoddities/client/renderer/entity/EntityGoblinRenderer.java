package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelGoblin;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@OnlyIn(Dist.CLIENT)
public class EntityGoblinRenderer extends MobRenderer<EntityGoblin, ModelGoblin>
{
	private static final float SCALE = 0.8F;
	
	String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/goblin/goblin_";
	public final ResourceLocation textureBase = new ResourceLocation(resourceBase+"base.png");
	public final ResourceLocation textureBlue = new ResourceLocation(resourceBase+"flesh.png");
	public final ResourceLocation textureGreen = new ResourceLocation(resourceBase+"green.png");
	public final ResourceLocation textureOrange = new ResourceLocation(resourceBase+"red.png");
	
	public EntityGoblinRenderer(EntityRendererProvider.Context manager) 
	{
		super(manager, new ModelGoblin(manager.bakeLayer(VOModelLayers.GOBLIN)), 0.5F);
		this.addLayer(new HumanoidArmorLayer<>(this, new ModelGoblin(manager.bakeLayer(VOModelLayers.GOBLIN_ARMOR_INNER)), new ModelGoblin(manager.bakeLayer(VOModelLayers.GOBLIN_ARMOR_OUTER))));
	    this.addLayer(new ItemInHandLayer<>(this, manager.getItemInHandRenderer()));
	}
	
	public ResourceLocation getTextureLocation(EntityGoblin entity) 
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
    protected void scale(EntityGoblin goblinIn, PoseStack matrixStackIn, float partialTickTime)
    {
    	float totalScale = SCALE;
    	if(goblinIn.isBaby())
    	{
    		this.shadowRadius = 0.25F;
        	totalScale = SCALE * (1F-(goblinIn.getGrowth()*goblinIn.getAgeProgress()));
    	}
    	else
    		this.shadowRadius = 0.5F;
    	
    	matrixStackIn.scale(totalScale, totalScale, totalScale);
    }
	
	public static class RenderFactory implements IRenderFactory<EntityGoblin>
	{
		public EntityRenderer<? super EntityGoblin> createRenderFor(EntityRendererProvider.Context manager) 
		{
			return new EntityGoblinRenderer(manager);
		}
		
	}
}
