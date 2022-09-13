package com.lying.variousoddities.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.passive.IChangeling;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public class ModelChangeling<T extends LivingEntity> extends HumanoidModel<T>
{
	ModelPart wingRight, wingLeft;
	
	public ModelChangeling(ModelPart partsIn)
	{
		super(partsIn);
		this.wingRight = this.body.getChild("right_wing");
		this.wingLeft = this.body.getChild("left_wing");
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		return LayerDefinition.create(createMesh(deformation), 64, 64);
	}
	
	public static MeshDefinition createMesh(CubeDeformation deformation)
	{
		MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0F);
		PartDefinition part = mesh.getRoot();
		
		PartDefinition head = part.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
			head.addOrReplaceChild("head0", CubeListBuilder.create().texOffs(19, 0).addBox(-1.5F, -3F, -1.5F, 3, 4, 3), PartPose.rotation(ModelUtils.toRadians(12D), 0F, 0F));
			head.addOrReplaceChild("head1", CubeListBuilder.create()
	        	.texOffs(0, 0).addBox(-2.5F, -7F, -6.5F, 5, 5, 9)
	        	.texOffs(28, 0).addBox(-2.5F, -7F, -6.5F, 5, 5, 9, deformation.extend(0.5F))
	        	.texOffs(31, 0).addBox(-0.5F, -11F, -3F, 1, 4, 1)
	        	.texOffs(47, -5).addBox(0F, -8.5F, 0F, 0, 8, 5), PartPose.rotation(ModelUtils.toRadians(17D), 0F, 0F));
		
		PartDefinition body = part.addOrReplaceChild("body", CubeListBuilder.create()
			.texOffs(0, 14).addBox(-4F, 0F, -2F, 8, 6, 4)
			.texOffs(0, 24).addBox(-4F, 0F, -2F, 8, 6, 4, deformation.extend(0.5F))
			.texOffs(0, 34).addBox(-3F, 6F, -1.25F, 6, 6, 3)
			.texOffs(0, 43).addBox(-2F, 6F, -1.5F, 4, 5, 1), PartPose.ZERO);
				body.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(10, 43).addBox(-2.5F, 0F, 0F, 5, 15, 1), PartPose.offsetAndRotation(-2.5F, 0F, 2F, ModelUtils.toRadians(6D), 0F, ModelUtils.toRadians(2D)));
				body.addOrReplaceChild("left_wing", CubeListBuilder.create().mirror().texOffs(10, 43).addBox(-2.5F, 0F, 0F, 5, 15, 1), PartPose.offsetAndRotation(2.5F, 0F, 2F, ModelUtils.toRadians(6D), 0F, ModelUtils.toRadians(-2D)));

		PartDefinition rightArm = part.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(50, 24).addBox(-2.5F, 5F, -1F, 3, 6, 3), PartPose.offset(-5.0F, 2.0F, 0.0F));
			rightArm.addOrReplaceChild("child", CubeListBuilder.create().texOffs(50, 14).addBox(-1.0F, -2.0F, -1.0F, 2, 8, 2), PartPose.rotation(ModelUtils.toRadians(6D), 0F, ModelUtils.toRadians(8D)));
		
		PartDefinition leftArm = part.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror().texOffs(50, 24).addBox(-0.5F, 5F, -1F, 3, 6, 3), PartPose.offset(5.0F, 2.0F, 0.0F));
			leftArm.addOrReplaceChild("child", CubeListBuilder.create().mirror().texOffs(50, 14).addBox(-1.0F, -2.0F, -1.0F, 2, 8, 2), PartPose.rotation(ModelUtils.toRadians(6D), 0F, ModelUtils.toRadians(-8D)));
		
		PartDefinition rightLeg = part.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-2.4F, 12.0F, 0.0F));
			rightLeg.addOrReplaceChild("thigh", CubeListBuilder.create().texOffs(24, 14).addBox(-2F, -2F, -6.5F, 4, 3, 9), PartPose.rotation(ModelUtils.toRadians(35D), 0F, 0F));
			rightLeg.addOrReplaceChild("ankle", CubeListBuilder.create().texOffs(24, 26).addBox(-1.5F, 3.5F, -2F, 3, 2, 7), PartPose.rotation(ModelUtils.toRadians(-30D), 0F, 0F));
			rightLeg.addOrReplaceChild("foot", CubeListBuilder.create()
		        .texOffs(24, 35).addBox(-1F, 3.5F, -11.75F, 2, 2, 8)
		        .texOffs(44, 35).addBox(-1F, 3.5F, -11.75F, 2, 2, 8, deformation.extend(0.5F)), PartPose.rotation(ModelUtils.toRadians(70D), 0F, 0F));
		
		PartDefinition leftLeg = part.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(2.4F, 12F, 0F));
			leftLeg.addOrReplaceChild("thigh", CubeListBuilder.create().mirror().texOffs(24, 14).addBox(-2F, -2F, -6.5F, 4, 3, 9), PartPose.rotation(ModelUtils.toRadians(35D), 0F, 0F));
			leftLeg.addOrReplaceChild("ankle", CubeListBuilder.create().mirror().texOffs(24, 26).addBox(-1.5F, 3.5F, -2F, 3, 2, 7), PartPose.rotation(ModelUtils.toRadians(-30D), 0F, 0F));
			leftLeg.addOrReplaceChild("foot", CubeListBuilder.create().mirror()
		        .texOffs(24, 35).addBox(-1F, 3.5F, -11.75F, 2, 2, 8)
		        .texOffs(44, 35).addBox(-1F, 3.5F, -11.75F, 2, 2, 8, deformation.extend(0.5F)), PartPose.rotation(ModelUtils.toRadians(70D), 0F, 0F));
		
		return mesh;
	}
	
	protected Iterable<ModelPart> headParts()
	{
	   return ImmutableList.of(this.head);
	}
	
	protected Iterable<ModelPart> bodyParts()
	{
		return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg);
	}
	
    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
    	super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	
    	this.body.xRot += ModelUtils.toRadians(8D);
    	
    	this.wingRight.zRot = ModelUtils.toRadians(2D);
    	if(entityIn instanceof IChangeling)
    	{
    		IChangeling changeling = (IChangeling)entityIn;
    		if(changeling.areWingsFlapping())
		    	this.wingRight.zRot = ModelUtils.toRadians(2D) + (ModelUtils.toRadians(5D) * (float)(Math.sin(changeling.getFlappingTime() * 2) + 1F));
    	}
    	this.wingLeft.zRot = -this.wingRight.zRot;
    	
    	this.leftLeg.z += 2F;
    	this.rightLeg.z += 2F;
    }
}
