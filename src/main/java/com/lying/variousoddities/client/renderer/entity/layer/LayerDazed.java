package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelDazed;
import com.lying.variousoddities.init.VOMobEffects;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class LayerDazed<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> 
{
	private final ModelDazed<T> dazedModel;
	private final ResourceLocation dazedTextured = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/dazed.png");
	
	public LayerDazed(RenderLayerParent<T, M> entityRendererIn, EntityModelSet modelsIn)
	{
		super(entityRendererIn);
		this.dazedModel = new ModelDazed<T>(modelsIn.bakeLayer(VOModelLayers.DAZED));
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(VOMobEffects.isPotionVisible(entityIn, VOMobEffects.DAZED))
		{
			matrixStackIn.pushPose();
				float scale = 1.2F;
				matrixStackIn.scale(scale, scale, scale);
				matrixStackIn.translate(0D, -0.8D, 0D);
				
				RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
				RenderSystem.disableBlend();
				VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.entityCutout(dazedTextured));
				dazedModel.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
				dazedModel.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 0.8F);
				RenderSystem.enableBlend();
			matrixStackIn.popPose();
		}
	}
}
