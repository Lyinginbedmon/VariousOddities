package com.lying.variousoddities.client.model.entity;

import com.lying.variousoddities.client.model.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

public class ModelBase<T extends Entity> extends EntityModel<T>
{
	ModelPart body;
	
	public ModelBase()
	{
		this.textureHeight = 64;
		this.textureWidth = 64;
		
		this.body = ModelUtils.freshRenderer(this);
		this.body.y = 17F;
		this.body.setTextureOffset(0, 0).addBox(-7F, -4F, -6F, 14, 6, 12, 1.5F);
		this.body.setTextureOffset(0, 0).addBox(-5F, -2F, -7.8F, 2, 2, 1, 0.2F);
		this.body.setTextureOffset(46, 0).addBox(3.5F, -2F, -7.8F, 2, 2, 1, 0.2F);
		
		ModelPart carapace = ModelUtils.freshRenderer(this);
		carapace.y = -3.5F;
		carapace.z = 1.7F;
		carapace.rotateAngleX = -ModelUtils.degree10;
		carapace.setTextureOffset(0, 18).addBox(-8F, -1.5F, -8F, 16, 3, 15, 1.5F);
		this.body.addChild(carapace);
	}
	
	public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
	{
		this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		
	}
}
