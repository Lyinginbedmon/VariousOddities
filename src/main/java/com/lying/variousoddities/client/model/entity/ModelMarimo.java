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

public class ModelMarimo<T extends Entity> extends EntityModel<T>
{
	public ModelPart marimo;
	
	public ModelMarimo(ModelPart partsIn)
	{
		this.marimo = partsIn.getChild("body");
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		part.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8F, 8F, 8F, deformation.extend(-0.5F))
				.texOffs(32, 0).addBox(-4F, -4F, -4F, 8, 8, 8), PartPose.offset(0F, 20F, 0F));
		return LayerDefinition.create(mesh, 64, 32);
	}
	
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
        this.marimo.xRot = headPitch * 0.017453292F;
        this.marimo.yRot = netHeadYaw * 0.017453292F;
    }
	
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
    	this.marimo.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
