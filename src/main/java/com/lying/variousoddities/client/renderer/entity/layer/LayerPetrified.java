package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VOMobEffects;
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
public class LayerPetrified<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> 
{
	private final ResourceLocation petrifiedTexture = new ResourceLocation("textures/block/stone.png");
	
	public LayerPetrified(RenderLayerParent<T, M> entityRendererIn)
	{
		super(entityRendererIn);
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		LivingData data = LivingData.getCapability(entitylivingbaseIn);
		if(data == null || !data.getVisualPotion(VOMobEffects.PETRIFIED.get()))
			return;
		
		matrixStackIn.pushPose();
			VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.entitySolid(petrifiedTexture));
			EntityModel<T> model = getParentModel();
			model.prepareMobModel(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
			model.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
		matrixStackIn.popPose();
	}
}
