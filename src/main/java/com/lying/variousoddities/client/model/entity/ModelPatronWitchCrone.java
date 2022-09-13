package com.lying.variousoddities.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.wip.EntityPatronWitch;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class ModelPatronWitchCrone extends HumanoidModel<EntityPatronWitch> implements IPonytailModel
{
	private ModelPart skirting;
	
	public ModelPart ponytail;
	public ModelPart ponytailAnchor;
	public ModelPart ponytailAnchor2;
	
	public ModelPatronWitchCrone(ModelPart partsIn)
	{
		super(partsIn);
		
		this.ponytailAnchor = partsIn.getChild("ponytail_anchor_A");
		this.ponytailAnchor2 = this.ponytailAnchor.getChild("ponytail_anchor_B");
        this.ponytail = this.ponytailAnchor2.getChild("ponytail");
		
		this.skirting = this.body.getChild("skirting");
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0F);
		PartDefinition part = mesh.getRoot();
		
		PartDefinition ponytailAnchor = part.addOrReplaceChild("ponytail_anchor_A", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 1, 1), PartPose.ZERO);
		PartDefinition ponytailAnchor2 = ponytailAnchor.addOrReplaceChild("ponytail_anchor_B", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 1, 1), PartPose.offset(0, -5, 4));
			ponytailAnchor2.addOrReplaceChild("ponytail", CubeListBuilder.create().texOffs(64, 32).addBox(-5F, 0F, 0F, 10, 16, 1), PartPose.ZERO);
		
		part.addOrReplaceChild("head", CubeListBuilder.create()
			.texOffs(0, 0).addBox(-4F, -8F, -4F, 8, 8, 8)
			.texOffs(32, 0).addBox(-4F, -8F, -4F, 8, 8, 8, deformation.extend(0.5F)), PartPose.offset(0F, 0F, -3F));
		
		PartDefinition body = part.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 37).addBox(-4, 0, -4, 8, 9, 4, deformation.extend(0.5F)), PartPose.ZERO);
			body.addOrReplaceChild("child", CubeListBuilder.create().texOffs(16, 16).addBox(-4F, -9F, -2F, 8, 9, 4), PartPose.offsetAndRotation(0, 9, 0, ModelUtils.toRadians(16D), 0F, 0F));
			body.addOrReplaceChild("child_2", CubeListBuilder.create().texOffs(16, 29).addBox(-4, 0, -2, 8, 4, 4), PartPose.offset(0, 8, 0));
			body.addOrReplaceChild("skirting", CubeListBuilder.create().texOffs(16, 50).addBox(-4, 0, -2, 8, 9, 4, deformation.extend(0.5F)), PartPose.offset(0, 9, 0));
		
		part.addOrReplaceChild("right_arm", CubeListBuilder.create()
			.texOffs(40, 16).addBox(-2, -2, -2, 3, 12, 4)
			.texOffs(40, 32).addBox(-2, -2, -2, 3, 12, 4, deformation.extend(0.5F)), PartPose.offset(-5F, 2F, -2F));
		
		part.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror()
			.texOffs(40, 16).addBox(-1, -2, -2, 3, 12, 4)
			.texOffs(40, 32).addBox(-1, -2, -2, 3, 12, 4, deformation.extend(0.5F)), PartPose.offset(5F, 2F, -2F));
		
		part.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4), PartPose.offset(-1.9F, 12F, 0F));
        
		part.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4), PartPose.offset(1.9F, 12F, 0F));
		
		return LayerDefinition.create(mesh, 64, 64);
	}
	
	protected Iterable<ModelPart> headParts()
	{
	   return ImmutableList.of(this.head);
	}
	
	protected Iterable<ModelPart> bodyParts()
	{
		return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg);
	}

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    @SuppressWarnings("incomplete-switch")
    public void setupAnim(EntityPatronWitch entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        boolean isFlying = entityIn.getFallFlyingTicks() > 4;
        this.head.yRot = netHeadYaw * 0.017453292F;
        
        if(isFlying)
            this.head.xRot = -((float)Math.PI / 4F);
        else
            this.head.xRot = headPitch * 0.017453292F;

        this.body.yRot = 0.0F;
        this.rightArm.z = -2F;
        this.rightArm.x = -5.0F;
        this.leftArm.z = -2F;
        this.leftArm.x = 5.0F;
        float f = 1.0F;
        if(isFlying)
        {
            f = (float)entityIn.getDeltaMovement().lengthSqr();
            f = f / 0.2F;
            f = f * f * f;
        }
        
        if(f < 1.0F) f = 1.0F;
        
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F / f;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount / f;
        this.rightLeg.yRot = 0.0F;
        this.leftLeg.yRot = 0.0F;
        this.rightLeg.zRot = 0.0F;
        this.leftLeg.zRot = 0.0F;

        if(this.riding)
        {
            this.rightArm.xRot += -((float)Math.PI / 5F);
            this.leftArm.xRot += -((float)Math.PI / 5F);
            this.rightLeg.xRot = -1.4137167F;
            this.rightLeg.yRot = ((float)Math.PI / 10F);
            this.rightLeg.zRot = 0.07853982F;
            this.leftLeg.xRot = -1.4137167F;
            this.leftLeg.yRot = -((float)Math.PI / 10F);
            this.leftLeg.zRot = -0.07853982F;
        }
        
        this.rightArm.yRot = 0.0F;
        this.rightArm.zRot = 0.0F;

        switch(this.leftArmPose)
        {
            case EMPTY:
                this.leftArm.yRot = 0.0F;
                break;
            case BLOCK:
                this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.9424779F;
                this.leftArm.yRot = 0.5235988F;
                break;
            case ITEM:
                this.leftArm.xRot = this.leftArm.xRot * 0.5F - ((float)Math.PI / 10F);
                this.leftArm.yRot = 0.0F;
        }
        
        switch (this.rightArmPose)
        {
            case EMPTY:
                this.rightArm.yRot = 0.0F;
                break;
            case BLOCK:
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.9424779F;
                this.rightArm.yRot = -0.5235988F;
                break;
            case ITEM:
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - ((float)Math.PI / 10F);
                this.rightArm.yRot = 0.0F;
        }
        
        if(this.attackTime > 0.0F)
        {
            HumanoidArm enumhandside = entityIn.getMainArm();
            ModelPart modelrenderer = this.getArm(enumhandside);
            float f1 = this.attackTime;
            this.body.yRot = Mth.sin(Mth.sqrt(f1) * ((float)Math.PI * 2F)) * 0.2F;
            
            if (enumhandside == HumanoidArm.LEFT)
                this.body.yRot *= -1.0F;
            
            this.rightArm.z = Mth.sin(this.body.yRot) * 5.0F;
            this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0F;
            this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0F;
            this.leftArm.x = Mth.cos(this.body.yRot) * 5.0F;
            this.rightArm.yRot += this.body.yRot;
            this.leftArm.yRot += this.body.yRot;
            this.leftArm.xRot += this.body.yRot;
            f1 = 1.0F - this.attackTime;
            f1 = f1 * f1;
            f1 = f1 * f1;
            f1 = 1.0F - f1;
            float f2 = Mth.sin(f1 * (float)Math.PI);
            float f3 = Mth.sin(this.attackTime * (float)Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
            modelrenderer.xRot = (float)((double)modelrenderer.xRot - ((double)f2 * 1.2D + (double)f3));
            modelrenderer.yRot += this.body.yRot * 2.0F;
            modelrenderer.zRot += Mth.sin(this.attackTime * (float)Math.PI) * -0.4F;
        }
        
        if(this.crouching)
        {
            this.body.xRot = 0.5F;
            this.rightArm.xRot += 0.4F;
            this.leftArm.xRot += 0.4F;
            this.rightLeg.z = 4.0F;
            this.leftLeg.z = 4.0F;
            this.rightLeg.y = 9.0F;
            this.leftLeg.y = 9.0F;
            this.head.y = 1.0F;
        }
        else
        {
            this.body.xRot = 0.0F;
            this.rightLeg.z = 0.1F;
            this.leftLeg.z = 0.1F;
            this.rightLeg.y = 12.0F;
            this.leftLeg.y = 12.0F;
            this.head.y = 0.0F;
        }
        
        this.rightArm.zRot += Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot -= Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.rightArm.xRot += Mth.sin(ageInTicks * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(ageInTicks * 0.067F) * 0.05F;
        
        if (this.rightArmPose == ArmPose.BOW_AND_ARROW)
        {
            this.rightArm.yRot = -0.1F + this.head.yRot;
            this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
            this.rightArm.xRot = -((float)Math.PI / 2F) + this.head.xRot;
            this.leftArm.xRot = -((float)Math.PI / 2F) + this.head.xRot;
        }
        else if (this.leftArmPose == ArmPose.BOW_AND_ARROW)
        {
            this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
            this.leftArm.yRot = 0.1F + this.head.yRot;
            this.rightArm.xRot = -((float)Math.PI / 2F) + this.head.xRot;
            this.leftArm.xRot = -((float)Math.PI / 2F) + this.head.xRot;
        }
        
        ponytailAnchor.copyFrom(head);
        this.skirting.xRot = ModelUtils.toRadians(2D) + Math.max(this.leftLeg.xRot, this.rightLeg.xRot);
    	this.ponytail.xRot = Math.max(body.xRot - head.xRot, -0.259F);
    }
    
    public void setPonytailHeight(float par1Float)
    {
    	this.ponytailAnchor2.y = par1Float;
    }
    
    public void setPonytailRotation(float par1Float, float par2Float, boolean par3Bool)
    {
    	this.ponytail.y = Math.min(5.6F, par2Float / 15F) + (par3Bool ? 3.5F : 0F);
    }
    
    public void renderPonytail(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn)
    {
    	this.ponytailAnchor.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
    }
}
