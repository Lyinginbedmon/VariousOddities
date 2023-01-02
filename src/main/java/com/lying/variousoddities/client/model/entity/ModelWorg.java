package com.lying.variousoddities.client.model.entity;

import java.util.Arrays;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.AbstractGoblinWolf.Genetics;
import com.lying.variousoddities.entity.passive.EntityWorg;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.ColorableAgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class ModelWorg extends ColorableAgeableListModel<EntityWorg> implements ArmedModel
{
    public ModelPart head;
    public ModelPart earLeft, earRight;
    public ModelPart muzzle;
    public ModelPart jaw;
    public ModelPart tongue;
    
    public ModelPart body;
    public ModelPart mane;
    
    public ModelPart legRearRight;
    public ModelPart legRearLeft;
    public ModelPart legFrontRight;
    public ModelPart legFrontLeft;
    
    public ModelPart tail;
    
	private final float JAW_RANGE = ModelUtils.toRadians(15D);
	private final float TONGUE_GAP = ModelUtils.toRadians(3D);
	private final float LEG_SPACE = 2.5F;
	
	private float scaleFactor = 0F;
	
	public ModelWorg(ModelPart partsIn)
	{
        this.head = partsIn.getChild("head");
        this.muzzle = this.head.getChild("muzzle");
        this.earRight = this.head.getChild("right_ear");
        this.earLeft = this.head.getChild("left_ear");
        this.jaw = this.muzzle.getChild("jaw");
		this.tongue = this.jaw.getChild("tongue");
        
        this.body = partsIn.getChild("body");
        this.mane = partsIn.getChild("mane");
        
        this.legFrontRight = partsIn.getChild("front_right_leg");
        this.legFrontLeft = partsIn.getChild("front_left_leg");
        this.legRearRight = partsIn.getChild("rear_right_leg");
        this.legRearLeft = partsIn.getChild("rear_left_leg");
        
        this.tail = partsIn.getChild("tail");
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		PartDefinition head = part.addOrReplaceChild("head", CubeListBuilder.create()
	        .texOffs(0, 0).addBox(-3F, -3F, -2F, 6, 6, 4)
	        .texOffs(20, 0).addBox(-3F, -3F, -2F, 6, 6, 4, deformation.extend(0.25F))
	        .texOffs(14, 10).addBox(-1.5F, 2F, -5F, 3, 1, 4, deformation.extend(0.01F)), PartPose.offset(0F, 14F, -7.5F));
			head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(28, 10).addBox(-1F, -1F, -0.5F, 2, 2, 1), PartPose.offsetAndRotation(-2.75F, -3.25F, 0.5F, ModelUtils.toRadians(-20D), 0F, ModelUtils.toRadians(-30D)));
			head.addOrReplaceChild("left_ear", CubeListBuilder.create().mirror().texOffs(28, 13).addBox(-1F, -1F, -0.5F, 2, 2, 1), PartPose.offsetAndRotation(2.75F, -3.25F, 0.5F, ModelUtils.toRadians(-20D), 0F, ModelUtils.toRadians(30D)));
			PartDefinition muzzle = head.addOrReplaceChild("muzzle", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5F, 0F, -5F, 3, 2, 4), PartPose.ZERO);
			PartDefinition jaw = muzzle.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 16).addBox(-1.5F, -0.5F, -4, 3, 1, 3), PartPose.offset(0F, 2.5F, -1F));
			jaw.addOrReplaceChild("tongue", CubeListBuilder.create().texOffs(16, 0).addBox(-2F, -0.7F, -3.75F, 1, 2, 2, deformation.extend(-0.25F)), PartPose.rotation(0F, 0F, ModelUtils.degree10));
		
		part.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 20).addBox(-3F, -2F, -3F, 6, 9, 7), PartPose.offset(0F, 14F, 2F));
        part.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(26, 20).addBox(-3F, -3F, -3F, 8, 7, 9), PartPose.offset(-1F, 14F, 2F));
        
        part.addOrReplaceChild("front_right_leg", CubeListBuilder.create().texOffs(18, 36).addBox(-1F, 0F, -1F, 2, 8, 2), PartPose.offset(-2.5F, 16F, -4F));
        part.addOrReplaceChild("front_left_leg", CubeListBuilder.create().texOffs(0, 36).addBox(-1F, 0F, -1F, 2, 8, 2), PartPose.offset(0.5F, 16F, -4F));
        part.addOrReplaceChild("rear_right_leg", CubeListBuilder.create().texOffs(18, 46).addBox(-1F, 0F, -1F, 2, 8, 2), PartPose.offset(-2.5F, 16F, 7F));
        part.addOrReplaceChild("rear_left_leg", CubeListBuilder.create().texOffs(0, 46).addBox(-1F, 0F, -1F, 2, 8, 2), PartPose.offset(0.5F, 16F, 7F));
        
        part.addOrReplaceChild("tail", CubeListBuilder.create()
            .texOffs(9, 36).addBox(-1F, 0F, -1F, 2, 6, 2)
            .texOffs(9, 46).addBox(-1F, 3F, -1F, 2, 4, 2, deformation.extend(0.2F)), PartPose.offset(0F, 12F, 8F));
		
		return LayerDefinition.create(mesh, 64, 64);
	}
	
	protected Iterable<ModelPart> bodyParts()
	{
		return Arrays.asList(this.body, this.mane, this.legFrontLeft, this.legFrontRight, this.legRearLeft, this.legRearRight, this.tail);
	}
	
	protected Iterable<ModelPart> headParts()
	{
		return Arrays.asList(this.head);
	}
	
	public void prepareMobModel(EntityWorg entityIn, float limbSwing, float limbSwingAmount, float partialTickTime)
	{
		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTickTime);
		this.scaleFactor = partialTickTime;
		
        if(entityIn.getTarget() != null) tail.yRot = 0F;
        else tail.yRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        
        Genetics genetics = entityIn.getGenetics();
        this.earRight.xRot = genetics.gene(0) ? ModelUtils.toRadians(20D) : -ModelUtils.toRadians(20D);
        this.earLeft.xRot = genetics.gene(1) ? ModelUtils.toRadians(20D) : -ModelUtils.toRadians(20D);
        this.muzzle.z = genetics.gene(2) ? 1F : 0F;
        this.tongue.visible = genetics.gene(3);
        
        float frontLegSpace = LEG_SPACE;
        float rearLegSpace = LEG_SPACE * 0.6F;
        if(entityIn.isSleeping())
        {
        	head.y = 13F;
        	head.z = -6.5F;
        	
            mane.setPos(-1F, 16F, -3F);
            mane.xRot = ((float)Math.PI * 2F / 5F);
            mane.yRot = 0F;
            body.setPos(0F, 18F, 0F);
            body.xRot = ((float)Math.PI / 4F);
            
            tail.setPos(0F, 21F, 6F);
            tail.yRot = (float)Math.cos(entityIn.tickCount / 10F) * (float)Math.toRadians(25D);
            
            legRearRight.setPos(-rearLegSpace, 22F, 2F);
            legRearRight.xRot = ((float)Math.PI * 3F / 2F);
            legRearLeft.setPos(rearLegSpace, 22F, 2F);
            legRearLeft.xRot = ((float)Math.PI * 3F / 2F);
            
            legFrontRight.setPos(-frontLegSpace, 17F, -4F);
            legFrontRight.xRot = 5.811947F;
            legFrontLeft.setPos(frontLegSpace, 17F, -4F);
            legFrontLeft.xRot = 5.811947F;
        }
        else
        {
        	head.y = 14F;
        	head.z = -7.5F;
        	
            body.setPos(0F, 14F, 2F);
            body.xRot = ((float)Math.PI / 2F);
            mane.setPos(-1F, 15F, -3F);
            mane.xRot = body.xRot;
            tail.setPos(0F, 12F, 8F);
            
            legRearRight.setPos(-rearLegSpace, 16F, 7F);
            legRearRight.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
            legRearLeft.setPos(rearLegSpace, 16F, 7F);
            legRearLeft.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
            
            legFrontRight.setPos(-frontLegSpace, 16F, -4F);
            legFrontRight.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
            legFrontLeft.setPos(frontLegSpace, 16F, -4F);
            legFrontLeft.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        }
        
        head.zRot = entityIn.getInterestedAngle(partialTickTime) + entityIn.getShakeAngle(partialTickTime, 0F);
        mane.zRot = entityIn.getShakeAngle(partialTickTime, -0.08F);
        body.zRot = entityIn.getShakeAngle(partialTickTime, -0.16F);
        tail.zRot = entityIn.getShakeAngle(partialTickTime, -0.2F);
	}
	
    public void setupAnim(EntityWorg entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
        head.xRot = headPitch * 0.017453292F;
        head.yRot = netHeadYaw * 0.017453292F;
        
        boolean tongue = entityIn.getGenetics().gene(3);
        jaw.xRot = (tongue ? TONGUE_GAP : 0F) + entityIn.getJawState(scaleFactor) * (JAW_RANGE - (tongue ? TONGUE_GAP : 0F));
        jaw.z = -1F + jaw.xRot;
        
        tail.xRot = entityIn.getTailRotation() + (entityIn.isSleeping() ? (float)Math.toRadians(45D) : 0F);
	}
    
	public void translateToHand(HumanoidArm sideIn, PoseStack matrixStackIn)
	{
		this.head.translateAndRotate(matrixStackIn);
		this.jaw.translateAndRotate(matrixStackIn);
	}
}
