package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.model.entity.ModelScorpion;
import com.lying.variousoddities.client.model.entity.ModelScorpionBabies;
import com.lying.variousoddities.client.renderer.entity.EntityScorpionRenderer;
import com.lying.variousoddities.entity.AbstractScorpion;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class LayerScorpionBabies extends LayerRenderer<AbstractScorpion, ModelScorpion>
{
	private final ModelScorpionBabies MODEL = new ModelScorpionBabies();
	private static final ResourceLocation TEXTURE = new ResourceLocation(EntityScorpionRenderer.resourceBase+"babies.png");
	
	public LayerScorpionBabies(IEntityRenderer<AbstractScorpion, ModelScorpion> entityRendererIn)
	{
		super(entityRendererIn);
	}
	
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractScorpion entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(entitylivingbaseIn.getGrowingAge() >= 0 && entitylivingbaseIn.getBabies())
		{
	         this.getEntityModel().copyModelAttributesTo(this.MODEL);
	         this.MODEL.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
	         this.MODEL.setRotationAngles(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
	         IVertexBuilder vertexBuilder = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(TEXTURE));
	         this.MODEL.render(matrixStackIn, vertexBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1.0F);
		}
	}

}
