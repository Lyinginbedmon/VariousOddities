package com.lying.variousoddities.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.wip.EntityPatronKirin;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelPatronKirinHorns extends BipedModel<EntityPatronKirin>
{
	public ModelPatronKirinHorns()
	{
		super(0F);
		
		this.bipedHead = ModelUtils.freshRenderer(this);
		
		// Antlers
		ModelRenderer leftRoot = ModelUtils.freshRenderer(this);
		leftRoot.mirror = true;
		leftRoot.setTextureOffset(0, 0).addBox(1F, -11F, -4.1F, 1, 4, 1);
		leftRoot.rotateAngleX = ModelUtils.toRadians(-7D);
		leftRoot.rotateAngleZ = ModelUtils.toRadians(7D);
		
		ModelRenderer leftBranch = ModelUtils.freshRenderer(this);
		leftBranch.mirror = true;
		leftBranch.setTextureOffset(4, 0).addBox(-8F, -9.5F, -5.4F, 1, 5, 1);
		leftBranch.rotateAngleX = ModelUtils.toRadians(-26D);
		leftBranch.rotateAngleZ = ModelUtils.toRadians(67D);
		
		ModelRenderer leftBranch2 = ModelUtils.freshRenderer(this);
		leftBranch2.mirror = true;
		leftBranch2.setTextureOffset(8, 0).addBox(5F, -15.5F, -5.3F, 1, 7, 1);
		leftBranch2.rotateAngleX = ModelUtils.toRadians(-22D);
		leftBranch2.rotateAngleZ = ModelUtils.toRadians(-5D);
		
		ModelRenderer leftPoint = ModelUtils.freshRenderer(this);
		leftPoint.mirror = true;
		leftPoint.setTextureOffset(12, 0).addBox(-4F, -16F, -2F, 1, 3, 1);
		leftPoint.rotateAngleX = ModelUtils.toRadians(-6D);
		leftPoint.rotateAngleZ = ModelUtils.toRadians(35D);
		
		this.bipedHead.addChild(leftRoot);
		this.bipedHead.addChild(leftBranch);
		this.bipedHead.addChild(leftBranch2);
		this.bipedHead.addChild(leftPoint);
		
		ModelRenderer rightRoot = ModelUtils.freshRenderer(this);
		rightRoot.setTextureOffset(0, 0).addBox(-2F, -11F, -4.1F, 1, 4, 1);
		rightRoot.rotateAngleX = ModelUtils.toRadians(-7D);
		rightRoot.rotateAngleZ = ModelUtils.toRadians(-7D);
		
		ModelRenderer rightBranch = ModelUtils.freshRenderer(this);
		rightBranch.setTextureOffset(4, 0).addBox(7F, -9.5F, -5.4F, 1, 5, 1);
		rightBranch.rotateAngleX = ModelUtils.toRadians(-26D);
		rightBranch.rotateAngleZ = ModelUtils.toRadians(-67D);
		
		ModelRenderer rightBranch2 = ModelUtils.freshRenderer(this);
		rightBranch2.setTextureOffset(8, 0).addBox(-6F, -15.5F, -5.3F, 1, 7, 1);
		rightBranch2.rotateAngleX = ModelUtils.toRadians(-22D);
		rightBranch2.rotateAngleZ = ModelUtils.toRadians(5D);
		
		ModelRenderer rightPoint = ModelUtils.freshRenderer(this);
		rightPoint.setTextureOffset(12, 0).addBox(3F, -16F, -2F, 1, 3, 1);
		rightPoint.rotateAngleX = ModelUtils.toRadians(-6D);
		rightPoint.rotateAngleZ = ModelUtils.toRadians(-35D);
		
		this.bipedHead.addChild(rightRoot);
		this.bipedHead.addChild(rightBranch);
		this.bipedHead.addChild(rightBranch2);
		this.bipedHead.addChild(rightPoint);
		
		// Ram horns
		
		float size = 0.5F;
		ModelRenderer rightRam1 = ModelUtils.freshRenderer(this);
		rightRam1.setTextureOffset(0, 8).addBox(-5.5F, -7.5F, -2F, 1, 2, 3, size);
		rightRam1.setTextureOffset(0, 18).addBox(-5.9F, -5F, 1F, 1, 2, 2, size * 0.5F);
		ModelRenderer rightRam2 = ModelUtils.freshRenderer(this);
		rightRam2.setTextureOffset(0, 13).addBox(-5.7F, -6F, -4.5F, 1, 2, 3, size * 0.75F);
		rightRam2.rotateAngleX = ModelUtils.toRadians(-45D);
		ModelRenderer rightRam3 = ModelUtils.freshRenderer(this);
		rightRam3.setTextureOffset(0, 22).addBox(-6.1F, -1.5F, 1.5F, 1, 1, 2, size * 0.25F);
		rightRam3.rotateAngleX = ModelUtils.toRadians(45D);
		ModelRenderer rightRam4 = ModelUtils.freshRenderer(this);
		rightRam4.setTextureOffset(0, 25).addBox(-6.3F, -2F, -1.5F, 1, 1, 2, 0F);
		rightRam4.rotateAngleX = ModelUtils.toRadians(-7D);
		
		ModelRenderer rightRam = ModelUtils.freshRenderer(this);
		rightRam.setRotationPoint(0F, 1F, 1F);
		rightRam.rotateAngleX = ModelUtils.degree5;
			rightRam.addChild(rightRam1);
			rightRam.addChild(rightRam2);
			rightRam.addChild(rightRam3);
			rightRam.addChild(rightRam4);
		
		ModelRenderer leftRam1 = ModelUtils.freshRenderer(this);
		leftRam1.mirror = true;
		leftRam1.setTextureOffset(0, 8).addBox(4.5F, -7.5F, -2F, 1, 2, 3, size);
		leftRam1.setTextureOffset(0, 18).addBox(4.7F, -5F, 1F, 1, 2, 2, size * 0.5F);
		ModelRenderer leftRam2 = ModelUtils.freshRenderer(this);
		leftRam2.mirror = true;
		leftRam2.setTextureOffset(0, 13).addBox(4.7F, -6F, -4.5F, 1, 2, 3, size * 0.75F);
		leftRam2.rotateAngleX = ModelUtils.toRadians(-45D);
		ModelRenderer leftRam3 = ModelUtils.freshRenderer(this);
		leftRam3.mirror = true;
		leftRam3.setTextureOffset(0, 22).addBox(4.9F, -1.5F, 1.5F, 1, 1, 2, size * 0.25F);
		leftRam3.rotateAngleX = ModelUtils.toRadians(45D);
		ModelRenderer leftRam4 = ModelUtils.freshRenderer(this);
		leftRam4.mirror = true;
		leftRam4.setTextureOffset(0, 25).addBox(5.1F, -2F, -1.5F, 1, 1, 2, 0F);
		leftRam4.rotateAngleX = ModelUtils.toRadians(-7D);
		
		ModelRenderer leftRam = ModelUtils.freshRenderer(this);
		leftRam.setRotationPoint(0F, 1F, 1F);
		leftRam.rotateAngleX = ModelUtils.degree5;
			leftRam.addChild(leftRam1);
			leftRam.addChild(leftRam2);
			leftRam.addChild(leftRam3);
			leftRam.addChild(leftRam4);
		
		this.bipedHead.addChild(rightRam);
		this.bipedHead.addChild(leftRam);
	}
	
	protected Iterable<ModelRenderer> getHeadParts()
	{
	   return ImmutableList.of(this.bipedHead);
	}
	
	protected Iterable<ModelRenderer> getBodyParts()
	{
	   return ImmutableList.of();
	}
}
