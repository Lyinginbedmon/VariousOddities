package com.lying.variousoddities.client.model.entity;

import com.lying.variousoddities.client.model.EnumLimbPosition;
import com.lying.variousoddities.client.model.ModelUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelMarimo<T extends Entity> extends EntityModel<T>
{
	public ModelRenderer marimo;
	
	public ModelMarimo()
	{
		this.textureHeight = 32;
		this.textureWidth = 64;
		
		this.marimo = ModelUtils.freshRenderer(this);
		this.marimo.rotationPointY = EnumLimbPosition.DOWN.getY() * 20F;
        this.marimo.setTextureOffset(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8, -0.5F);
        this.marimo.setTextureOffset(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
	}
	
	public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
        this.marimo.rotateAngleX = headPitch * 0.017453292F;
        this.marimo.rotateAngleY = netHeadYaw * 0.017453292F;
    }
	
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
    	this.marimo.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
