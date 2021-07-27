package com.lying.variousoddities.client.model.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

public abstract class ModelMountChest<T extends Entity> extends EntityModel<T>
{
	protected final ModelRenderer chestR, chestL;
	protected final ModelRenderer body;
	
	public ModelMountChest(float scaleIn)
	{
		this.textureHeight = 64;
		this.textureWidth = 64;
		
		this.body = new ModelRenderer(this);
		this.body.setRotationPoint(0F, 11F, 0F);
		this.body.setTextureOffset(15, 20).addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1);
		
		this.chestR = new ModelRenderer(this, 26, 21);
		this.chestR.setRotationPoint(6F, -8F, 0F);
		this.chestR.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);
		this.chestR.rotateAngleY = -((float)Math.PI / 2F);
			this.body.addChild(chestR);
		
		this.chestL = new ModelRenderer(this, 26, 21);
		this.chestL.setRotationPoint(-6F, -8F, 0F);
		this.chestL.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);
		this.chestL.rotateAngleY = ((float)Math.PI / 2F);
			this.body.addChild(chestL);
	}
	
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
	{
		matrixStackIn.push();
			float scale = 0.57F;
			matrixStackIn.scale(scale, scale, scale);
			this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.pop();
	}
	
	public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		
	}
}
