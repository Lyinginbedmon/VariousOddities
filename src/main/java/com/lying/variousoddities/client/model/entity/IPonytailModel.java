package com.lying.variousoddities.client.model.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

public interface IPonytailModel
{
	public void setPonytailHeight(float par1Float);
	
	public void setPonytailRotation(float par1Float, float par2Float, boolean par3Bool);
	
	public void renderPonytail(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn);
}
