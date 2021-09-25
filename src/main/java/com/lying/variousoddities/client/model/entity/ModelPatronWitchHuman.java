package com.lying.variousoddities.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.wip.EntityPatronWitch;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelPatronWitchHuman extends BipedModel<EntityPatronWitch> implements IPonytailModel
{
	public ModelRenderer leftSleeve, rightSleeve;
	public ModelRenderer tabardFront, tabardRear;
	
	public ModelRenderer ponytail;
	public ModelRenderer ponytailAnchor;
	public ModelRenderer ponytailAnchor2;
	
	public ModelRenderer smileHead;
	public ModelRenderer jawHead;
	public ModelRenderer jawBase;
	public ModelRenderer jawLeft, jawRight;
	
	public ModelPatronWitchHuman()
	{
		super(0F, 0F, 64, 64);
        float layerOffset = 2.2F;
		
		this.ponytailAnchor = ModelUtils.freshRenderer(this).addBox(0F, 0F, 0F, 1, 1, 1);
			this.ponytailAnchor2 = ModelUtils.freshRenderer(this).addBox(0F, 0F, 0F, 1, 1, 1);
			this.ponytailAnchor2.setRotationPoint(0, -5, 4);
		
        this.ponytail = ModelUtils.freshRenderer(this).setTextureSize(64, 32);
        this.ponytail.addBox(-5F, 0F, 0F, 10, 16, 1);
        	this.ponytailAnchor2.addChild(ponytail);
				this.ponytailAnchor.addChild(ponytailAnchor2);
        
        this.bipedHead = ModelUtils.freshRenderer(this);
        this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8);
        this.bipedHead.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        addEars(this.bipedHead);
		
        	// Smile
        this.smileHead = ModelUtils.freshRenderer(this);
        this.smileHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.smileHead.setTextureOffset(16, 48).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8);
        this.smileHead.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        addEars(this.smileHead);
        
			// Jaw
		this.jawHead = ModelUtils.freshRenderer(this);
        this.jawHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.jawHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 6, 8);
        this.jawHead.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        this.jawHead.setTextureOffset(36, 32).addBox(-4F, -2F, 2F, 8, 2, 2);
    	addEars(this.jawHead);
			this.jawBase = ModelUtils.freshRenderer(this);
			this.jawBase.setRotationPoint(0F, -2F, 4F);
			this.jawLeft = ModelUtils.freshRenderer(this);
			this.jawLeft.setRotationPoint(0F, 0F, 0F);
			this.jawLeft.mirror = true;
			this.jawLeft.setTextureOffset(16, 32).addBox(0F, 0F, -8F, 4, 2, 6);
				this.jawBase.addChild(this.jawLeft);
			this.jawRight = ModelUtils.freshRenderer(this);
			this.jawRight.setRotationPoint(0F, 0F, 0F);
			this.jawRight.setTextureOffset(16, 32).addBox(-4F, 0F, -8F, 4, 2, 6);
				this.jawBase.addChild(this.jawRight);
				this.jawHead.addChild(jawBase);
        
