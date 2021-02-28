package com.lying.variousoddities.client.model.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.lying.variousoddities.client.model.EnumLimbPosition;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.AbstractScorpion;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

public class ModelScorpion extends AgeableModel<AbstractScorpion>
{
	ModelRenderer body;
	ModelClaw rightClaw, leftClaw;
	ModelTail tail;
	List<ModelLeg> leftLegs = new ArrayList<ModelLeg>();
	List<ModelLeg> rightLegs = new ArrayList<ModelLeg>();
	
	private static final float legOffset = 5F;
	
	public ModelScorpion()
	{
		this.textureHeight = 64;
		this.textureWidth = 32;
		
		this.body = ModelUtils.freshRenderer(this);
		this.body.rotationPointY = 18F;
		this.body.setTextureOffset(14, 16).addBox(-5F, -3F, -9F, 2, 2, 1, 0.2F);
		this.body.setTextureOffset(14, 13).addBox(3.5F, -3F, -9F, 2, 2, 1, 0.2F);
			ModelRenderer bodyBase = ModelUtils.freshRenderer(this);
			bodyBase.rotateAngleX = -ModelUtils.degree90;
			bodyBase.setTextureOffset(0, 0).addBox(-3F, -13F, -1F, 6, 18, 1, 3.5F);
		this.body.addChild(bodyBase);
		
		this.tail = new ModelTail(this);
		
		this.rightClaw = new ModelClaw(EnumLimbPosition.RIGHT, this);
		this.leftClaw = new ModelClaw(EnumLimbPosition.LEFT, this);
		
		for(int i=-2; i<2; i++)
		{
			leftLegs.add(new ModelLeg(i, EnumLimbPosition.LEFT, this));
			rightLegs.add(new ModelLeg(i, EnumLimbPosition.RIGHT, this));
		}
	}
    
    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(AbstractScorpion entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
    	rightClaw.swingProgress = leftClaw.swingProgress = tail.swingProgress = this.swingProgress;
    	
    	tail.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	rightClaw.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	leftClaw.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	for(int i=0; i<leftLegs.size(); i++)
    	{
    		leftLegs.get(i).setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    		rightLegs.get(i).setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	}
    }
    
	protected Iterable<ModelRenderer> getHeadParts(){ return Collections.emptyList(); }
	
	protected Iterable<ModelRenderer> getBodyParts()
	{
		return Arrays.asList(this.body, this.tail, this.rightClaw, this.leftClaw, rightLegs.get(0), leftLegs.get(0), rightLegs.get(1), leftLegs.get(1), rightLegs.get(2), leftLegs.get(2), rightLegs.get(3), leftLegs.get(3));
	}
    
    public class ModelClaw extends ModelRenderer
    {
    	public float swingProgress;
    	
    	private final EnumLimbPosition side;
    	private final boolean isLeft;
    	
    	ModelRenderer theArm;
    	ModelRenderer theClaw;
    	
    	public ModelClaw(EnumLimbPosition sideIn, Model theModel)
    	{
    		super(theModel);
    		side = sideIn;
    		isLeft = sideIn == EnumLimbPosition.LEFT;
    		
    		ModelRenderer armBase = ModelUtils.freshRenderer(theModel);
    		armBase.mirror = isLeft;
    		armBase.setTextureOffset(12, 19).addBox(-1.5F, -2.5F, -1.5F, 3, 7, 3);
    		
    		this.theArm = ModelUtils.freshRenderer(theModel);
    		theArm.setRotationPoint(8F * (isLeft ? -1F : 1F), 16.5F, -7.5F);
    		theArm.addChild(armBase);
    		
    		this.theClaw = ModelUtils.freshRenderer(theModel);
    		theClaw.mirror = isLeft;
    		theClaw.rotationPointY = 4F;
    		theClaw.setTextureOffset(12, 29).addBox(-2F, 0F, -2F, 4, 6, 4, 1F);
    		theClaw.setTextureOffset(24, 24).addBox(2F * (isLeft ? 1F : -1.6F), -0.5F, -1F, 1, 3, 2, 0.75F);
    		theArm.addChild(theClaw);
    	}
        
        /**
         * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
         * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
         * "far" arms and legs can swing at most.
         */
        public void setRotationAngles(AbstractScorpion entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
        {
            if(swingProgress > 0.0F && swingSideMatches(this.getMainHand(entityIn)))
        	{
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
        	}
            else
            {
            	theClaw.rotateAngleZ = ModelUtils.degree10 * 4F * (isLeft ? -1F : 1F);
            	
            	theArm.rotateAngleX = -ModelUtils.degree180 * 0.5F;
            	theArm.rotateAngleX += (float)Math.cos(entityIn.ticksExisted / 20F) * ModelUtils.degree10 * 0.5F;
            	theArm.rotateAngleY = ModelUtils.degree90 * 0.5F * (isLeft ? 1F : -1F);
            	theArm.rotateAngleZ = 0F;
            }
        }
    	
    	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    	{
    		theArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    	}
    	
    	public boolean swingSideMatches(HandSide handIn)
    	{
    		if(handIn == HandSide.LEFT && this.side == EnumLimbPosition.RIGHT){ return true; }
    		if(handIn == HandSide.RIGHT && this.side == EnumLimbPosition.LEFT){ return true; }
    		return false;
    	}

        protected HandSide getMainHand(Entity entityIn)
        {
            if (entityIn instanceof LivingEntity)
            {
                LivingEntity entitylivingbase = (LivingEntity)entityIn;
                HandSide enumhandside = entitylivingbase.getPrimaryHand();
                return entitylivingbase.swingingHand == Hand.MAIN_HAND ? enumhandside : enumhandside.opposite();
            }
            else
            {
                return HandSide.RIGHT;
            }
        }
        
        public ModelRenderer getClaw(){ return this.theArm; }
    }
    
