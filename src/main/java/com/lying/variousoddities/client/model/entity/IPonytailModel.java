package com.lying.variousoddities.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public interface IPonytailModel
{
	public void setPonytailHeight(float par1Float);
	
	public void setPonytailRotation(float par1Float, float par2Float, boolean par3Bool);
	
	public void renderPonytail(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn);
}
