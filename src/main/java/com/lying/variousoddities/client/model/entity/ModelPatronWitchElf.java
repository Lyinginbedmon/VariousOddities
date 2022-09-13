package com.lying.variousoddities.client.model.entity;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.passive.IChangeling;
import com.lying.variousoddities.entity.wip.EntityPatronWitch;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelPatronWitchElf extends HumanoidModel<EntityPatronWitch> implements IPonytailModel
{
	ModelPart wingRight, wingLeft;
	
	ModelPart bustle1;
	ModelPart bustle2;
	
	public ModelPart ponytail;
	public ModelPart ponytailAnchor;
	public ModelPart ponytailAnchor2;
	
	public ModelPatronWitchElf(ModelPart partsIn)
	{
		super(partsIn);
		
		this.ponytailAnchor = partsIn.getChild("ponytail_anchor_A");
		this.ponytailAnchor2 = this.ponytailAnchor.getChild("ponytail_anchor_B");
        this.ponytail = this.ponytailAnchor2.getChild("ponytail");
		this.bustle1 = this.body.getChild("bustle_A");
		this.bustle2 = this.body.getChild("bustle_B");
		this.wingRight = this.body.getChild("right_wing");
		this.wingLeft = this.body.getChild("left_wing");
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0F);
		PartDefinition part = mesh.getRoot();
		
		PartDefinition ponytailAnchor = part.addOrReplaceChild("ponytail_anchor_A", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 1, 1), PartPose.ZERO);
		PartDefinition ponytailAnchor2 = ponytailAnchor.addOrReplaceChild("ponytail_anchor_B", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 1, 1), PartPose.offset(0, -5, 4));
			ponytailAnchor2.addOrReplaceChild("ponytail", CubeListBuilder.create().texOffs(64, 32).addBox(-5F, 0F, 0F, 10, 16, 1), PartPose.ZERO);
		
		PartDefinition head = part.getChild("head");
			head.addOrReplaceChild("right_ear_A", CubeListBuilder.create().texOffs(24, 0).addBox(-0.5F, 0F, -0.5F, 1, 1, 4), PartPose.offsetAndRotation(-4F, -5.5F, 0F, ModelUtils.toRadians(13D), ModelUtils.toRadians(-22D), 0F));
			head.addOrReplaceChild("right_ear_B", CubeListBuilder.create().texOffs(24, 2).addBox(0F, 0F, 0F, 0, 2, 3), PartPose.offsetAndRotation(-4F, -4.5F, 0F, ModelUtils.toRadians(13D), ModelUtils.toRadians(-22D), 0F));
			head.addOrReplaceChild("left_ear_A", CubeListBuilder.create().texOffs(24, 0).addBox(-0.5F, 0F, -0.5F, 1, 1, 4), PartPose.offsetAndRotation(4F, -5.5F, 0F, ModelUtils.toRadians(13D), ModelUtils.toRadians(22D), 0F));
			head.addOrReplaceChild("left_ear_B", CubeListBuilder.create().texOffs(24, 2).addBox(0F, 0F, 0F, 0, 2, 3), PartPose.offsetAndRotation(4F, -4.5F, 0F, ModelUtils.toRadians(13D), ModelUtils.toRadians(22D), 0F));
		
		PartDefinition body = part.addOrReplaceChild("body", CubeListBuilder.create()
			.texOffs(16, 16).addBox(-4F, 0F, -2F, 8, 5, 4)
			.texOffs(16, 25).addBox(-4F, 0F, -2F, 8, 5, 4, deformation.extend(0.5F))
			.texOffs(16, 34).addBox(-3.5F, 5F, -1.5F, 7, 5, 3), PartPose.ZERO);
			body.addOrReplaceChild("bustle_1", CubeListBuilder.create().texOffs(36, 34).addBox(-4F, 0F, -2F, 8, 1, 4), PartPose.offsetAndRotation(0F, 9F, 0F, ModelUtils.toRadians(4D), 0F, 0F));
			body.addOrReplaceChild("bustle_2", CubeListBuilder.create().texOffs(16, 42).addBox(-4.5F, 0F, -2.5F, 9, 4, 6), PartPose.offsetAndRotation(0F, 0F, 0F, ModelUtils.toRadians(10D), 0F, 0F));
			body.addOrReplaceChild("bustle_A", CubeListBuilder.create().texOffs(0, 52).addBox(-4.5F, 0F, -1F, 9, 5, 6, deformation.extend(0.2F)), PartPose.offsetAndRotation(0F, 12F, 0F, ModelUtils.toRadians(4D), 0F, 0F));
			body.addOrReplaceChild("bustle_B", CubeListBuilder.create().texOffs(30, 52).addBox(-4.5F, 0F, 0F, 9, 6, 6, deformation.extend(0.3F)), PartPose.offsetAndRotation(0F, 3F, 0F, ModelUtils.toRadians(4D), 0F, 0F));
			body.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 32).addBox(-5F, 0F, 0F, 5, 15, 1), PartPose.offset(0F, 4F, 2F));
			body.addOrReplaceChild("left_wing", CubeListBuilder.create().mirror().texOffs(0, 32).addBox(0F, 0F, 0F, 5, 15, 1), PartPose.offset(0F, 4F, 2F));
		
		part.addOrReplaceChild("right_arm", CubeListBuilder.create()
			.texOffs(40, 16).addBox(-2F, -2F, -2F, 3, 12, 4)
			.texOffs(40, 39).addBox(-2F, -2F, -2F, 3, 4, 4, deformation.extend(0.5F)), PartPose.offset(-5F, 2F, 0F));
		
		part.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror()
				.texOffs(40, 16).addBox(-1F, -2F, -2F, 3, 12, 4)
				.texOffs(40, 39).addBox(-1F, -2F, -2F, 3, 4, 4, deformation.extend(0.5F)), PartPose.offset(5F, 2F, 0F));
		
		return LayerDefinition.create(mesh, 64, 64);
	}
	
    public void setupAnim(EntityPatronWitch entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
    	super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	
    	ponytailAnchor.copyFrom(head);
    	ponytail.xRot = Math.max(body.xRot - head.xRot, -0.259F);
    	
    	float bustleX = Math.max(this.leftLeg.xRot, this.rightLeg.xRot) + ModelUtils.toRadians(4D);
    	bustleX += Math.sin(ageInTicks / 35F) * ModelUtils.toRadians(4D);
    	
    	this.bustle1.xRot = bustleX;
    	this.bustle2.xRot = bustleX * 0.5F;
    	
    	this.wingRight.zRot = 0F;
    	if(entityIn instanceof IChangeling)
    	{
    		IChangeling changeling = (IChangeling)entityIn;
    		if(changeling.areWingsFlapping())
		    	this.wingRight.zRot = ModelUtils.toRadians(2D) + (ModelUtils.toRadians(5D) * (float)(Math.sin(changeling.getFlappingTime() * 2) + 1F));
    	}
    	this.wingLeft.zRot = -this.wingRight.zRot;
    	
    	bustleX += ModelUtils.toRadians(17D);
    	this.wingLeft.xRot = bustleX;
    	this.wingRight.xRot = bustleX;
    }
    
    public void setPonytailHeight(float par1Float)
    {
    	this.ponytailAnchor2.y = par1Float;
    }
    
    public void setPonytailRotation(float par1Float, float par2Float, boolean par3Bool)
    {
    	this.ponytail.y = Math.min(5.6F, par2Float / 15F) + (par3Bool ? 3.5F : 0F);
    }
    
    public void renderPonytail(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn)
    {
    	this.ponytailAnchor.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
    }
}