    public class ModelTail extends ModelRenderer
    {
    	public float swingProgress;
    	
    	List<ModelRenderer> tailSegments = new ArrayList<ModelRenderer>();
    	ModelRenderer tailRoot;
    	ModelRenderer tailStinger;
    	
    	public ModelTail(Model theModel)
    	{
    		super(theModel);
    		this.tailRoot = ModelUtils.freshRenderer(theModel);
    		this.tailRoot.setRotationPoint(0F, 14.5F, 19F);
    		this.tailRoot.setTextureOffset(16, 0).addBox(-2.5F, -1F, -2F, 5, 2, 3, 2F);
    		this.tailSegments.add(tailRoot);
    		
    		for(int i=0; i<3; i++)
    		{
    			ModelRenderer tailSegment = ModelUtils.freshRenderer(theModel);
    			tailSegment.rotationPointZ = 8F - (i * 0.3F);
    			tailSegment.setTextureOffset(14, 5).addBox(-2F, -0.5F, 0F, 4, 1, 5, 2F - (i * 0.6F));
    			
    			tailSegments.get(i).addChild(tailSegment);
    			tailSegments.add(tailSegment);
    		}
    		tailSegments.get(1).rotationPointZ -= 4F;
    		
    		this.tailStinger = ModelUtils.freshRenderer(theModel);
    		this.tailStinger.rotationPointZ = 6.5F;
    		this.tailStinger.setTextureOffset(24, 11).addBox(-1F, 0F, -1F, 2, 4, 2, 0.25F);
    		tailSegments.get(tailSegments.size()-1).addChild(tailStinger);
    	}
        
        /**
         * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
         * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
         * "far" arms and legs can swing at most.
         */
        public void setRotationAngles(AbstractScorpion entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
        {
        	if(this.swingProgress > 0.0F && !entityIn.isChild())
        	{
        		this.swingProgress = 1 - (Math.abs(this.swingProgress-0.5F)*2F);
        		
        		float tailStart = ModelUtils.toRadians(48D);
        		float tailEnd = ModelUtils.toRadians(-5D);
        		for(ModelRenderer segment : tailSegments)
        		{
        			segment.rotateAngleX = tailStart + (tailEnd - tailStart)*this.swingProgress;
        		}

        		float rootStart = ModelUtils.toRadians(58D);
        		float rootEnd = ModelUtils.toRadians(180D);
        		tailRoot.rotateAngleX = rootStart + (rootEnd - rootStart)*this.swingProgress;
        		
        		tailRoot.rotationPointY = 14.5F - (4F * this.swingProgress);
        		tailRoot.rotationPointZ = 19F - (6F * this.swingProgress);
        	}
        	else
        	{
        		this.tailRoot.setRotationPoint(0F, 14.5F, 19F);
        		
	        	float rotation = (float)Math.sin(ageInTicks / 20);
	        	rotation *= Math.signum(rotation);
	        	rotation = rotation*ModelUtils.toRadians(10D) + ModelUtils.toRadians(38D);
	        	for(ModelRenderer segment : tailSegments)
	        	{
	        		segment.rotateAngleX = rotation;
	        	}
        	}
        	tailStinger.rotateAngleX = ModelUtils.toRadians(180D);
        }
    	
    	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    	{
    		tailRoot.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    	}
    }
    
    public class ModelLeg extends ModelRenderer
    {
    	private final int index;
    	private final EnumLimbPosition side;
    	private final boolean isLeft;
    	
    	private ModelRenderer theLegUpper;
    	private ModelRenderer theLegLower;
    	
    	public ModelLeg(int index, EnumLimbPosition side, Model theModel)
    	{
    		super(theModel);
    		this.index = index;
    		this.side = side;
    		this.isLeft = this.side == EnumLimbPosition.LEFT;
    		
    		float pol = (isLeft ? 1F : -1F);
    		
    		// Leg parts are treated as horizontal for the orientation code
    		// But are generated vertically for ease of texturing
    		ModelRenderer upperLeg = ModelUtils.freshRenderer(theModel);
    		upperLeg.rotateAngleZ = -ModelUtils.degree90;
    		upperLeg.setTextureOffset(0, 19).addBox(0-1.5F, 0F, -1.5F, 3, 6, 3);
			
			ModelRenderer lowerLeg = ModelUtils.freshRenderer(theModel);
			lowerLeg.rotateAngleZ = -ModelUtils.degree90;
			lowerLeg.setTextureOffset(0, 28).addBox(-1F, 0F, -1F, 2, 8, 2, 0.2F);
    		
			// Main leg elements, these control the rotation so the parts are always properly oriented
			this.theLegUpper = ModelUtils.freshRenderer(theModel);
			theLegUpper.setRotationPoint((legOffset + (index%2 == 0 ? 1.5F : 0F)) * pol, 18F, 8F + index * 5.5F);
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
        
        /**
         * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
         * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
         * "far" arms and legs can swing at most.
         */
        public void setRotationAngles(AbstractScorpion entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
        {
        	float legBase = MathHelper.cos(limbSwing * 1.5F + (float)Math.PI) * 2F * limbSwingAmount;
        	boolean limbPolarity = (isLeft ? index%2 == 0 : index%2 != 0);
        	
    		theLegUpper.rotateAngleZ = Math.min(-ModelUtils.degree10, (legBase * (limbPolarity ? 1 : -1)) - ModelUtils.degree10) * (isLeft ? 1F : -1F);
    		
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
