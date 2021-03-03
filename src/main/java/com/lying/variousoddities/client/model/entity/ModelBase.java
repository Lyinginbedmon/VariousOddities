package com.lying.variousoddities.client.model.entity;

import com.lying.variousoddities.client.model.ModelUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBase<T extends Entity> extends EntityModel<T>
{
	ModelRenderer body;
	
	public ModelBase()
	{
		this.textureHeight = 64;
		this.textureWidth = 64;
		
		this.body = ModelUtils.freshRenderer(this);
		this.body.rotationPointY = 17F;
		this.body.setTextureOffset(0, 0).addBox(-7F, -4F, -6F, 14, 6, 12, 1.5F);
		this.body.setTextureOffset(0, 0).addBox(-5F, -2F, -7.8F, 2, 2, 1, 0.2F);
		this.body.setTextureOffset(46, 0).addBox(3.5F, -2F, -7.8F, 2, 2, 1, 0.2F);
		
		ModelRenderer carapace = ModelUtils.freshRenderer(this);
		carapace.rotationPointY = -3.5F;
		carapace.rotationPointZ = 1.7F;
		carapace.rotateAngleX = -ModelUtils.degree10;
		carapace.setTextureOffset(0, 18).addBox(-8F, -1.5F, -8F, 16, 3, 15, 1.5F);
		this.body.addChild(carapace);
	}
	
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
	{
		this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		
	}
}
