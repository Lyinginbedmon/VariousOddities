package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelWarg;
import com.lying.variousoddities.client.renderer.entity.layer.LayerWargArmour;
import com.lying.variousoddities.client.renderer.entity.layer.LayerWargChest;
import com.lying.variousoddities.client.renderer.entity.layer.LayerWargDecor;
import com.lying.variousoddities.client.renderer.entity.layer.LayerWargSaddle;
import com.lying.variousoddities.entity.mount.EntityWarg;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityWargRenderer extends MobRenderer<EntityWarg, ModelWarg>
{
	public static final String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/warg/";
	private static final ResourceLocation TEXTURE_BROWN = new ResourceLocation(resourceBase+"warg_brown.png");
	private static final ResourceLocation TEXTURE_BLACK = new ResourceLocation(resourceBase+"warg_black.png");
	private static final ResourceLocation TEXTURE_WHITE = new ResourceLocation(resourceBase+"warg_white.png");
	
	public EntityWargRenderer(EntityRendererProvider.Context manager)
	{
		super(manager, new ModelWarg(manager.bakeLayer(VOModelLayers.WARG)), 1F);
		
		addLayer(new LayerWargChest(this, manager.getModelSet()));
		addLayer(new LayerWargDecor(this, manager.getModelSet()));
		addLayer(new LayerWargArmour(this, manager.getModelSet()));
		addLayer(new LayerWargSaddle(this, manager.getModelSet()));
	}
	
	public ResourceLocation getTextureLocation(EntityWarg entity)
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
	
	public void render(EntityWarg wargIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
	{
		if(wargIn.isWet())
		{
			float f = wargIn.getShadingWhileWet(partialTicks);
			this.model.setColor(f, f, f);
		}
		
		super.render(wargIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		if(wargIn.isWet())
			this.model.setColor(1F, 1F, 1F);
	}
	
    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void scale(EntityWarg wargIn, PoseStack matrixStackIn, float partialTickTime)
    {
    	super.scale(wargIn, matrixStackIn, partialTickTime);
    	float fullScale = 1.75F;
    	matrixStackIn.scale(fullScale, fullScale, fullScale);
    }
}
