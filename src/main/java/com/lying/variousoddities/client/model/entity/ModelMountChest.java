package com.lying.variousoddities.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

public abstract class ModelMountChest<T extends Entity> extends EntityModel<T>
{
	protected final ModelPart chestR, chestL;
	protected final ModelPart body;
	
	public ModelMountChest(ModelPart partsIn)
	{
		this.body = partsIn.getChild("body");
		this.chestR = partsIn.getChild("right_chest");
		this.chestL = partsIn.getChild("left_chest");
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation, float sizeIn)
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		PartDefinition body = part.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(15, 20).addBox(-0.5F, -0.5F, -0.5F, 1F, 1F, 1F, deformation), PartPose.offset(0F, 11F, 0F));
			body.addOrReplaceChild("right_chest", CubeListBuilder.create().texOffs(26, 21).addBox(-4F, 0F, -2F, 8F, 8F, 3F, deformation), PartPose.offsetAndRotation(6F, -8F, 0F, 0F, -((float)Math.PI / 2F), 0F));
			body.addOrReplaceChild("left_chest", CubeListBuilder.create().texOffs(26, 21).addBox(-4F, 0F, -2F, 8F, 8F, 3F, deformation), PartPose.offsetAndRotation(-6F, -8F, 0F, 0F, ((float)Math.PI / 2F), 0F));
		return LayerDefinition.create(mesh, 64, 64);
	}
	
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
	{
		matrixStackIn.pushPose();
			float scale = 0.57F;
			matrixStackIn.scale(scale, scale, scale);
			this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.popPose();
	}
	
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		
	}
}
