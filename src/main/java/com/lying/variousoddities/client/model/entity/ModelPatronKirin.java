package com.lying.variousoddities.client.model.entity;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.wip.EntityPatronKirin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelPatronKirin extends HumanoidModel<EntityPatronKirin>
{
	private ModelPart coatTail;
	
	private ModelPart tail;
	private List<ModelPart> tailSegments = Lists.newArrayList();
	
	public ModelPatronKirin(ModelPart partsIn)
	{
		super(partsIn);
		
		this.coatTail = this.body.getChild("coat_tail");
		
		this.tail = this.body.getChild("tail");
		this.tailSegments.add(tail);
		this.tailSegments.add(this.tail.getChild("child"));
		this.tailSegments.add(this.tailSegments.get(1).getChild("child"));
		this.tailSegments.add(this.tailSegments.get(2).getChild("child"));
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0F);
		PartDefinition part = mesh.getRoot();
		
		part.getChild("head").addOrReplaceChild("child", CubeListBuilder.create().texOffs(32, 0).addBox(-4F, -8F, -4F, 8, 8, 8, deformation.extend(0.25F)), PartPose.ZERO);
		part.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 86).addBox(-4F, -8F, -4F, 8, 8, 8, deformation.extend(0.5F)), PartPose.ZERO);
		PartDefinition body = part.getChild("body").addOrReplaceChild("child", CubeListBuilder.create().texOffs(16, 45).addBox(-4F, 0F, -2F, 8, 4, 4, deformation.extend(0.5F)), PartPose.ZERO);
			body.addOrReplaceChild("coat_tail", CubeListBuilder.create().texOffs(16, 32).addBox(-4F, 0F, -2F, 8, 9, 4, deformation.extend(0.5F)), PartPose.offsetAndRotation(0F, 9.5F, 0.5F, ModelUtils.toRadians(25.5D), 0F, 0F));
		part.getChild("right_arm").addOrReplaceChild("child", CubeListBuilder.create().texOffs(40, 32).addBox(-3F, -2F, -2F, 4, 4, 4, deformation.extend(0.5F)), PartPose.ZERO);
		part.getChild("left_arm").addOrReplaceChild("child", CubeListBuilder.create().texOffs(0, 32).addBox(-1F, -2F, -2F, 4, 4, 4, deformation.extend(0.5F)), PartPose.ZERO);
		
		PartDefinition rightLeg = part.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-2.4F, 12F, 0F));
			rightLeg.addOrReplaceChild("thigh", CubeListBuilder.create().texOffs(0, 53).addBox(-2F, -2F, -6.5F, 4, 4, 9), PartPose.rotation(ModelUtils.toRadians(35D), 0F, 0F));
			rightLeg.addOrReplaceChild("ankle", CubeListBuilder.create().texOffs(0, 66).addBox(-1F, 3.5F, -2F, 2, 2, 7), PartPose.rotation(ModelUtils.toRadians(-30D), 0F, 0F));
			rightLeg.addOrReplaceChild("foot", CubeListBuilder.create()
				.texOffs(0, 75).addBox(-1.5F, 2F, -11.8F, 3, 3, 8)
				.texOffs(22, 76).addBox(-2.5F, 1.0F, -13F, 5, 5, 5, deformation.extend(-0.5F)), PartPose.rotation(ModelUtils.toRadians(70D), 0F, 0F));
		
		PartDefinition leftLeg = part.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror(), PartPose.offset(2.4F, 12F, 0F));
			leftLeg.addOrReplaceChild("thigh", CubeListBuilder.create().mirror().texOffs(0, 53).addBox(-2F, -2F, -6.5F, 4, 4, 9), PartPose.rotation(ModelUtils.toRadians(35D), 0F, 0F));
			leftLeg.addOrReplaceChild("ankle", CubeListBuilder.create().mirror().texOffs(0, 66).addBox(-1F, 3.5F, -2F, 2, 2, 7), PartPose.rotation(ModelUtils.toRadians(-30D), 0F, 0F));
			leftLeg.addOrReplaceChild("foot", CubeListBuilder.create().mirror()
				.texOffs(0, 75).addBox(-1.5F, 2F, -11.8F, 3, 3, 8)
				.texOffs(22, 76).addBox(-2.5F, 1.0F, -13F, 5, 5, 5, deformation.extend(-0.5F)), PartPose.rotation(ModelUtils.toRadians(70D), 0F, 0F));
		
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(26, 53).addBox(-1F, -1F, -1F, 2, 6, 2, deformation.extend(-0.2F)), PartPose.offset(0F, 12F, 2F));
			PartDefinition tail1 = tail.addOrReplaceChild("child", CubeListBuilder.create()
				.texOffs(26, 53).addBox(-1F, 0F, -1F, 2, 6, 2, deformation.extend(-0.2F))
				.texOffs(34, 53).addBox(0F, 1F, 0F, 0, 5, 2), PartPose.offsetAndRotation(0F, 3.5F, 0F, ModelUtils.degree10, 0F, 0F));
			PartDefinition tail2 = tail1.addOrReplaceChild("child", CubeListBuilder.create().mirror().texOffs(26, 53).addBox(-1F, 0F, -1F, 2, 6, 2, deformation.extend(-0.2F)), PartPose.offsetAndRotation(0F, 4.5F, 0F, ModelUtils.degree10 * 2, 0F, 0F));
			tail2.addOrReplaceChild("child", CubeListBuilder.create()
				.texOffs(26, 53).addBox(-1F, 0F, -1F, 2, 6, 2, deformation.extend(-0.2F))
				.texOffs(26, 64).addBox(-1.5F, 1.5F, -1.5F, 3, 7, 3, deformation.extend(-0.25F))
				.texOffs(34, 60).addBox(0F, 1F, 0F, 0, 2, 2), PartPose.offsetAndRotation(0F, 5.5F, 0F, ModelUtils.degree10 * 3, 0F, 0F));
		
		return LayerDefinition.create(mesh, 64, 128);
	}
    
    public void setupAnim(EntityPatronKirin entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
    	super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        
    	this.coatTail.xRot = ModelUtils.toRadians(25.5D) + Math.max(this.leftLeg.xRot, this.rightLeg.xRot);
    	
    	float time = ((float)Math.sin(ageInTicks / 10)) * 0.5F;
    	int i = 0;
    	for(ModelPart segment : tailSegments)
    	{
    		segment.yRot = time / 1.5F;
    		segment.xRot = (time * Math.signum(time)) / 8 + (ModelUtils.degree10 * i++);
    	}
    	this.tail.xRot += ModelUtils.toRadians(45D);
    }
}
