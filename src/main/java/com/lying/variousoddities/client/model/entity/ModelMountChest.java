package com.lying.variousoddities.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

public abstract class ModelMountChest<T extends Entity> extends EntityModel<T>
{
	protected final ModelPart chestR, chestL;
	protected final ModelPart body;
	
	public ModelMountChest(float scaleIn)
	{
		this.textureHeight = 64;
		this.textureWidth = 64;
		
		this.body = new ModelPart(this);
		this.body.setPos(0F, 11F, 0F);
		this.body.setTextureOffset(15, 20).addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1);
		
		this.chestR = new ModelPart(this, 26, 21);
		this.chestR.setPos(6F, -8F, 0F);
		this.chestR.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);
		this.chestR.yRot = -((float)Math.PI / 2F);
			this.body.addChild(chestR);
		
		this.chestL = new ModelPart(this, 26, 21);
		this.chestL.setPos(-6F, -8F, 0F);
		this.chestL.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);
		this.chestL.yRot = ((float)Math.PI / 2F);
			this.body.addChild(chestL);
	}
	
	public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
	{
		matrixStackIn.pushPose();
			float scale = 0.57F;
			matrixStackIn.scale(scale, scale, scale);
			this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.popPose();
	}
	
	public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		
	}
}
