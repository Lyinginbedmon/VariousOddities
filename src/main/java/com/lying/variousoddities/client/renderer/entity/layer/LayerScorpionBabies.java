package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelScorpion;
import com.lying.variousoddities.client.model.entity.ModelScorpionBabies;
import com.lying.variousoddities.client.renderer.entity.EntityScorpionRenderer;
import com.lying.variousoddities.entity.AbstractScorpion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerScorpionBabies<T extends AbstractScorpion> extends RenderLayer<T, ModelScorpion<T>>
{
	private final ModelScorpionBabies<T> model;
	private static final ResourceLocation TEXTURE = new ResourceLocation(EntityScorpionRenderer.resourceBase+"babies.png");
	
	public LayerScorpionBabies(RenderLayerParent<T, ModelScorpion<T>> entityRendererIn, EntityModelSet modelsIn)
	{
		super(entityRendererIn);
		model = new ModelScorpionBabies<T>(modelsIn.bakeLayer(VOModelLayers.SCORPION_BABIES));
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(entitylivingbaseIn.getAge() >= 0 && entitylivingbaseIn.getBabies())
		{
	         this.getParentModel().copyPropertiesTo(model);
	         this.model.prepareMobModel(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
	         this.model.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
	         VertexConsumer vertexBuilder = bufferIn.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
	         this.model.renderToBuffer(matrixStackIn, vertexBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1.0F);
		}
	}

}
