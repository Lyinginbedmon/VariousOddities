package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.model.entity.ModelWarg;
import com.lying.variousoddities.entity.mount.EntityWarg;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@OnlyIn(Dist.CLIENT)
public class EntityWargRenderer extends MobRenderer<EntityWarg, ModelWarg>
{
	public static final String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/warg/warg_";
	private static final ResourceLocation TEXTURE_BROWN = new ResourceLocation(resourceBase+"brown.png");
	private static final ResourceLocation TEXTURE_BLACK = new ResourceLocation(resourceBase+"black.png");
	private static final ResourceLocation TEXTURE_WHITE = new ResourceLocation(resourceBase+"white.png");
	
	public EntityWargRenderer(EntityRendererManager manager)
	{
		super(manager, new ModelWarg(), 1F);
	}
	
	public ResourceLocation getEntityTexture(EntityWarg entity)
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
	
	public void render(EntityWarg wargIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		if(wargIn.isWet())
		{
			float f = wargIn.getShadingWhileWet(partialTicks);
			this.entityModel.setTint(f, f, f);
		}
		
		super.render(wargIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		if(wargIn.isWet())
			this.entityModel.setTint(1F, 1F, 1F);
	}
	
    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void preRenderCallback(EntityWarg wargIn, MatrixStack matrixStackIn, float partialTickTime)
    {
    	super.preRenderCallback(wargIn, matrixStackIn, partialTickTime);
    	float fullScale = 1.75F;
    	matrixStackIn.scale(fullScale, fullScale, fullScale);
    }
	
	public static class RenderFactory implements IRenderFactory<EntityWarg>
	{
		public EntityRenderer<? super EntityWarg> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityWargRenderer(manager);
		}
	}
}
