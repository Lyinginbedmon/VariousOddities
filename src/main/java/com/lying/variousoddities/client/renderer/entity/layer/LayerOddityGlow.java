package com.lying.variousoddities.client.renderer.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerOddityGlow<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M>
{
    private final ResourceLocation theTexture;
    
	public LayerOddityGlow(RenderLayerParent<T, M> entityRendererIn, ResourceLocation textureIn)
	{
		super(entityRendererIn);
    	theTexture = textureIn;
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
	      VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.eyes(getTexture(entitylivingbaseIn)));
	      this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	protected ResourceLocation getTexture(T entitylivingbaseIn){ return theTexture; }
}
