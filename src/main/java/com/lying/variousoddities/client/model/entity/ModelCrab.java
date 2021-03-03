package com.lying.variousoddities.client.model.entity;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.client.model.EnumLimbPosition;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.AbstractCrab;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class ModelCrab extends EntityModel<AbstractCrab>
{
	ModelRenderer body;
	ModelClaw rightClaw, leftClaw;
	List<ModelLeg> leftLegs = new ArrayList<ModelLeg>();
	List<ModelLeg> rightLegs = new ArrayList<ModelLeg>();
	
	private static final float legOffset = 7F;
	
	private boolean bigLeft, bigRight;
	private boolean scuttle;
	
	public ModelCrab()
	{
		this.textureHeight = 64;
		this.textureWidth = 64;
		
		this.body = ModelUtils.freshRenderer(this);
		this.body.rotationPointY = 17F;
		this.body.setTextureOffset(0, 0).addBox(-7F, -4F, -6F, 14, 6, 12, 1.5F);
		this.body.setTextureOffset(0, 0).addBox(-5F, -2F, -7.8F, 2, 2, 1, 0.2F);
		this.body.setTextureOffset(46, 0).addBox(3.5F, -2F, -7.8F, 2, 2, 1, 0.2F);
		
		ModelRenderer carapace = ModelUtils.freshRenderer(this);
		carapace.rotationPointY = -3.5F;
		carapace.rotationPointZ = 1.7F;
		carapace.rotateAngleX = -ModelUtils.degree10;
		carapace.setTextureOffset(0, 18).addBox(-8F, -1.5F, -8F, 16, 3, 15, 1.5F);
		this.body.addChild(carapace);
		
		this.rightClaw = new ModelClaw(EnumLimbPosition.LEFT, this);
		this.leftClaw = new ModelClaw(EnumLimbPosition.RIGHT, this);
		
		for(int i=-1; i<2; i++)
		{
			leftLegs.add(new ModelLeg(i, EnumLimbPosition.LEFT, this));
			rightLegs.add(new ModelLeg(i, EnumLimbPosition.RIGHT, this));
		}
	}
	
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
	{
		matrixStackIn.push();
			if(this.scuttle)
	    		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90F));
			
			this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			
			float bigScale = 1.2F;
			matrixStackIn.push();
				if(this.bigLeft)
				{
					matrixStackIn.scale(bigScale, bigScale, bigScale);
		    		matrixStackIn.translate(-0.05F, -0.15F, 0.05F);
				}
				this.leftClaw.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			matrixStackIn.pop();
			matrixStackIn.push();
				if(this.bigRight)
				{
					matrixStackIn.scale(bigScale, bigScale, bigScale);
		    		matrixStackIn.translate(0.05F, -0.15F, 0.05F);
				}
				this.rightClaw.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			matrixStackIn.pop();
			
			for(ModelLeg leg : leftLegs)
				leg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			
			for(ModelLeg leg : rightLegs)
				leg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.pop();
	}
	
	public void setRotationAngles(AbstractCrab entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		this.bigLeft = entityIn.hasBigLeftClaw();
		this.bigRight = entityIn.hasBigRightClaw();
		this.scuttle = entityIn.shouldScuttle();
		
		if(entityIn.isPartying())
		{
			float wiggle = (float)Math.sin(ageInTicks);
			this.body.rotationPointX = wiggle;
			this.rightClaw.setXOffset(wiggle);
			this.leftClaw.setXOffset(wiggle);
			
	    	for(int i=0; i<leftLegs.size(); i++)
	    	{
	    		leftLegs.get(i).setOffsetX(wiggle);
	    		rightLegs.get(i).setOffsetX(wiggle);
	    	}
		}
		else
		{
			this.body.rotationPointX = 0F;
			this.rightClaw.setXOffset(0F);
			this.leftClaw.setXOffset(0F);
			
	    	for(int i=0; i<leftLegs.size(); i++)
	    	{
	    		leftLegs.get(i).setOffsetX(0F);
	    		rightLegs.get(i).setOffsetX(0F);
	    	}
		}
		
    	for(int i=0; i<leftLegs.size(); i++)
    	{
    		leftLegs.get(i).setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    		rightLegs.get(i).setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	}
    	
    	rightClaw.swingProgress = this.swingProgress;
    	rightClaw.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	leftClaw.swingProgress = this.swingProgress;
    	leftClaw.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
	}
	
    /**
     * Sets the models various rotation angles then renders the model.
     */
