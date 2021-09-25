package com.lying.variousoddities.client.model.entity;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.passive.IChangeling;
import com.lying.variousoddities.entity.wip.EntityPatronWitch;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelPatronWitchElf extends BipedModel<EntityPatronWitch> implements IPonytailModel
{
	ModelRenderer wingRight, wingLeft;
	
	ModelRenderer bustle1;
	ModelRenderer bustle2;
	
	public ModelRenderer ponytail;
	public ModelRenderer ponytailAnchor;
	public ModelRenderer ponytailAnchor2;
	
	public ModelPatronWitchElf()
	{
		super(0F, 0F, 64, 64);
		
		this.ponytailAnchor = ModelUtils.freshRenderer(this).addBox(0F, 0F, 0F, 1, 1, 1);
			this.ponytailAnchor2 = ModelUtils.freshRenderer(this).addBox(0F, 0F, 0F, 1, 1, 1);
			this.ponytailAnchor2.setRotationPoint(0, -5, 4);
		
        this.ponytail = ModelUtils.freshRenderer(this).setTextureSize(64, 32);
        this.ponytail.addBox(-5F, 0F, 0F, 10, 16, 1);
        	this.ponytailAnchor2.addChild(ponytail);
				this.ponytailAnchor.addChild(ponytailAnchor2);
		
			ModelRenderer earLeft1 = ModelUtils.freshRenderer(this);
			earLeft1.setRotationPoint(4F, -5.5F, 0F);
			earLeft1.setTextureOffset(24, 0).addBox(-0.5F, 0F, -0.5F, 1, 1, 4);
			earLeft1.rotateAngleX = ModelUtils.toRadians(13D);
			earLeft1.rotateAngleY = ModelUtils.toRadians(22D);
		this.bipedHead.addChild(earLeft1);
			ModelRenderer earLeft2 = ModelUtils.freshRenderer(this);
			earLeft2.setRotationPoint(4F, -4.5F, 0F);
			earLeft2.setTextureOffset(24, 2).addBox(0F, 0F, 0F, 0, 2, 3);
			earLeft2.rotateAngleX = ModelUtils.toRadians(13D);
			earLeft2.rotateAngleY = ModelUtils.toRadians(22D);
		this.bipedHead.addChild(earLeft2);
		
			ModelRenderer earRight1 = ModelUtils.freshRenderer(this);
			earRight1.setRotationPoint(-4F, -5.5F, 0F);
			earRight1.setTextureOffset(24, 0).addBox(-0.5F, 0F, -0.5F, 1, 1, 4);
			earRight1.rotateAngleX = ModelUtils.toRadians(13D);
			earRight1.rotateAngleY = ModelUtils.toRadians(-22D);
		this.bipedHead.addChild(earRight1);
			ModelRenderer earRight2 = ModelUtils.freshRenderer(this);
			earRight2.setRotationPoint(-4F, -4.5F, 0F);
			earRight2.setTextureOffset(24, 2).addBox(0F, 0F, 0F, 0, 2, 3);
			earRight2.rotateAngleX = ModelUtils.toRadians(13D);
			earRight2.rotateAngleY = ModelUtils.toRadians(-22D);
		this.bipedHead.addChild(earRight2);
		
		this.bipedBody = ModelUtils.freshRenderer(this);
		this.bipedBody.setRotationPoint(0F, 0F, 0F);
		this.bipedBody.setTextureOffset(16, 16).addBox(-4F, 0F, -2F, 8, 5, 4);
		this.bipedBody.setTextureOffset(16, 25).addBox(-4F, 0F, -2F, 8, 5, 4, 0.5F);
		this.bipedBody.setTextureOffset(16, 34).addBox(-3.5F, 5F, -1.5F, 7, 5, 3);
			ModelRenderer bustleTop1 = ModelUtils.freshRenderer(this);
			bustleTop1.rotateAngleX = ModelUtils.toRadians(4D);
			bustleTop1.setRotationPoint(0F, 9F, 0F);
			bustleTop1.setTextureOffset(36, 34).addBox(-4F, 0F, -2F, 8, 1, 4);
		this.bipedBody.addChild(bustleTop1);
			ModelRenderer bustleTop2 = ModelUtils.freshRenderer(this);
			bustleTop2.rotateAngleX = ModelUtils.toRadians(10D);
			bustleTop2.setRotationPoint(0F, 10F, 0F);
			bustleTop2.setTextureOffset(16, 42).addBox(-4.5F, 0F, -2.5F, 9, 4, 6);
		this.bipedBody.addChild(bustleTop2);
		
		this.bipedRightArm = ModelUtils.freshRenderer(this);
		this.bipedRightArm.setRotationPoint(-5F, 2F, 0F);
		this.bipedRightArm.setTextureOffset(40, 16).addBox(-2F, -2F, -2F, 3, 12, 4);
		this.bipedRightArm.setTextureOffset(40, 39).addBox(-2F, -2F, -2F, 3, 4, 4, 0.5F);
		
		this.bipedLeftArm = ModelUtils.freshRenderer(this);
		this.bipedLeftArm.mirror = true;
		this.bipedLeftArm.setRotationPoint(5F, 2F, 0F);
		this.bipedLeftArm.setTextureOffset(40, 16).addBox(-1F, -2F, -2F, 3, 12, 4);
		this.bipedLeftArm.setTextureOffset(40, 39).addBox(-1F, -2F, -2F, 3, 4, 4, 0.5F);
		
		this.bustle1 = ModelUtils.freshRenderer(this);
		this.bustle1.rotateAngleX = ModelUtils.toRadians(4D);
		this.bustle1.setRotationPoint(0F, 12F, 0F);
		this.bustle1.setTextureOffset(0, 52).addBox(-4.5F, 0F, -1F, 9, 5, 6, 0.2F);
		this.bipedBody.addChild(this.bustle1);
		
		this.bustle2 = ModelUtils.freshRenderer(this);
		this.bustle2.rotateAngleX = ModelUtils.toRadians(4D);
		this.bustle2.setRotationPoint(0F, 3F, 0F);
		this.bustle2.setTextureOffset(30, 52).addBox(-4.5F, 0F, 0F, 9, 6, 6, 0.3F);
		this.bustle1.addChild(this.bustle2);
        
		wingRight = ModelUtils.freshRenderer(this);
		wingRight.setRotationPoint(0F, 4F, 2F);
		wingRight.setTextureOffset(0, 32).addBox(-5F, 0F, 0F, 5, 15, 1);
			this.bipedBody.addChild(wingRight);
		
		wingLeft = ModelUtils.freshRenderer(this);
		wingLeft.setRotationPoint(0F, 4F, 2F);
		wingLeft.mirror = true;
		wingLeft.setTextureOffset(0, 32).addBox(0F, 0F, 0F, 5, 15, 1);
			this.bipedBody.addChild(wingLeft);
	}
	
    public void setRotationAngles(EntityPatronWitch entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
    	super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	
    	ponytailAnchor.copyModelAngles(bipedHead);
    	ponytail.rotateAngleX = Math.max(bipedBody.rotateAngleX - bipedHead.rotateAngleX, -0.259F);
    	
    	float bustleX = Math.max(this.bipedLeftLeg.rotateAngleX, this.bipedRightLeg.rotateAngleX) + ModelUtils.toRadians(4D);
    	bustleX += Math.sin(ageInTicks / 35F) * ModelUtils.toRadians(4D);
    	
    	this.bustle1.rotateAngleX = bustleX;
    	this.bustle2.rotateAngleX = bustleX * 0.5F;
    	
    	this.wingRight.rotateAngleZ = 0F;
    	if(entityIn instanceof IChangeling)
    	{
    		IChangeling changeling = (IChangeling)entityIn;
    		if(changeling.isFlapping())
		    	this.wingRight.rotateAngleZ = ModelUtils.toRadians(2D) + (ModelUtils.toRadians(5D) * (float)(Math.sin(changeling.getFlappingTime() * 2) + 1F));
    	}
    	this.wingLeft.rotateAngleZ = -this.wingRight.rotateAngleZ;
    	
    	bustleX += ModelUtils.toRadians(17D);
    	this.wingLeft.rotateAngleX = bustleX;
    	this.wingRight.rotateAngleX = bustleX;
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
}
