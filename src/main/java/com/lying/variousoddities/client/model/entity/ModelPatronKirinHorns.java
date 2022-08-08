package com.lying.variousoddities.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.wip.EntityPatronKirin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelPatronKirinHorns extends HumanoidModel<EntityPatronKirin>
{
	public ModelPatronKirinHorns()
	{
		super(0F);
		
		this.head = ModelUtils.freshRenderer(this);
		
		// Antlers
		ModelPart leftRoot = ModelUtils.freshRenderer(this);
		leftRoot.mirror = true;
		leftRoot.setTextureOffset(0, 0).addBox(1F, -11F, -4.1F, 1, 4, 1);
		leftRoot.xRot = ModelUtils.toRadians(-7D);
		leftRoot.zRot = ModelUtils.toRadians(7D);
		
		ModelPart leftBranch = ModelUtils.freshRenderer(this);
		leftBranch.mirror = true;
		leftBranch.setTextureOffset(4, 0).addBox(-8F, -9.5F, -5.4F, 1, 5, 1);
		leftBranch.xRot = ModelUtils.toRadians(-26D);
		leftBranch.zRot = ModelUtils.toRadians(67D);
		
		ModelPart leftBranch2 = ModelUtils.freshRenderer(this);
		leftBranch2.mirror = true;
		leftBranch2.setTextureOffset(8, 0).addBox(5F, -15.5F, -5.3F, 1, 7, 1);
		leftBranch2.xRot = ModelUtils.toRadians(-22D);
		leftBranch2.zRot = ModelUtils.toRadians(-5D);
		
		ModelPart leftPoint = ModelUtils.freshRenderer(this);
		leftPoint.mirror = true;
		leftPoint.setTextureOffset(12, 0).addBox(-4F, -16F, -2F, 1, 3, 1);
		leftPoint.xRot = ModelUtils.toRadians(-6D);
		leftPoint.zRot = ModelUtils.toRadians(35D);
		
		this.head.addChild(leftRoot);
		this.head.addChild(leftBranch);
		this.head.addChild(leftBranch2);
		this.head.addChild(leftPoint);
		
		ModelPart rightRoot = ModelUtils.freshRenderer(this);
		rightRoot.setTextureOffset(0, 0).addBox(-2F, -11F, -4.1F, 1, 4, 1);
		rightRoot.xRot = ModelUtils.toRadians(-7D);
		rightRoot.zRot = ModelUtils.toRadians(-7D);
		
		ModelPart rightBranch = ModelUtils.freshRenderer(this);
		rightBranch.setTextureOffset(4, 0).addBox(7F, -9.5F, -5.4F, 1, 5, 1);
		rightBranch.xRot = ModelUtils.toRadians(-26D);
		rightBranch.zRot = ModelUtils.toRadians(-67D);
		
		ModelPart rightBranch2 = ModelUtils.freshRenderer(this);
		rightBranch2.setTextureOffset(8, 0).addBox(-6F, -15.5F, -5.3F, 1, 7, 1);
		rightBranch2.xRot = ModelUtils.toRadians(-22D);
		rightBranch2.zRot = ModelUtils.toRadians(5D);
		
		ModelPart rightPoint = ModelUtils.freshRenderer(this);
		rightPoint.setTextureOffset(12, 0).addBox(3F, -16F, -2F, 1, 3, 1);
		rightPoint.xRot = ModelUtils.toRadians(-6D);
		rightPoint.zRot = ModelUtils.toRadians(-35D);
		
		this.head.addChild(rightRoot);
		this.head.addChild(rightBranch);
		this.head.addChild(rightBranch2);
		this.head.addChild(rightPoint);
		
		// Ram horns
		
		float size = 0.5F;
		ModelPart rightRam1 = ModelUtils.freshRenderer(this);
		rightRam1.setTextureOffset(0, 8).addBox(-5.5F, -7.5F, -2F, 1, 2, 3, size);
		rightRam1.setTextureOffset(0, 18).addBox(-5.9F, -5F, 1F, 1, 2, 2, size * 0.5F);
		ModelPart rightRam2 = ModelUtils.freshRenderer(this);
		rightRam2.setTextureOffset(0, 13).addBox(-5.7F, -6F, -4.5F, 1, 2, 3, size * 0.75F);
		rightRam2.xRot = ModelUtils.toRadians(-45D);
		ModelPart rightRam3 = ModelUtils.freshRenderer(this);
		rightRam3.setTextureOffset(0, 22).addBox(-6.1F, -1.5F, 1.5F, 1, 1, 2, size * 0.25F);
		rightRam3.xRot = ModelUtils.toRadians(45D);
		ModelPart rightRam4 = ModelUtils.freshRenderer(this);
		rightRam4.setTextureOffset(0, 25).addBox(-6.3F, -2F, -1.5F, 1, 1, 2, 0F);
		rightRam4.xRot = ModelUtils.toRadians(-7D);
		
		ModelPart rightRam = ModelUtils.freshRenderer(this);
		rightRam.setRotationPoint(0F, 1F, 1F);
		rightRam.xRot = ModelUtils.degree5;
			rightRam.addChild(rightRam1);
			rightRam.addChild(rightRam2);
			rightRam.addChild(rightRam3);
			rightRam.addChild(rightRam4);
		
		ModelPart leftRam1 = ModelUtils.freshRenderer(this);
		leftRam1.mirror = true;
		leftRam1.setTextureOffset(0, 8).addBox(4.5F, -7.5F, -2F, 1, 2, 3, size);
		leftRam1.setTextureOffset(0, 18).addBox(4.7F, -5F, 1F, 1, 2, 2, size * 0.5F);
		ModelPart leftRam2 = ModelUtils.freshRenderer(this);
		leftRam2.mirror = true;
		leftRam2.setTextureOffset(0, 13).addBox(4.7F, -6F, -4.5F, 1, 2, 3, size * 0.75F);
		leftRam2.xRot = ModelUtils.toRadians(-45D);
		ModelPart leftRam3 = ModelUtils.freshRenderer(this);
		leftRam3.mirror = true;
		leftRam3.setTextureOffset(0, 22).addBox(4.9F, -1.5F, 1.5F, 1, 1, 2, size * 0.25F);
		leftRam3.xRot = ModelUtils.toRadians(45D);
		ModelPart leftRam4 = ModelUtils.freshRenderer(this);
		leftRam4.mirror = true;
		leftRam4.setTextureOffset(0, 25).addBox(5.1F, -2F, -1.5F, 1, 1, 2, 0F);
		leftRam4.xRot = ModelUtils.toRadians(-7D);
		
		ModelPart leftRam = ModelUtils.freshRenderer(this);
		leftRam.setRotationPoint(0F, 1F, 1F);
		leftRam.xRot = ModelUtils.degree5;
			leftRam.addChild(leftRam1);
			leftRam.addChild(leftRam2);
			leftRam.addChild(leftRam3);
			leftRam.addChild(leftRam4);
		
		this.head.addChild(rightRam);
		this.head.addChild(leftRam);
	}
	
	protected Iterable<ModelPart> getHeadParts()
	{
	   return ImmutableList.of(this.head);
	}
	
	protected Iterable<ModelPart> getBodyParts()
	{
	   return ImmutableList.of();
	}
}
