package com.lying.variousoddities.client.model.entity;

import java.util.Random;

import com.lying.variousoddities.entity.passive.EntityGhastling;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModelGhastling extends HierarchicalModel<EntityGhastling>
{
	private final ModelPart body;
	private final ModelPart[] tentacles = new ModelPart[6];
	
	private static final float TENT_SPREAD = 2F;
	
	public ModelGhastling(ModelPart partsIn)
	{
		body = partsIn.getChild("body");
		for(int i=0; i<6; ++i)
			tentacles[i] = body.getChild("tentacle_"+i);
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		PartDefinition body = part.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -4F, -4F, 8, 8, 8, deformation), PartPose.offset(0F, 21F, 0F));
		Random rand = new Random(1660L);
			for(int i=0; i<6; ++i)
				body.addOrReplaceChild("tentacle_"+i, CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0F, -0.5F, 1, rand.nextInt(3) + 4, 1, deformation.extend(0.7F)), PartPose.offset((((float)(i % 3) - (float)(i / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * TENT_SPREAD, ((float)(i / 3) / 2.0F * 2.0F - 1.0F) * TENT_SPREAD, 2F));
		
		return LayerDefinition.create(mesh, 32, 32);
	}
	
	public ModelPart root()
	{
		return body;
	}
	
	public void setupAnim(EntityGhastling entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		this.body.xRot = headPitch * ((float)Math.PI / 180F);
		this.body.yRot = netHeadYaw * ((float)Math.PI / 180F);
		for(int i = 0; i < this.tentacles.length; ++i)
			this.tentacles[i].xRot = 0.2F * Mth.sin(ageInTicks * 0.3F + (float)i) + 0.4F;
	}
	
	public void renderOnShoulder(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, int ageInTicks)
	{
		this.setupAnim(null, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		this.root().render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
	}
}
