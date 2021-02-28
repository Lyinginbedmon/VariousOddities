package com.lying.variousoddities.client.model.entity;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.AbstractCrab;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelCrabBarnacles extends EntityModel<AbstractCrab>
{
	ModelRenderer body;
	
	public ModelCrabBarnacles()
	{
		this.textureHeight = 16;
		this.textureWidth = 16;
		this.body = ModelUtils.freshRenderer(this);
		
		ModelRenderer barnacles1 = ModelUtils.freshRenderer(this);
		barnacles1.setRotationPoint(6.5F, 9.25F, 6F);
		barnacles1.rotateAngleX = -ModelUtils.degree10;
		barnacles1.setTextureOffset(0, 0).addBox(-1F, 0F, -1F, 2, 3, 2);
		barnacles1.setTextureOffset(0, 5).addBox(0.25F, 1F, -3F, 2, 2, 2);
		barnacles1.setTextureOffset(0, 10).addBox(-2F, 0.5F, 0.5F, 2, 2, 2);
		barnacles1.setTextureOffset(0, 0).addBox(-0.5F, 1.5F, -0.5F, 2, 1, 2, 0.2F);
		
		ModelRenderer barnacles2 = ModelUtils.freshRenderer(this);
		barnacles2.setRotationPoint(-5F, 9F, -3F);
		barnacles2.rotateAngleX = -ModelUtils.degree10;
		barnacles2.rotateAngleY = -ModelUtils.degree10 * 1.5F;
		barnacles2.setTextureOffset(0, 0).addBox(-1.75F, -1F, -0.75F, 2, 2, 2);
		barnacles2.setTextureOffset(0, 5).addBox(-3F, 0F, -2.5F, 2, 3, 2, 0.2F);
		barnacles2.setTextureOffset(0, 10).addBox(1F, 0F, 0F, 2, 3, 2);
		
		ModelRenderer barnacles3 = ModelUtils.freshRenderer(this);
		barnacles3.setRotationPoint(0F, 9F, 0F);
		barnacles3.rotateAngleX = -(ModelUtils.degree5 * 3F);
		barnacles3.setTextureOffset(0, 0).addBox(1F, 0F, 2F, 2, 3, 2);
		barnacles3.setTextureOffset(0, 5).addBox(-3F, -1F, 5F, 2, 3, 2, -0.2F);
		
		this.body.addChild(barnacles1);
		this.body.addChild(barnacles2);
		this.body.addChild(barnacles3);
	}
	
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
	{
		this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	public void setRotationAngles(AbstractCrab entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){ }

}
