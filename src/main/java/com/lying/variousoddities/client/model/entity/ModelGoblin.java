package com.lying.variousoddities.client.model.entity;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.hostile.EntityGoblin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModelGoblin extends HumanoidModel<EntityGoblin>
{
	HeadHandler head;
	
	public ModelGoblin(ModelPart partsIn)
	{
		super(partsIn);
		this.head = new HeadHandler(partsIn);
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0F);
		PartDefinition part = mesh.getRoot();
		
		part.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -7.0F, -3.5F, 8, 8, 8, deformation.extend(0.5F)), PartPose.ZERO);
		
		HeadHandler.createHead(part);
		
		part.addOrReplaceChild("body", CubeListBuilder.create()
			.texOffs(0, 24).addBox(-4F, 0F, -2F, 8, 7, 4)
			.texOffs(24, 24).addBox(-3F, 6.8F, -1.5F, 6, 5, 3), PartPose.ZERO);
		
		PartDefinition rightArm = part.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
			rightArm.addOrReplaceChild("upper", CubeListBuilder.create().mirror().texOffs(52, 38).addBox(-1.5F, -0.5F, -1.5F, 3, 7, 3, deformation.extend(-0.2F)), PartPose.rotation(ModelUtils.degree10 * 1.5F, 0F, ModelUtils.degree10 * 2F));
			rightArm.addOrReplaceChild("lower", CubeListBuilder.create().mirror().texOffs(48, 48).addBox(-2F, 0F, -2F, 4, 8, 4), PartPose.offsetAndRotation(-2F, 5F, 1.5F, -ModelUtils.degree5, 0F, 0F));
		
		PartDefinition leftArm = part.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
			leftArm.addOrReplaceChild("upper", CubeListBuilder.create().mirror().texOffs(52, 16).addBox(-1.5F, -0.5F, -1.5F, 3, 7, 3, deformation.extend(-0.2F)), PartPose.rotation(ModelUtils.toRadians(15D), 0F, ModelUtils.toRadians(-20D)));
			leftArm.addOrReplaceChild("lower", CubeListBuilder.create().mirror().texOffs(48, 26).addBox(-2F, 0F, -2F, 4, 8, 4), PartPose.offsetAndRotation(2F, 5F, 1.5F, -ModelUtils.degree5, 0F, 0F));
		
		PartDefinition rightLeg = part.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
			rightLeg.addOrReplaceChild("thigh", CubeListBuilder.create().texOffs(20, 35).addBox(-1.5F, -2F, -1.5F, 3, 7, 3, deformation.extend(-0.1F)), PartPose.offsetAndRotation(-1F, 0F, 0F, 0F, 0F, ModelUtils.degree5));
			rightLeg.addOrReplaceChild("shin", CubeListBuilder.create().texOffs(20, 45).addBox(-2.5F, 0F, -2.5F, 5, 8, 5), PartPose.offset(-1.5F, 4F, 0F));
		
		PartDefinition leftLeg = part.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror(), PartPose.offset(1.9F, 12F, 0F));
			leftLeg.addOrReplaceChild("thigh", CubeListBuilder.create().mirror().texOffs(0, 35).addBox(-1.5F, -2F, -1.5F, 3, 7, 3, deformation.extend(-0.1F)), PartPose.offsetAndRotation(1F, 0F, 0F, 0F, 0F, -ModelUtils.degree5));
			leftLeg.addOrReplaceChild("shin", CubeListBuilder.create().mirror().texOffs(0, 45).addBox(-2.5F, 0F, -2.5F, 5, 8, 5), PartPose.offset(1.5F, 4F, 0F));
		
		return LayerDefinition.create(mesh, 64, 64);
	}
	
    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setupAnim(EntityGoblin entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
		super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	
    	this.head.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	this.head.setEars(entityIn.getEars());
    	this.head.setNose(entityIn.getNose());
    }
	
	public class HeadHandler
	{
		private static final float earFlare = ModelUtils.toRadians(30D);
		
		ModelPart head;
		ModelPart ears;
		ModelPart nose1, nose2;
		
		public HeadHandler(ModelPart partsIn)
		{
			this.head = partsIn.getChild("head");
			this.ears = this.head.getChild("ears");
			this.nose1 = this.head.getChild("nose_A");
			this.nose2 = this.head.getChild("nose_B");
		}
		
		public static void createHead(PartDefinition part)
		{
			PartDefinition head = part.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -3.5F, 8, 7, 7), PartPose.ZERO);
				PartDefinition ears = head.addOrReplaceChild("ears", CubeListBuilder.create(), PartPose.ZERO);
					ears.addOrReplaceChild("left_ear", CubeListBuilder.create().mirror()
						.texOffs(21, 16).addBox(-0.5F, 0F, -0.5F, 1, 7, 1)
						.texOffs(25, 11).addBox(0F, -0.5F, -5F, 0, 7, 5), PartPose.offsetAndRotation(-3.5F, -6F, -2F, ModelUtils.toRadians(105D), -earFlare, 0F));
					ears.addOrReplaceChild("right_ear", CubeListBuilder.create()
						.texOffs(35, 16).addBox(-0.5F, 0F, -0.5F, 1, 7, 1)
						.texOffs(39, 11).addBox(0F, -0.5F, -5F, 0, 7, 5), PartPose.offsetAndRotation(3.5F, -6F, -2F, ModelUtils.toRadians(105D), earFlare, 0F));
				head.addOrReplaceChild("nose_A", CubeListBuilder.create().texOffs(0, 16).addBox(-2.5F, -4F, -3.7F, 5, 4, 1), PartPose.offsetAndRotation(0F, -0.5F, 0F, ModelUtils.degree10, 0F, 0F));
				head.addOrReplaceChild("nose_B", CubeListBuilder.create().texOffs(12, 16).addBox(-1F, 0F, -5F, 2, 6, 2), PartPose.offsetAndRotation(0F, -3F, 0.5F, ModelUtils.toRadians(-30D), 0F, 0F));
		}
		
	    public void setupAnim(EntityGoblin entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
		{
	        float f = 0.01F * (float)(entityIn.getId() % 10);
	        this.nose2.xRot = -(float)(Math.toRadians(30D)) + (Mth.sin((float)entityIn.tickCount * f) * 4.5F * 0.017453292F);
	        this.nose2.yRot = 0.0F;
	        this.nose2.zRot = Mth.cos((float)entityIn.tickCount * f) * 2.5F * 0.017453292F;
		}
		
		public void setEars(boolean par1Bool){ ears.visible = par1Bool; }
		public void setNose(boolean par1Bool)
		{
			nose1.visible = par1Bool;
			nose2.visible = !par1Bool;
		}
	}
}
