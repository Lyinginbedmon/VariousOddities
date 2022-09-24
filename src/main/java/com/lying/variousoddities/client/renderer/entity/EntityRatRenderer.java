package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelRat;
import com.lying.variousoddities.client.renderer.entity.layer.LayerGlowRat;
import com.lying.variousoddities.entity.AbstractRat;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityRatRenderer<T extends AbstractRat> extends MobRenderer<T, ModelRat<T>>
{
	private final float scale;
	
	public static final String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/rat/rat_";
	
	public EntityRatRenderer(EntityRendererProvider.Context manager, float renderScale) 
	{
		super(manager, new ModelRat<T>(manager.bakeLayer(VOModelLayers.RAT)), 0.5F * (renderScale / 1.5F));
		scale = renderScale;
		addLayer(new LayerGlowRat<T>(this));
	}
	
	public EntityRatRenderer(EntityRendererProvider.Context manager)
	{
		this(manager, 0.6F);
	}
	
	public ResourceLocation getTextureLocation(AbstractRat entity) 
	{
		return new ResourceLocation(resourceBase+entity.getRatBreed().getName()+".png");
	}
	
    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void scale(AbstractRat ratIn, PoseStack matrixStackIn, float partialTickTime)
    {
    	float fullScale = this.scale * ratIn.getRatBreed().getScale();
    	matrixStackIn.scale(fullScale, fullScale, fullScale);
    }
}