//    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
//    {
//    	this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, (AbstractCrab)entityIn);
//    	AbstractCrab theCrab = (AbstractCrab)entityIn;
//    	boolean shouldScuttle = AbstractCrab.shouldScuttle(theCrab);
//    	if(shouldScuttle)
//    	{
//    		GlStateManager.pushMatrix();
//    		GlStateManager.rotate(90F, 0F, 1F, 0F);
//    	}
//    	body.render(scale);
//    	
//    	float bigScale = 1.2F;
//    	boolean bigLeft = theCrab.getBigHand(HandSide.LEFT);
//    	if(bigLeft)
//    	{
//    		GlStateManager.pushMatrix();
//    		GlStateManager.scale(bigScale, bigScale, bigScale);
//    		GlStateManager.translate(-0.05F, -0.15F, 0.05F);
//    	}
//    	leftClaw.render(scale);
//    	if(bigLeft){ GlStateManager.popMatrix(); }
//
//    	boolean bigRight = theCrab.getBigHand(HandSide.RIGHT);
//    	if(bigRight)
//    	{
//    		GlStateManager.pushMatrix();
//    		GlStateManager.scale(bigScale, bigScale, bigScale);
//    		GlStateManager.translate(0.05F, -0.15F, 0.05F);
//    	}
//    	rightClaw.render(scale);
//    	if(bigRight){ GlStateManager.popMatrix(); }
//    	
//    	for(ModelLeg leg : leftLegs){ leg.render(scale); }
//    	for(ModelLeg leg : rightLegs){ leg.render(scale); }
//    	
//    	if(shouldScuttle)
//    	{
//    		GlStateManager.popMatrix();
//    	}
//    }
    
    public class ModelClaw
    {
    	public float swingProgress;
    	
    	private final EnumLimbPosition side;
    	private final boolean isLeft;
    	
    	ModelRenderer theArm;
    	ModelRenderer theClaw;
    	
    	private final float defaultRotationX;
    	private float offsetRotationX = 0F;
    	
    	public ModelClaw(EnumLimbPosition side, Model theModel)
    	{
    		this.side = side;
    		this.isLeft = this.side == EnumLimbPosition.LEFT;
    		
    		ModelRenderer armBase = ModelUtils.freshRenderer(theModel);
    		armBase.mirror = isLeft;
    		armBase.setTextureOffset(16, 36).addBox(-1.5F, -1F, -1.5F, 3, 5, 3);
    		
    		this.theArm = ModelUtils.freshRenderer(theModel);
    		defaultRotationX = 8F * (isLeft ? -1F : 1F);
    		theArm.setRotationPoint(defaultRotationX, 13.5F, -6F);
    		theArm.addChild(armBase);
    		
    		this.theClaw = ModelUtils.freshRenderer(theModel);
    		theClaw.mirror = isLeft;
    		theClaw.rotationPointY = 4F;
    		theClaw.setTextureOffset(28, 36).addBox(-2F, 0F, -2F, 4, 6, 4, 1F);
    		theClaw.setTextureOffset(28, 46).addBox(2F * (isLeft ? 1F : -1.6F), -0.5F, -1F, 1, 3, 2, 0.75F);
    		theArm.addChild(theClaw);
    	}
    	
    	public void setXOffset(float par1Float)
    	{
    		this.offsetRotationX = par1Float;
    	}
        
        /**
         * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
         * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
         * "far" arms and legs can swing at most.
         */
    	public void setRotationAngles(AbstractCrab entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
        {
    		this.theArm.rotationPointX = defaultRotationX + offsetRotationX;
            if(swingProgress > 0.0F && swingSideMatches(this.getMainHand(entityIn)))
        	{
            	theClaw.rotateAngleZ = 0F;
            	
            	theArm.rotateAngleX = -ModelUtils.degree90;
            	theArm.rotateAngleY = 0F;
            	theArm.rotateAngleZ = 0F;
            	
                float f1 = 1.0F - swingProgress;
                f1 = f1 * f1;
                f1 = f1 * f1;
                f1 = 1.0F - f1;
                float f2 = MathHelper.sin(f1 * (float)Math.PI);
                float f3 = MathHelper.sin(swingProgress * (float)Math.PI) * 0.7F * 0.75F;
                theArm.rotateAngleY = (float)((double)theArm.rotateAngleY - ((double)f2 * 1.2D + (double)f3)) * (isLeft ? -1F : 1F);
                theArm.rotateAngleZ += MathHelper.sin(swingProgress * (float)Math.PI) * -0.4F;
        	}
            else
            {
            	theClaw.rotateAngleZ = ModelUtils.degree10 * 4F * (isLeft ? -1F : 1F);
            	
            	theArm.rotateAngleX = -ModelUtils.degree180 * 0.75F;
            	theArm.rotateAngleX += (float)Math.cos(entityIn.ticksExisted / 20F) * ModelUtils.degree10 * 0.5F;
            	theArm.rotateAngleY = ModelUtils.degree90 * 0.5F * (isLeft ? 1F : -1F);
            	theArm.rotateAngleZ = this.offsetRotationX * 0.5F;
            }
        }

    	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    	{
    		theArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    	}
    	
    	public boolean swingSideMatches(HandSide handIn)
    	{
    		if(handIn == HandSide.LEFT && this.side == EnumLimbPosition.LEFT){ return true; }
    		if(handIn == HandSide.RIGHT && this.side == EnumLimbPosition.RIGHT){ return true; }
    		return false;
    	}

        protected HandSide getMainHand(Entity entityIn)
        {
            if (entityIn instanceof LivingEntity)
            {
                LivingEntity LivingEntity = (LivingEntity)entityIn;
                HandSide HandSide = LivingEntity.getPrimaryHand();
                return LivingEntity.swingingHand == Hand.MAIN_HAND ? HandSide : HandSide.opposite();
            }
            else
            {
                return HandSide.RIGHT;
            }
        }
        
        public ModelRenderer getClaw(){ return this.theArm; }
    }
    
    public class ModelLeg
    {
    	private final int index;
    	private final EnumLimbPosition side;
    	private final boolean isLeft;
    	
    	private ModelRenderer theLegUpper;
    	private ModelRenderer theLegLower;
    	
    	private final float defaultRotationX;
    	private float offsetRotationX = 0F;
    	
    	public ModelLeg(int index, EnumLimbPosition side, Model theModel)
    	{
    		this.index = index;
    		this.side = side;
    		this.isLeft = this.side == EnumLimbPosition.LEFT;
    		
    		float pol = (isLeft ? 1F : -1F);
    		
    		// Leg parts are treated as horizontal for the orientation code
    		// But are generated vertically for ease of texturing
    		ModelRenderer upperLeg = ModelUtils.freshRenderer(theModel);
    		upperLeg.rotateAngleZ = -ModelUtils.degree90;
    		upperLeg.setTextureOffset(0, 36).addBox(0-1.5F, 0F, -1.5F, 3, 6, 3);
			
			ModelRenderer lowerLeg = ModelUtils.freshRenderer(theModel);
			lowerLeg.rotateAngleZ = -ModelUtils.degree90;
			lowerLeg.setTextureOffset(0, 45).addBox(-1F, 0F, -1F, 2, 8, 2, 0.2F);
    		
			// Main leg elements, these control the rotation so the parts are always properly oriented
			this.theLegUpper = ModelUtils.freshRenderer(theModel);
			defaultRotationX = (legOffset + (index%2 == 0 ? 1.5F : 0F)) * pol;
			theLegUpper.setRotationPoint(defaultRotationX, 18F, index * 5.2F);
			theLegUpper.rotateAngleZ = -ModelUtils.degree10 * pol;
			theLegUpper.rotateAngleY = -ModelUtils.toRadians(10D) * index * pol;
			if(!isLeft){ theLegUpper.rotateAngleY += ModelUtils.toRadians(180D); }
			theLegUpper.addChild(upperLeg);
			
			this.theLegLower = ModelUtils.freshRenderer(theModel);
			theLegLower.rotateAngleZ = ModelUtils.degree90 / 1.5F;
			theLegLower.rotationPointX = 5F;
			theLegLower.addChild(lowerLeg);
			theLegUpper.addChild(theLegLower);
    	}
    	
    	public void setOffsetX(float par1Float){ this.offsetRotationX = par1Float; }
        
        /**
         * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
         * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
         * "far" arms and legs can swing at most.
         */
    	public void setRotationAngles(AbstractCrab entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
        {
        	float legBase = MathHelper.cos(limbSwing * 1.5F + (float)Math.PI) * 2F * limbSwingAmount;
        	boolean limbPolarity = (isLeft ? index%2 == 0 : index%2 != 0);
        	
    		this.theLegUpper.rotationPointX = defaultRotationX + offsetRotationX;
        	
    		theLegUpper.rotateAngleZ = Math.min(-ModelUtils.degree10, (legBase * (limbPolarity ? 1 : -1)) - ModelUtils.degree10) * (isLeft ? 1F : -1F);
    		theLegUpper.rotateAngleZ += offsetRotationX * ModelUtils.toRadians(22.5D);
    		
    		theLegUpper.rotateAngleY = !isLeft ? ModelUtils.degree180 : 0F;
    		theLegUpper.rotateAngleY += (MathHelper.cos(limbSwing * 0.66682F + (float)Math.PI) * 0.8F * limbSwingAmount) * (limbPolarity ? 1F : -1F);
    		
    		theLegLower.rotateAngleZ = ModelUtils.degree90 / 1.5F;
    		theLegLower.rotateAngleZ -= ((((isLeft ? 1F : -1F )*ModelUtils.degree10) + theLegUpper.rotateAngleZ) * 1F) * (isLeft ? 1F : -1F);
        }

    	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    	{
    		this.theLegUpper.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    	}
    }
}
