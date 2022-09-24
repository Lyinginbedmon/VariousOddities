package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VOMobEffects;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerEntangled<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> 
{
	private final ResourceLocation entangledTexture = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/entangled.png");
	
	public LayerEntangled(RenderLayerParent<T, M> entityRendererIn)
	{
		super(entityRendererIn);
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		LivingData data = LivingData.forEntity(entitylivingbaseIn);
		if(data == null || !data.getVisualPotion(VOMobEffects.ENTANGLED))
			return;
		
		matrixStackIn.pushPose();
			float scale = 1.01F;
			matrixStackIn.scale(scale, scale, scale);
			matrixStackIn.translate(0F, -0.01F, 0F);
			float f = ageInTicks + partialTicks;
			VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.energySwirl(entangledTexture, Mth.cos(f * 0.01F) * 0.2F, f * 0.01F));
			EntityModel<T> model = getParentModel();
			model.prepareMobModel(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
			model.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
		matrixStackIn.popPose();
	}
}
