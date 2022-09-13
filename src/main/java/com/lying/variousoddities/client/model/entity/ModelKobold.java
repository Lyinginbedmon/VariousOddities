package com.lying.variousoddities.client.model.entity;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.passive.EntityKobold;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelKobold extends HumanoidModel<EntityKobold>
{
	private final ModelPart snout;
	private final ModelPart horns;
	private final float JAW_RANGE = ModelUtils.toRadians(6D);
	private final ModelPart jaw;
	private final ModelPart belly;
	ModelPart tail;
	List<ModelPart> tailSegments = Lists.newArrayList();
	
	public ModelKobold(ModelPart partsIn)
	{
		super(partsIn);
		
		this.snout = this.head.getChild("snout");
		this.jaw = this.snout.getChild("jaw");
		this.horns = this.head.getChild("horns");
		this.belly = this.body.getChild("belly");
		
		// Tail
		tail = this.body.getChild("tail_root");
		tailSegments.add(tail);
		
		ModelPart prev = tail;
		for(int i=0; i<2; i++)
			tailSegments.add(prev = prev.getChild("child"));
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0F);
		PartDefinition part = mesh.getRoot();
		
		part.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(30, 0).addBox(-4, -6, -5, 8, 6, 7, deformation.extend(0.5F)), PartPose.ZERO);
		
		PartDefinition head = part.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -6, -5, 8, 6, 7), PartPose.ZERO);
		PartDefinition snout = head.addOrReplaceChild("snout", CubeListBuilder.create()
			.texOffs(0, 13).addBox(-3F, -3.5F, -10, 6, 2, 5)
			.texOffs(39, 13).addBox(-2F, -4.5F, -7.5F, 4, 1, 3), PartPose.rotation(ModelUtils.toRadians(3D), 0F, 0F));
			snout.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(22, 13).addBox(-1.5F, -1.5F, -9, 3, 1, 6), PartPose.ZERO);
			int hornX = 45, hornY = 13;
		PartDefinition horns = head.addOrReplaceChild("horns", CubeListBuilder.create(), PartPose.ZERO);
			horns.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(hornX, hornY).addBox(-4.2F, -6.5F, -2F, 2, 2, 8), PartPose.rotation(ModelUtils.degree10, -ModelUtils.degree5, 0F));
			horns.addOrReplaceChild("left_horn", CubeListBuilder.create().mirror().texOffs(hornX, hornY).addBox(2.2F, -6.5F, -2F, 2, 2, 8), PartPose.rotation(ModelUtils.degree10, ModelUtils.degree5, 0F));
		
		PartDefinition body = part.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 23).addBox(-4F, 0F, -2F, 8, 5, 4), PartPose.ZERO);
			body.addOrReplaceChild("belly", CubeListBuilder.create().texOffs(24, 23).addBox(-3.5F, 4.8F, -1.5F, 7, 7, 3), PartPose.ZERO);
			PartDefinition tailRoot = body.addOrReplaceChild("tail_root", CubeListBuilder.create().texOffs(0, 33).addBox(-1F, -1F, 2F, 2, 2, 8), PartPose.offset(0F, 9F, -1F));
			PartDefinition prev = tailRoot;
			for(int i=0; i<2; i++)
				prev = prev.addOrReplaceChild("child", CubeListBuilder.create().texOffs(20+(20*i), 33).addBox(-1F, -1F, 0F, 2, 2, 8 - (2*i)), PartPose.offset(0F, 0F, 9.5F - i*2));
		
		PartDefinition rightArm = part.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
			rightArm.addOrReplaceChild("upper", CubeListBuilder.create().texOffs(0, 43).addBox(-2.0F, -2.0F, -1.5F, 3, 6, 3, deformation.extend(0.01F)), PartPose.rotation(ModelUtils.degree10, 0F, 0F));
			rightArm.addOrReplaceChild("lower", CubeListBuilder.create().texOffs(12, 43).addBox(-2.0F, 2.5F, 1.0F, 3, 6, 3, deformation.extend(0.2F)), PartPose.rotation(ModelUtils.toRadians(-30D), 0F, 0F));
		
        PartDefinition leftArm = part.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror(), PartPose.offset(5.0F, 2.0F, 0.0F));
	        leftArm.addOrReplaceChild("upper", CubeListBuilder.create().mirror().texOffs(24, 43).addBox(-1.0F, -2.0F, -1.5F, 3, 6, 3, deformation.extend(0.01F)), PartPose.rotation(ModelUtils.degree10, 0F, 0F));
	        leftArm.addOrReplaceChild("lower", CubeListBuilder.create().mirror().texOffs(36, 43).addBox(-1.0F, 2.5F, 1.0F, 3, 6, 3, deformation.extend(0.2F)), PartPose.rotation(ModelUtils.toRadians(-30D), 0F, 0F));

        PartDefinition rightLeg = part.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-2.4F, 12.0F, 0.0F));
	        rightLeg.addOrReplaceChild("thigh", CubeListBuilder.create().texOffs(0, 52).addBox(-2F, -2F, -6.5F, 4, 3, 9), PartPose.rotation(ModelUtils.toRadians(35D), 0F, 0F));
	        rightLeg.addOrReplaceChild("ankle", CubeListBuilder.create().texOffs(26, 52).addBox(-1.5F, 3.5F, -2F, 3, 2, 7), PartPose.rotation(ModelUtils.toRadians(-30D), 0F, 0F));
	        PartDefinition rightFoot = rightLeg.addOrReplaceChild("foot", CubeListBuilder.create().texOffs(46, 52).addBox(-1.5F, 10F, -3F, 3, 2, 5), PartPose.ZERO);
	        	rightFoot.addOrReplaceChild("bridge", CubeListBuilder.create().texOffs(46, 59).addBox(-1F, 3.5F, -9.75F, 2, 2, 6), PartPose.rotation(ModelUtils.toRadians(70D), 0F, 0F));
	    
		PartDefinition leftLeg = part.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror(), PartPose.offset(2.4F, 12F, 0F));
			leftLeg.addOrReplaceChild("thigh", CubeListBuilder.create().mirror().texOffs(0, 67).addBox(-2F, -2F, -6.5F, 4, 3, 9), PartPose.rotation(ModelUtils.toRadians(35D), 0F, 0F));
			leftLeg.addOrReplaceChild("ankle", CubeListBuilder.create().mirror().texOffs(26, 67).addBox(-1.5F, 3.5F, -2F, 3, 2, 7), PartPose.rotation(ModelUtils.toRadians(-30D), 0F, 0F));
			PartDefinition leftFoot = leftLeg.addOrReplaceChild("foot", CubeListBuilder.create().mirror().texOffs(46, 67).addBox(-1.5F, 10F, -3F, 3, 2, 5), PartPose.ZERO);
				leftFoot.addOrReplaceChild("bridge", CubeListBuilder.create().mirror().texOffs(46, 74).addBox(-1F, 3.5F, -9.75F, 2, 2, 6), PartPose.rotation(ModelUtils.toRadians(70D), 0F, 0F));
	    
		return LayerDefinition.create(mesh, 80, 96);
	}
	
	public void setupAnim(EntityKobold entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		
		float partialTicks = 0F;
		jaw.xRot = entityIn.getJawState(partialTicks) * JAW_RANGE;
		
		snout.z = (entityIn.getShortSnout() ? 2F : 0.5F);
		horns.visible = entityIn.getHorns();
		
		float time = ((float)Math.sin(ageInTicks / 20)) * 0.5F;
		for(ModelPart segment : tailSegments)
		{
			segment.yRot = time / 3;
			segment.xRot = (time * Math.signum(time)) / 8;
		}
		
		float dif = this.body.yRot - this.head.yRot;
		this.head.zRot = Math.max(-0.6F, Math.min(0.6F, dif/4));
		this.hat.copyFrom(this.head);
		
		belly.y = (this.crouching ? -1.3F : 0F);
		
//	    if(VOHelper.isCreatureAttribute(entityIn, EnumCreatureAttribute.UNDEAD))
//	    {
//	    	this.leftArm.xRot -= ModelUtils.degree90;
//	    	this.rightArm.xRot -= ModelUtils.degree90;
//	    }
	}
}
