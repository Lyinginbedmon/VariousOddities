package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelWorg;
import com.lying.variousoddities.client.renderer.entity.layer.LayerHeldItemWorg;
import com.lying.variousoddities.entity.passive.EntityWorg;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@OnlyIn(Dist.CLIENT)
public class EntityWorgRenderer extends MobRenderer<EntityWorg, ModelWorg>
{
	private static final String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/worg/worg_";
	private static final ResourceLocation TEXTURE_BROWN = new ResourceLocation(resourceBase+"brown.png");
	private static final ResourceLocation TEXTURE_BLACK = new ResourceLocation(resourceBase+"black.png");
	private static final ResourceLocation TEXTURE_WHITE = new ResourceLocation(resourceBase+"white.png");
	
	public EntityWorgRenderer(EntityRendererProvider.Context context)
	{
		super(context, new ModelWorg(context.bakeLayer(VOModelLayers.WORG)), 0.5F);
		
		this.addLayer(new LayerHeldItemWorg(this, context.getItemInHandRenderer()));
	}
	
	public ResourceLocation getTextureLocation(EntityWorg entity)
	{
		switch(entity.getColor())
		{
			case 1:
				return TEXTURE_BROWN;
			case 2:
				return TEXTURE_WHITE;
			default:
				return TEXTURE_BLACK;
		}
	}
	
	public void render(EntityWorg worgIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
	{
		if(worgIn.isWet())
		{
			float f = worgIn.getShadingWhileWet(partialTicks);
			this.model.setColor(f, f, f);
		}
		
		super.render(worgIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		if(worgIn.isWet())
			this.model.setColor(1F, 1F, 1F);
	}
	
    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void scale(EntityWorg worgIn, PoseStack matrixStackIn, float partialTickTime)
    {
    	super.scale(worgIn, matrixStackIn, partialTickTime);
    	float fullScale = 1.15F;
    	matrixStackIn.scale(fullScale, fullScale, fullScale);
    }
	
	public static class RenderFactory implements IRenderFactory<EntityWorg>
	{
		public EntityRenderer<? super EntityWorg> createRenderFor(EntityRendererProvider.Context manager) 
		{
			return new EntityWorgRenderer(manager);
		}
	}
}
