package com.lying.variousoddities.client.model.entity;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.client.model.EnumLimbPosition;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.AbstractCrab;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class ModelCrab extends EntityModel<AbstractCrab>
{
	ModelPart body;
	ClawHandler rightClaw, leftClaw;
	List<LegHandler> leftLegs = new ArrayList<LegHandler>();
	List<LegHandler> rightLegs = new ArrayList<LegHandler>();
	
	private static final float legOffset = 7F;
	
	private boolean bigLeft, bigRight;
	private boolean scuttle;
	
	public ModelCrab(ModelPart partsIn)
	{
		this.body = partsIn.getChild("body");
		
		this.rightClaw = new ClawHandler(EnumLimbPosition.LEFT, partsIn.getChild("right_claw"));
		this.leftClaw = new ClawHandler(EnumLimbPosition.RIGHT, partsIn.getChild("left_claw"));
		
		for(int i=-1; i<2; i++)
		{
			leftLegs.add(new LegHandler(i, EnumLimbPosition.LEFT, partsIn.getChild("leg_"+i+"_left")));
			rightLegs.add(new LegHandler(i, EnumLimbPosition.RIGHT, partsIn.getChild("leg_"+i+"_right")));
		}
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		part.addOrReplaceChild("body", CubeListBuilder.create()
			.texOffs(0, 0).addBox(-7F, -4F, -6F, 14, 6, 12, deformation.extend(1.5F))
			.texOffs(0, 0).addBox(-5F, -2F, -7.8F, 2, 2, 1, deformation.extend(0.2F))
			.texOffs(46, 0).addBox(3.5F, -2F, -7.8F, 2, 2, 1, deformation.extend(0.2F)), PartPose.offset(0F, 17F, 0F))
			.addOrReplaceChild("carapace", CubeListBuilder.create().texOffs(0, 18).addBox(-8F, -1.5F, -8F, 16, 3, 15, deformation.extend(1.5F)), PartPose.offsetAndRotation(0F, -3.5F, 1.7F, -ModelUtils.degree10, 0F, 0F));
		
		ClawHandler.addClaw(EnumLimbPosition.RIGHT, part, deformation);
		ClawHandler.addClaw(EnumLimbPosition.LEFT, part, deformation);
		
		for(int i=-1; i<2; i++)
		{
			LegHandler.addLeg(i, EnumLimbPosition.LEFT, part, deformation);
			LegHandler.addLeg(i, EnumLimbPosition.RIGHT, part, deformation);
		}
		
		return LayerDefinition.create(mesh, 64, 64);
	}
	
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
	{
		matrixStackIn.pushPose();
			if(this.scuttle)
	    		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90F));
			
			this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			
			float bigScale = 1.2F;
			matrixStackIn.pushPose();
				if(this.bigLeft)
				{
					matrixStackIn.scale(bigScale, bigScale, bigScale);
		    		matrixStackIn.translate(-0.05F, -0.15F, 0.05F);
				}
				this.leftClaw.getClaw().render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			matrixStackIn.popPose();
			matrixStackIn.pushPose();
				if(this.bigRight)
				{
					matrixStackIn.scale(bigScale, bigScale, bigScale);
		    		matrixStackIn.translate(0.05F, -0.15F, 0.05F);
				}
				this.rightClaw.getClaw().render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			matrixStackIn.popPose();
			
			for(LegHandler leg : leftLegs)
				leg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
			
			for(LegHandler leg : rightLegs)
				leg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.popPose();
	}
	
	public void setupAnim(AbstractCrab entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		this.bigLeft = entityIn.hasBigLeftClaw();
		this.bigRight = entityIn.hasBigRightClaw();
		this.scuttle = entityIn.shouldScuttle();
		
		if(entityIn.isPartying())
		{
			float wiggle = (float)Math.sin(ageInTicks);
			this.body.x = wiggle;
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
			this.body.x = 0F;
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
    		leftLegs.get(i).setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    		rightLegs.get(i).setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	}
    	
    	rightClaw.swingProgress = this.attackTime;
    	rightClaw.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	
    	leftClaw.swingProgress = this.attackTime;
    	leftClaw.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
	}
    
    public class ClawHandler
    {
    	public float swingProgress;
    	
    	private final EnumLimbPosition side;
    	private final boolean isLeft;
    	
    	ModelPart theArm;
    	ModelPart theClaw;
    	
    	private final float defaultRotationX;
    	private float offsetRotationX = 0F;
    	
    	public static void addClaw(EnumLimbPosition side, PartDefinition theModel, CubeDeformation deformation)
    	{
    		boolean isLeft = side == EnumLimbPosition.LEFT;
    		float defaultRotationX = 8F * (isLeft ? -1F : 1F);
    		
    		PartDefinition theArm = theModel.addOrReplaceChild((isLeft ? "left_claw" : "right_claw"), CubeListBuilder.create(), PartPose.offset(defaultRotationX, 13.5F, -6F));
    			theArm.addOrReplaceChild("base", CubeListBuilder.create().texOffs(16, 36).addBox(-1.5F, -1F, -1.5F, 3, 5, 3), PartPose.ZERO);
    		
    		CubeListBuilder clawCubes = CubeListBuilder.create();
    		if(isLeft)
    			clawCubes.mirror();
    		clawCubes.texOffs(28, 36).addBox(-2F, 0F, -2F, 4, 6, 4, deformation.extend(1F));
    		clawCubes.texOffs(28, 46).addBox(2F * (isLeft ? 1F : -1.6F), -0.5F, -1F, 1, 3, 2, deformation.extend(0.75F));
    			theArm.addOrReplaceChild("claw", clawCubes, PartPose.offset(0F, 4F, 0F));
    	}
    	
    	public ClawHandler(EnumLimbPosition side, ModelPart armIn)
    	{
    		this.side = side;
    		this.isLeft = side == EnumLimbPosition.LEFT;
    		this.theArm = armIn;
    		this.theClaw = armIn.getChild("claw");
    		this.defaultRotationX = 8F * (this.isLeft ? -1F : 1F);
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
    	public void setupAnim(AbstractCrab entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
        {
    		this.theArm.x = defaultRotationX + offsetRotationX;
            if(swingProgress > 0.0F && swingSideMatches(this.getMainHand(entityIn)))
        	{
            	theClaw.zRot = 0F;
            	
            	theArm.xRot = -ModelUtils.degree90;
            	theArm.yRot = 0F;
            	theArm.zRot = 0F;
            	
                float f1 = 1.0F - swingProgress;
                f1 = f1 * f1;
                f1 = f1 * f1;
                f1 = 1.0F - f1;
                float f2 = Mth.sin(f1 * (float)Math.PI);
                float f3 = Mth.sin(swingProgress * (float)Math.PI) * 0.7F * 0.75F;
                theArm.yRot = (float)((double)theArm.yRot - ((double)f2 * 1.2D + (double)f3)) * (isLeft ? -1F : 1F);
                theArm.zRot += Mth.sin(swingProgress * (float)Math.PI) * -0.4F;
        	}
            else
            {
            	theClaw.zRot = ModelUtils.degree10 * 4F * (isLeft ? -1F : 1F);
            	
            	theArm.xRot = -ModelUtils.degree180 * 0.75F;
            	theArm.xRot += (float)Math.cos(entityIn.tickCount / 20F) * ModelUtils.degree10 * 0.5F;
            	theArm.yRot = ModelUtils.degree90 * 0.5F * (isLeft ? 1F : -1F);
            	theArm.zRot = this.offsetRotationX * 0.5F;
            }
        }
    	
    	public boolean swingSideMatches(HumanoidArm handIn)
    	{
    		if(handIn == HumanoidArm.LEFT && this.side == EnumLimbPosition.LEFT){ return true; }
    		if(handIn == HumanoidArm.RIGHT && this.side == EnumLimbPosition.RIGHT){ return true; }
    		return false;
    	}
    	
        protected HumanoidArm getMainHand(Entity entityIn)
        {
            if (entityIn instanceof LivingEntity)
            {
                LivingEntity LivingEntity = (LivingEntity)entityIn;
                HumanoidArm HumanoidArm = LivingEntity.getMainArm();
                return LivingEntity.swingingArm == InteractionHand.MAIN_HAND ? HumanoidArm : HumanoidArm.getOpposite();
            }
            else
                return HumanoidArm.RIGHT;
        }
        
        public ModelPart getClaw(){ return this.theArm; }
    }
    
    public class LegHandler
    {
    	private final int index;
    	private final EnumLimbPosition side;
    	private final boolean isLeft;
    	
    	private ModelPart theLegUpper;
    	private ModelPart theLegLower;
    	
    	private final float defaultRotationX;
    	private float offsetRotationX = 0F;
    	
    	public static void addLeg(int index, EnumLimbPosition side, PartDefinition theModel, CubeDeformation deformation)
    	{
    		boolean isLeft = side == EnumLimbPosition.LEFT;
    		
    		float pol = (isLeft ? 1F : -1F);
			float defaultRotationX = (legOffset + (index%2 == 0 ? 1.5F : 0F)) * pol;
			
			PartDefinition legUpper = theModel.addOrReplaceChild("leg_"+index+"_"+side.name().toLowerCase(), CubeListBuilder.create(), PartPose.offsetAndRotation(defaultRotationX, 18F, index * 5.2F, 0F, -ModelUtils.toRadians(10D) * index * pol + (isLeft ? ModelUtils.toRadians(180D) : 0F), -ModelUtils.degree10 * pol));
				legUpper.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(0, 36).addBox(0-1.5F, 0F, -1.5F, 3, 6, 3), PartPose.rotation(0F, 0F, ModelUtils.toRadians(-90D)));
			
			PartDefinition legLower = legUpper.addOrReplaceChild("lower", CubeListBuilder.create(), PartPose.offsetAndRotation(5F, 0F, 0F, 0F, 0F, ModelUtils.degree90 / 1.5F));
				legLower.addOrReplaceChild("leg", CubeListBuilder.create().texOffs(0, 45).addBox(-1F, 0F, -1F, 2, 8, 2, deformation.extend(0.2F)), PartPose.rotation(0F, 0F, ModelUtils.toRadians(-90D)));
    	}
    	
    	public LegHandler(int index, EnumLimbPosition side, ModelPart legUpper)
    	{
    		this.index = index;
    		this.side = side;
    		this.isLeft = this.side == EnumLimbPosition.LEFT;
    		
    		float pol = (isLeft ? 1F : -1F);
			this.defaultRotationX = (legOffset + (index%2 == 0 ? 1.5F : 0F)) * pol;
    		
			this.theLegUpper = legUpper;
			this.theLegLower = legUpper.getChild("lower");
    	}
    	
    	public void setOffsetX(float par1Float){ this.offsetRotationX = par1Float; }
        
        /**
         * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
         * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
         * "far" arms and legs can swing at most.
         */
    	public void setupAnim(AbstractCrab entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
        {
        	float legBase = Mth.cos(limbSwing * 1.5F + (float)Math.PI) * 2F * limbSwingAmount;
        	boolean limbPolarity = (isLeft ? index%2 == 0 : index%2 != 0);
        	
    		this.theLegUpper.x = defaultRotationX + offsetRotationX;
        	
    		theLegUpper.zRot = Math.min(-ModelUtils.degree10, (legBase * (limbPolarity ? 1 : -1)) - ModelUtils.degree10) * (isLeft ? 1F : -1F);
    		theLegUpper.zRot += offsetRotationX * ModelUtils.toRadians(22.5D);
    		
    		theLegUpper.yRot = !isLeft ? ModelUtils.degree180 : 0F;
    		theLegUpper.yRot += (Mth.cos(limbSwing * 0.66682F + (float)Math.PI) * 0.8F * limbSwingAmount) * (limbPolarity ? 1F : -1F);
    		
    		theLegLower.zRot = ModelUtils.degree90 / 1.5F;
    		theLegLower.zRot -= ((((isLeft ? 1F : -1F )*ModelUtils.degree10) + theLegUpper.zRot) * 1F) * (isLeft ? 1F : -1F);
        }

    	public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    	{
    		this.theLegUpper.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    	}
    }
}
