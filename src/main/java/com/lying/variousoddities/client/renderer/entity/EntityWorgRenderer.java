package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.model.entity.ModelWorg;
import com.lying.variousoddities.entity.passive.EntityWorg;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityWorgRenderer extends MobRenderer<EntityWorg, ModelWorg>
{
	private static final String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/worg/worg_";
	private static final ResourceLocation TEXTURE_BROWN = new ResourceLocation(resourceBase+"brown.png");
	private static final ResourceLocation TEXTURE_BLACK = new ResourceLocation(resourceBase+"black.png");
	private static final ResourceLocation TEXTURE_WHITE = new ResourceLocation(resourceBase+"white.png");
	
	public EntityWorgRenderer(EntityRendererManager p_i50961_1)
	{
		super(p_i50961_1, new ModelWorg(), 0.5F);
	}
	
	public ResourceLocation getEntityTexture(EntityWorg entity)
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
	
	public void render(EntityWorg worgIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		if(worgIn.isWet())
		{
			float f = worgIn.getShadingWhileWet(partialTicks);
			this.entityModel.setTint(f, f, f);
		}
		
		super.render(worgIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		if(worgIn.isWet())
			this.entityModel.setTint(1F, 1F, 1F);
	}
	
    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void preRenderCallback(EntityWorg worgIn, MatrixStack matrixStackIn, float partialTickTime)
    {
    	super.preRenderCallback(worgIn, matrixStackIn, partialTickTime);
    	float fullScale = 1.15F;
    	matrixStackIn.scale(fullScale, fullScale, fullScale);
    }
	
	public static class RenderFactory implements IRenderFactory<EntityWorg>
	{
		public EntityRenderer<? super EntityWorg> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityWorgRenderer(manager);
		}
	}
}