//        this.bipedHeadwear = ModelUtils.freshRenderer(this);
//        this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
//        this.bipedHeadwear.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        
        this.bipedBody = ModelUtils.freshRenderer(this);
        this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4);
        	// Buckles
        this.bipedBody.setTextureOffset(16, 40).addBox(-4F, 0F, -layerOffset, 8, 8, 0);
        
        this.bipedRightArm = ModelUtils.freshRenderer(this);
        this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4);
			rightSleeve = ModelUtils.freshRenderer(this);
			rightSleeve.setRotationPoint(0F, 8F, 0F);
			rightSleeve.setTextureOffset(0, 32).addBox(-3F, -4F, -2F, 4, 5, 4, 0.2F);
				this.bipedRightArm.addChild(rightSleeve);
		
        this.bipedLeftArm = ModelUtils.freshRenderer(this);
        this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.setTextureOffset(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4);
			leftSleeve = ModelUtils.freshRenderer(this);
			leftSleeve.setRotationPoint(0F, 8F, 0F);
			leftSleeve.mirror = true;
			leftSleeve.setTextureOffset(0, 32).addBox(-1F, -4F, -2F, 4, 5, 4, 0.2F);
				this.bipedLeftArm.addChild(leftSleeve);
		
        this.bipedRightLeg = ModelUtils.freshRenderer(this);
        this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
        
        this.bipedLeftLeg = ModelUtils.freshRenderer(this);
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.setTextureOffset(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
        
        this.tabardFront = ModelUtils.freshRenderer(this);
        this.tabardFront.setRotationPoint(0F, 12F, 0F);
        this.tabardFront.setTextureOffset(36, 36).addBox(-4F, 0F, -layerOffset, 8, 6, 0);
        
        this.tabardRear = ModelUtils.freshRenderer(this);
        this.tabardRear.setRotationPoint(0F, 12F, 0F);
        this.tabardRear.setTextureOffset(36, 42).addBox(-4F, 0F, layerOffset, 8, 6, 0);
	}
	
	protected Iterable<ModelRenderer> getHeadParts()
	{
	   return ImmutableList.of(this.bipedHead, this.smileHead, this.jawHead);
	}
	
	protected Iterable<ModelRenderer> getBodyParts()
	{
		return ImmutableList.of(this.bipedBody, this.bipedRightArm, this.bipedLeftArm, this.bipedRightLeg, this.bipedLeftLeg, this.tabardFront, this.tabardRear);
	}
    
    public void setPonytailHeight(float par1Float)
    {
    	this.ponytailAnchor2.rotationPointY = par1Float;
    }
    
    public void setPonytailRotation(float par1Float, float par2Float, boolean par3Bool)
    {
    	this.ponytail.rotationPointY = Math.min(5.6F, par2Float / 15F) + (par3Bool ? 3.5F : 0F);
    }
    
    public void renderPonytail(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn)
    {
    	this.ponytailAnchor.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
    }
    
    private void addEars(ModelRenderer head)
    {
		head.setTextureOffset(24, 0).addBox(-4.5F, -6F, 0F, 1, 3, 2);
		head.setTextureOffset(30, 0).addBox(-5F, -8F, 2F, 1, 3, 1);
		head.setTextureOffset(24, 5).addBox(-5F, -7F, 1F, 1, 1, 1);
		head.setTextureOffset(24, 0).addBox(3.5F, -6F, 0F, 1, 3, 2);
		head.setTextureOffset(30, 0).addBox(4F, -8F, 2F, 1, 3, 1);
		head.setTextureOffset(24, 5).addBox(4F, -7F, 1F, 1, 1, 1);
    }
    
    public ModelRenderer getHeadForWitch(EntityPatronWitch witch)
    {
    	if(witch.isJawOpen())
    		return witch.isJawSplit() ? this.jawHead : this.smileHead;
    	return this.bipedHead;
    }
    
    public void setRotationAngles(EntityPatronWitch entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
    	super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	
    	ponytailAnchor.copyModelAngles(bipedHead);
    	ponytail.rotateAngleX = Math.max(bipedBody.rotateAngleX - bipedHead.rotateAngleX, -0.259F);
    	
    	jawHead.copyModelAngles(bipedHead);
    	smileHead.copyModelAngles(bipedHead);
		
		this.tabardFront.rotateAngleX = Math.min(Math.min(this.bipedLeftLeg.rotateAngleX, this.bipedRightLeg.rotateAngleX), ModelUtils.toRadians(-2D));
		this.tabardRear.rotateAngleX = Math.max(Math.max(this.bipedLeftLeg.rotateAngleX, this.bipedRightLeg.rotateAngleX), ModelUtils.toRadians(2D));
		
		if(entityIn.isJawOpen())
		{
    		if(entityIn.isJawSplit())
    		{
    			setVisibleHead(this.jawHead);
        		float rot = entityIn.getJawState(0F);
    			
		    	this.jawBase.rotateAngleX = rot * ModelUtils.toRadians(10D);
		    	
		    	float jawAng = ModelUtils.toRadians(15D);
		    	this.jawLeft.rotateAngleY = -rot * jawAng;
		    	this.jawRight.rotateAngleY = rot * jawAng;
    		}
    		else
    			setVisibleHead(this.smileHead);
    	}
		else
			setVisibleHead(this.bipedHead);
    }
    
    public void setVisibleHead(ModelRenderer head)
    {
		this.bipedHead.showModel = false;
		this.jawHead.showModel = false;
		this.smileHead.showModel = false;
		head.showModel = true;
    }
}
