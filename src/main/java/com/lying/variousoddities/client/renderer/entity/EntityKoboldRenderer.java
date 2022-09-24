package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelKobold;
import com.lying.variousoddities.client.renderer.entity.layer.LayerHeldItemPride;
import com.lying.variousoddities.entity.passive.EntityKobold;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityKoboldRenderer extends MobRenderer<EntityKobold, ModelKobold>
{
	private static final float SCALE = 0.8F;
	
	String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/kobold/kobold_";
	public final ResourceLocation textureBase = new ResourceLocation(resourceBase+"base.png");
	public final ResourceLocation textureBlue = new ResourceLocation(resourceBase+"blue.png");
	public final ResourceLocation textureGreen = new ResourceLocation(resourceBase+"green.png");
	public final ResourceLocation textureOrange = new ResourceLocation(resourceBase+"orange.png");
	public final ResourceLocation textureZombie = new ResourceLocation(resourceBase+"zombie.png");
	
	public EntityKoboldRenderer(EntityRendererProvider.Context manager) 
	{
		super(manager, new ModelKobold(manager.bakeLayer(VOModelLayers.KOBOLD)), 0.5F);
		this.addLayer(new HumanoidArmorLayer<>(this, new ModelKobold(manager.bakeLayer(VOModelLayers.KOBOLD_ARMOR_INNER)), new ModelKobold(manager.bakeLayer(VOModelLayers.KOBOLD_ARMOR_OUTER))));
		this.addLayer(new LayerHeldItemPride<>(this, manager.getItemInHandRenderer()));
		
//        this.addLayer(new LayerArmorKobold(this));
	}
	
	public ResourceLocation getTextureLocation(EntityKobold entity) 
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
    protected void scale(EntityKobold koboldIn, PoseStack matrixStackIn, float partialTickTime)
    {
    	float totalScale = SCALE;
    	if(koboldIn.isBaby())
    	{
    		this.shadowRadius = 0.25F;
        	totalScale = SCALE * (1F-(koboldIn.getGrowth()*koboldIn.getAgeProgress()));
    	}
    	else
    		this.shadowRadius = 0.5F;
    	
    	matrixStackIn.scale(totalScale, totalScale, totalScale);
    }
}
