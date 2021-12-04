package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.model.entity.ModelDazed;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

public class LayerDazed<T extends LivingEntity, M extends BipedModel<T>> extends LayerRenderer<T, M> 
{
	private final ModelDazed<T> dazedModel = new ModelDazed<T>();
	private final ResourceLocation dazedTextured = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/dazed.png");
	
	public LayerDazed(IEntityRenderer<T, M> entityRendererIn)
	{
		super(entityRendererIn);
	}
	
	@SuppressWarnings("deprecation")
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(VOPotions.isPotionVisible(entityIn, VOPotions.DAZED))
		{
			matrixStackIn.push();
				float scale = 1.2F;
				matrixStackIn.scale(scale, scale, scale);
				matrixStackIn.translate(0D, -0.8D, 0D);
				
				RenderSystem.color4f(1F, 1F, 1F, 1F);
				RenderSystem.disableBlend();
				IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEntityCutout(dazedTextured));
				dazedModel.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
				dazedModel.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 0.8F);
				RenderSystem.enableBlend();
			matrixStackIn.pop();
		}
	}
}
