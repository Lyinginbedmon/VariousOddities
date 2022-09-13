package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelWarg;
import com.lying.variousoddities.client.model.entity.ModelWargSaddle;
import com.lying.variousoddities.client.renderer.entity.EntityWargRenderer;
import com.lying.variousoddities.entity.mount.EntityWarg;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class LayerWargSaddle extends RenderLayer<EntityWarg, ModelWarg>
{
	private final ModelWargSaddle model;
	private static final ResourceLocation TEXTURE = new ResourceLocation(EntityWargRenderer.resourceBase+"saddle.png");
	
	public LayerWargSaddle(RenderLayerParent<EntityWarg, ModelWarg> entityRendererIn, EntityModelSet modelsIn)
	{
		super(entityRendererIn);
		this.model = new ModelWargSaddle(modelsIn.bakeLayer(VOModelLayers.WARG_SADDLE));
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, EntityWarg entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(!entitylivingbaseIn.isSaddled())
			return;
		
        this.getParentModel().copyPropertiesTo(this.model);
        model.prepareMobModel(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer vertexBuilder = bufferIn.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderToBuffer(matrixStackIn, vertexBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1.0F);
	}
}
