package com.lying.variousoddities.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.wip.EntityPatronWitch;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class ModelPatronWitchCrone extends BipedModel<EntityPatronWitch> implements IPonytailModel
{
	ModelRenderer skirting;
	
	public ModelRenderer ponytail;
	public ModelRenderer ponytailAnchor;
	public ModelRenderer ponytailAnchor2;
	
	public ModelPatronWitchCrone()
	{
		super(0, 0, 64, 64);
		this.textureHeight = 64;
		this.textureWidth = 64;
		
		this.bipedHead = ModelUtils.freshRenderer(this);
		this.bipedHead.setRotationPoint(0F, 0F, -3F);
		this.bipedHead.setTextureOffset(0, 0).addBox(-4F, -8F, -4F, 8, 8, 8);
		this.bipedHead.setTextureOffset(32, 0).addBox(-4F, -8F, -4F, 8, 8, 8, 0.5F);
		
		this.ponytailAnchor = ModelUtils.freshRenderer(this).addBox(0F, 0F, 0F, 1, 1, 1);
			this.ponytailAnchor2 = ModelUtils.freshRenderer(this).addBox(0F, 0F, 0F, 1, 1, 1);
			this.ponytailAnchor2.setRotationPoint(0, -5, 4);
		
        this.ponytail = ModelUtils.freshRenderer(this).setTextureSize(64, 32);
        this.ponytail.addBox(-5F, 0F, 0F, 10, 16, 1);
        	this.ponytailAnchor2.addChild(ponytail);
				this.ponytailAnchor.addChild(ponytailAnchor2);
		
		this.bipedBody = ModelUtils.freshRenderer(this);
			// Tunic
		this.bipedBody.setTextureOffset(16, 37).addBox(-4, 0, -4, 8, 9, 4, 0.5F);
			ModelRenderer body1 = ModelUtils.freshRenderer(this);
			body1.setRotationPoint(0, 9, 0);
			body1.setTextureOffset(16, 16).addBox(-4F, -9, -2, 8, 9, 4);
			body1.rotateAngleX = ModelUtils.toRadians(16D);
				this.bipedBody.addChild(body1);
			ModelRenderer body2 = ModelUtils.freshRenderer(this);
			body2.setRotationPoint(0, 8, 0);
			body2.setTextureOffset(16, 29).addBox(-4, 0, -2, 8, 4, 4);
				this.bipedBody.addChild(body2);
			this.skirting = ModelUtils.freshRenderer(this);
			this.skirting.setRotationPoint(0, 9, 0);
			this.skirting.setTextureOffset(16, 50).addBox(-4, 0, -2, 8, 9, 4, 0.5F);
				this.bipedBody.addChild(this.skirting);
		
		this.bipedRightArm = ModelUtils.freshRenderer(this);
		this.bipedRightArm.setRotationPoint(-5, 2, -2);
		this.bipedRightArm.setTextureOffset(40, 16).addBox(-2, -2, -2, 3, 12, 4);
		this.bipedRightArm.setTextureOffset(40, 32).addBox(-2, -2, -2, 3, 12, 4, 0.5F);
		
		this.bipedLeftArm = ModelUtils.freshRenderer(this);
		this.bipedLeftArm.setRotationPoint(5, 2, -2);
		this.bipedLeftArm.mirror = true;
		this.bipedLeftArm.setTextureOffset(40, 16).addBox(-1, -2, -2, 3, 12, 4);
		this.bipedLeftArm.setTextureOffset(40, 32).addBox(-1, -2, -2, 3, 12, 4, 0.5F);
		
        this.bipedRightLeg = ModelUtils.freshRenderer(this);
        this.bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
        this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        
        this.bipedLeftLeg = ModelUtils.freshRenderer(this);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
	}
	
	protected Iterable<ModelRenderer> getHeadParts()
	{
	   return ImmutableList.of(this.bipedHead);
	}
	
	protected Iterable<ModelRenderer> getBodyParts()
	{
		return ImmutableList.of(this.bipedBody, this.bipedRightArm, this.bipedLeftArm, this.bipedRightLeg, this.bipedLeftLeg);
	}

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    @SuppressWarnings("incomplete-switch")
    public void setRotationAngles(EntityPatronWitch entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        boolean isFlying = entityIn.getTicksElytraFlying() > 4;
        this.bipedHead.rotateAngleY = netHeadYaw * 0.017453292F;
        
        if(isFlying)
            this.bipedHead.rotateAngleX = -((float)Math.PI / 4F);
        else
            this.bipedHead.rotateAngleX = headPitch * 0.017453292F;

        this.bipedBody.rotateAngleY = 0.0F;
        this.bipedRightArm.rotationPointZ = -2F;
        this.bipedRightArm.rotationPointX = -5.0F;
        this.bipedLeftArm.rotationPointZ = -2F;
        this.bipedLeftArm.rotationPointX = 5.0F;
        float f = 1.0F;
        if(isFlying)
        {
        	Vector3d motion = entityIn.getMotion();
            f = (float)(motion.getX() * motion.getX() + motion.getY() * motion.getY() + motion.getZ() * motion.getZ());
            f = f / 0.2F;
            f = f * f * f;
        }
        
        if(f < 1.0F) f = 1.0F;
        
        this.bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F / f;
        this.bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
        this.bipedRightArm.rotateAngleZ = 0.0F;
        this.bipedLeftArm.rotateAngleZ = 0.0F;
        this.bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
        this.bipedLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount / f;
        this.bipedRightLeg.rotateAngleY = 0.0F;
        this.bipedLeftLeg.rotateAngleY = 0.0F;
        this.bipedRightLeg.rotateAngleZ = 0.0F;
        this.bipedLeftLeg.rotateAngleZ = 0.0F;

        if(this.isSitting)
        {
            this.bipedRightArm.rotateAngleX += -((float)Math.PI / 5F);
            this.bipedLeftArm.rotateAngleX += -((float)Math.PI / 5F);
            this.bipedRightLeg.rotateAngleX = -1.4137167F;
            this.bipedRightLeg.rotateAngleY = ((float)Math.PI / 10F);
            this.bipedRightLeg.rotateAngleZ = 0.07853982F;
            this.bipedLeftLeg.rotateAngleX = -1.4137167F;
            this.bipedLeftLeg.rotateAngleY = -((float)Math.PI / 10F);
            this.bipedLeftLeg.rotateAngleZ = -0.07853982F;
        }
        
        this.bipedRightArm.rotateAngleY = 0.0F;
        this.bipedRightArm.rotateAngleZ = 0.0F;

        switch(this.leftArmPose)
        {
            case EMPTY:
                this.bipedLeftArm.rotateAngleY = 0.0F;
                break;
            case BLOCK:
                this.bipedLeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX * 0.5F - 0.9424779F;
                this.bipedLeftArm.rotateAngleY = 0.5235988F;
                break;
            case ITEM:
                this.bipedLeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F);
                this.bipedLeftArm.rotateAngleY = 0.0F;
        }
        
        switch (this.rightArmPose)
        {
            case EMPTY:
                this.bipedRightArm.rotateAngleY = 0.0F;
                break;
            case BLOCK:
                this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX * 0.5F - 0.9424779F;
                this.bipedRightArm.rotateAngleY = -0.5235988F;
                break;
            case ITEM:
                this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F);
                this.bipedRightArm.rotateAngleY = 0.0F;
        }
        
        if(this.swingProgress > 0.0F)
        {
            HandSide enumhandside = this.getMainHand(entityIn);
            ModelRenderer modelrenderer = this.getArmForSide(enumhandside);
            float f1 = this.swingProgress;
            this.bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt(f1) * ((float)Math.PI * 2F)) * 0.2F;
            
            if (enumhandside == HandSide.LEFT)
            {
                this.bipedBody.rotateAngleY *= -1.0F;
            }

            this.bipedRightArm.rotationPointZ = MathHelper.sin(this.bipedBody.rotateAngleY) * 5.0F;
            this.bipedRightArm.rotationPointX = -MathHelper.cos(this.bipedBody.rotateAngleY) * 5.0F;
            this.bipedLeftArm.rotationPointZ = -MathHelper.sin(this.bipedBody.rotateAngleY) * 5.0F;
            this.bipedLeftArm.rotationPointX = MathHelper.cos(this.bipedBody.rotateAngleY) * 5.0F;
            this.bipedRightArm.rotateAngleY += this.bipedBody.rotateAngleY;
            this.bipedLeftArm.rotateAngleY += this.bipedBody.rotateAngleY;
            this.bipedLeftArm.rotateAngleX += this.bipedBody.rotateAngleY;
            f1 = 1.0F - this.swingProgress;
            f1 = f1 * f1;
            f1 = f1 * f1;
            f1 = 1.0F - f1;
            float f2 = MathHelper.sin(f1 * (float)Math.PI);
            float f3 = MathHelper.sin(this.swingProgress * (float)Math.PI) * -(this.bipedHead.rotateAngleX - 0.7F) * 0.75F;
            modelrenderer.rotateAngleX = (float)((double)modelrenderer.rotateAngleX - ((double)f2 * 1.2D + (double)f3));
            modelrenderer.rotateAngleY += this.bipedBody.rotateAngleY * 2.0F;
            modelrenderer.rotateAngleZ += MathHelper.sin(this.swingProgress * (float)Math.PI) * -0.4F;
        }
        
        if(this.isSneak)
        {
            this.bipedBody.rotateAngleX = 0.5F;
            this.bipedRightArm.rotateAngleX += 0.4F;
            this.bipedLeftArm.rotateAngleX += 0.4F;
            this.bipedRightLeg.rotationPointZ = 4.0F;
            this.bipedLeftLeg.rotationPointZ = 4.0F;
            this.bipedRightLeg.rotationPointY = 9.0F;
            this.bipedLeftLeg.rotationPointY = 9.0F;
            this.bipedHead.rotationPointY = 1.0F;
        }
        else
        {
            this.bipedBody.rotateAngleX = 0.0F;
            this.bipedRightLeg.rotationPointZ = 0.1F;
            this.bipedLeftLeg.rotationPointZ = 0.1F;
            this.bipedRightLeg.rotationPointY = 12.0F;
            this.bipedLeftLeg.rotationPointY = 12.0F;
            this.bipedHead.rotationPointY = 0.0F;
        }
        
        this.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        this.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        
        if (this.rightArmPose == ArmPose.BOW_AND_ARROW)
        {
            this.bipedRightArm.rotateAngleY = -0.1F + this.bipedHead.rotateAngleY;
            this.bipedLeftArm.rotateAngleY = 0.1F + this.bipedHead.rotateAngleY + 0.4F;
            this.bipedRightArm.rotateAngleX = -((float)Math.PI / 2F) + this.bipedHead.rotateAngleX;
            this.bipedLeftArm.rotateAngleX = -((float)Math.PI / 2F) + this.bipedHead.rotateAngleX;
        }
        else if (this.leftArmPose == ArmPose.BOW_AND_ARROW)
        {
            this.bipedRightArm.rotateAngleY = -0.1F + this.bipedHead.rotateAngleY - 0.4F;
            this.bipedLeftArm.rotateAngleY = 0.1F + this.bipedHead.rotateAngleY;
            this.bipedRightArm.rotateAngleX = -((float)Math.PI / 2F) + this.bipedHead.rotateAngleX;
            this.bipedLeftArm.rotateAngleX = -((float)Math.PI / 2F) + this.bipedHead.rotateAngleX;
        }
        
        ponytailAnchor.copyModelAngles(bipedHead);
        this.skirting.rotateAngleX = ModelUtils.toRadians(2D) + Math.max(this.bipedLeftLeg.rotateAngleX, this.bipedRightLeg.rotateAngleX);
    	this.ponytail.rotateAngleX = Math.max(bipedBody.rotateAngleX - bipedHead.rotateAngleX, -0.259F);
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
