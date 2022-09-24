package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelScorpion;
import com.lying.variousoddities.client.renderer.entity.layer.LayerScorpionBabies;
import com.lying.variousoddities.entity.AbstractScorpion;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityScorpionRenderer<T extends AbstractScorpion> extends MobRenderer<T, ModelScorpion<T>>
{
	private final float scale;
	
	public static final String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/scorpion/scorpion_";
	public static final ResourceLocation TEXTURE_BABY = new ResourceLocation(resourceBase+"child.png");
	
	public EntityScorpionRenderer(EntityRendererProvider.Context manager, float renderScale) 
	{
		super(manager, new ModelScorpion<T>(manager.bakeLayer(VOModelLayers.SCORPION)), 0.5F * (renderScale / 1.5F));
		scale = renderScale;
		addLayer(new LayerScorpionBabies<T>(this, manager.getModelSet()));
	}
	
	public EntityScorpionRenderer(EntityRendererProvider.Context manager)
	{
		this(manager, 0.6F);
	}
	
	public ResourceLocation getTextureLocation(AbstractScorpion entity) 
	{
		return entity.isBaby() ? TEXTURE_BABY : entity.getScorpionType().getTexture();
	}
	
    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void scale(AbstractScorpion scorpionIn, PoseStack matrixStackIn, float partialTickTime)
    {
    	if(scorpionIn.isBaby())
    	{
    		int growingAge = Math.abs(scorpionIn.getAge());
    		float age = 1 - ((float)growingAge / 2400F);
    		float childScale = Math.max(0.6F, Math.min(1.0F, age)) * this.scale;
        	matrixStackIn.scale(childScale, childScale, childScale);
    	}
    	else
    		matrixStackIn.scale(scale, scale, scale);
    }
}
