package com.lying.variousoddities.client.renderer.entity.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class LayerOddityGlow<T extends Entity, M extends EntityModel<T>> extends LayerRenderer<T, M>
{
    private final ResourceLocation theTexture;
    
	public LayerOddityGlow(IEntityRenderer<T, M> entityRendererIn, ResourceLocation textureIn)
	{
		super(entityRendererIn);
    	theTexture = textureIn;
	}
	
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
	      IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEyes(getTexture(entitylivingbaseIn)));
	      this.getEntityModel().render(matrixStackIn, ivertexbuilder, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	protected ResourceLocation getTexture(T entitylivingbaseIn){ return theTexture; }
}
