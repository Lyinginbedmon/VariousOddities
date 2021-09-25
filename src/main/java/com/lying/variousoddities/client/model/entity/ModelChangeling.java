package com.lying.variousoddities.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.passive.IChangeling;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class ModelChangeling<T extends LivingEntity> extends BipedModel<T>
{
	ModelRenderer wingRight, wingLeft;
	
	public ModelChangeling()
	{
		super(0F);
		this.textureHeight = 64;
		this.textureWidth = 64;
		
        this.bipedHead = new ModelRenderer(this, 0, 0);
        this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        	ModelRenderer head0 = ModelUtils.freshRenderer(this);
        	head0.setTextureOffset(19, 0).addBox(-1.5F, -3F, -1.5F, 3, 4, 3);
        	head0.rotateAngleX = ModelUtils.toRadians(12D);
        	this.bipedHead.addChild(head0);
        	ModelRenderer head1 = ModelUtils.freshRenderer(this);
        	head1.rotateAngleX = ModelUtils.toRadians(17D);
        	head1.setTextureOffset(0, 0).addBox(-2.5F, -7F, -6.5F, 5, 5, 9);
        	head1.setTextureOffset(28, 0).addBox(-2.5F, -7F, -6.5F, 5, 5, 9, 0.5F);
        	head1.setTextureOffset(31, 0).addBox(-0.5F, -11F, -3F, 1, 4, 1);
        	head1.setTextureOffset(47, -5).addBox(0F, -8.5F, 0F, 0, 8, 5);
        	this.bipedHead.addChild(head1);
		
		this.bipedBody = ModelUtils.freshRenderer(this);
		this.bipedBody.setTextureOffset(0, 14).addBox(-4F, 0F, -2F, 8, 6, 4);
		this.bipedBody.setTextureOffset(0, 24).addBox(-4F, 0F, -2F, 8, 6, 4, 0.5F);
		this.bipedBody.setTextureOffset(0, 34).addBox(-3F, 6F, -1.25F, 6, 6, 3);
		this.bipedBody.setTextureOffset(0, 43).addBox(-2F, 6F, -1.5F, 4, 5, 1);
        
		wingRight = ModelUtils.freshRenderer(this);
		wingRight.setRotationPoint(-2.5F, 0F, 2F);
		wingRight.setTextureOffset(10, 43).addBox(-2.5F, 0F, 0F, 5, 15, 1);
		wingRight.rotateAngleX = ModelUtils.toRadians(6D);
		wingRight.rotateAngleZ = ModelUtils.toRadians(2D);
			this.bipedBody.addChild(wingRight);
		
		wingLeft = ModelUtils.freshRenderer(this);
		wingLeft.setRotationPoint(2.5F, 0F, 2F);
		wingLeft.mirror = true;
		wingLeft.setTextureOffset(10, 43).addBox(-2.5F, 0F, 0F, 5, 15, 1);
		wingLeft.rotateAngleX = ModelUtils.toRadians(6D);
		wingLeft.rotateAngleZ = ModelUtils.toRadians(-2D);
			this.bipedBody.addChild(wingLeft);
		
        this.bipedRightArm = ModelUtils.freshRenderer(this);
        this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.bipedRightArm.setTextureOffset(50, 24).addBox(-2.5F, 5F, -1F, 3, 6, 3);
        	ModelRenderer rightArm = ModelUtils.freshRenderer(this);
        	rightArm.setTextureOffset(50, 14).addBox(-1.0F, -2.0F, -1.0F, 2, 8, 2);
        	rightArm.rotateAngleX = ModelUtils.toRadians(6D);
        	rightArm.rotateAngleZ = ModelUtils.toRadians(8D);
        	this.bipedRightArm.addChild(rightArm);
        
        this.bipedLeftArm = ModelUtils.freshRenderer(this);
        this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.setTextureOffset(50, 24).addBox(-0.5F, 5F, -1F, 3, 6, 3);
	    	ModelRenderer leftArm = ModelUtils.freshRenderer(this);
	    	leftArm.mirror = true;
	    	leftArm.setTextureOffset(50, 14).addBox(-1.0F, -2.0F, -1.0F, 2, 8, 2);
	    	leftArm.rotateAngleX = ModelUtils.toRadians(6D);
	    	leftArm.rotateAngleZ = ModelUtils.toRadians(-8D);
	    	this.bipedLeftArm.addChild(leftArm);
	    	
	        // Thigh
	        ModelRenderer thigh = ModelUtils.freshRenderer(this);
	        thigh.setTextureOffset(24, 14).addBox(-2F, -2F, -6.5F, 4, 3, 9);
	        thigh.rotateAngleX = (float)(Math.toRadians(35D));
			
	        // Ankle
	        ModelRenderer ankle = ModelUtils.freshRenderer(this);
	        ankle.setTextureOffset(24, 26).addBox(-1.5F, 3.5F, -2F, 3, 2, 7);
	        ankle.rotateAngleX = (float)(Math.toRadians(-30D));
	        
	        // Foot
	        ModelRenderer foot = ModelUtils.freshRenderer(this);
	        foot.setTextureOffset(24, 35).addBox(-1F, 3.5F, -11.75F, 2, 2, 8);
	        foot.setTextureOffset(44, 35).addBox(-1F, 3.5F, -11.75F, 2, 2, 8, 0.5F);
	        foot.rotateAngleX = (float)(Math.toRadians(70D));
		this.bipedRightLeg = ModelUtils.freshRenderer(this);
	    this.bipedRightLeg.setRotationPoint(-2.4F, 12.0F, 0.0F);
	    this.bipedRightLeg.addChild(thigh);
	    this.bipedRightLeg.addChild(ankle);
	    this.bipedRightLeg.addChild(foot);
	
	        // Thigh
	        thigh = ModelUtils.freshRenderer(this);
	        thigh.mirror=true;
	        thigh.setTextureOffset(24, 14).addBox(-2F, -2F, -6.5F, 4, 3, 9);
	        thigh.rotateAngleX = ModelUtils.toRadians(35D);
			
	        // Ankle
	        ankle = ModelUtils.freshRenderer(this);
	        ankle.mirror=true;
	        ankle.setTextureOffset(24, 26).addBox(-1.5F, 3.5F, -2F, 3, 2, 7);
	        ankle.rotateAngleX = ModelUtils.toRadians(-30D);
	        
	        // Foot
	        foot = ModelUtils.freshRenderer(this);
	        foot.mirror=true;
	        foot.setTextureOffset(24, 35).addBox(-1F, 3.5F, -11.75F, 2, 2, 8);
	        foot.setTextureOffset(44, 35).addBox(-1F, 3.5F, -11.75F, 2, 2, 8, 0.5F);
	        foot.rotateAngleX = ModelUtils.toRadians(70D);
		this.bipedLeftLeg = ModelUtils.freshRenderer(this);
		this.bipedLeftLeg.setRotationPoint(2.4F, 12F, 0F);
		this.bipedLeftLeg.addChild(thigh);
		this.bipedLeftLeg.addChild(ankle);
		this.bipedLeftLeg.addChild(foot);
	}
	
	protected Iterable<ModelRenderer> getHeadParts()
	{
	   return ImmutableList.of(this.bipedHead);
	}
	
	protected Iterable<ModelRenderer> getBodyParts()
	{
		return ImmutableList.of(this.bipedBody, this.bipedRightArm, this.bipedLeftArm, this.bipedRightLeg, this.bipedLeftLeg);
	}
	
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
    	super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	
    	this.bipedBody.rotateAngleX += ModelUtils.toRadians(8D);
    	
    	this.wingRight.rotateAngleZ = ModelUtils.toRadians(2D);
    	if(entityIn instanceof IChangeling)
    	{
    		IChangeling changeling = (IChangeling)entityIn;
    		if(changeling.isFlapping())
		    	this.wingRight.rotateAngleZ = ModelUtils.toRadians(2D) + (ModelUtils.toRadians(5D) * (float)(Math.sin(changeling.getFlappingTime() * 2) + 1F));
    	}
    	this.wingLeft.rotateAngleZ = -this.wingRight.rotateAngleZ;
    	
    	this.bipedLeftLeg.rotationPointZ += 2F;
    	this.bipedRightLeg.rotationPointZ += 2F;
    }
}
