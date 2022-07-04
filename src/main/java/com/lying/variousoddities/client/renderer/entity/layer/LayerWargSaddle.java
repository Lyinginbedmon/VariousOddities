package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.model.entity.ModelWarg;
import com.lying.variousoddities.client.model.entity.ModelWargSaddle;
import com.lying.variousoddities.client.renderer.entity.EntityWargRenderer;
import com.lying.variousoddities.entity.mount.EntityWarg;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class LayerWargSaddle extends LayerRenderer<EntityWarg, ModelWarg>
{
	private final ModelWargSaddle MODEL = new ModelWargSaddle(0.3F);
	private static final ResourceLocation TEXTURE = new ResourceLocation(EntityWargRenderer.resourceBase+"saddle.png");
	
	public LayerWargSaddle(IEntityRenderer<EntityWarg, ModelWarg> entityRendererIn)
	{
		super(entityRendererIn);
	}
	
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityWarg entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(!entitylivingbaseIn.isSaddled())
			return;
		
        this.getEntityModel().copyModelAttributesTo(this.MODEL);
        MODEL.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
        MODEL.setRotationAngles(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        IVertexBuilder vertexBuilder = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(TEXTURE));
        MODEL.render(matrixStackIn, vertexBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1.0F);
	}
}
