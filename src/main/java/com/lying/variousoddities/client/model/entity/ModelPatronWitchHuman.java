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

public class ModelPatronWitchHuman extends HumanoidModel<EntityPatronWitch> implements IPonytailModel
{
	public ModelPart leftSleeve, rightSleeve;
	public ModelPart tabardFront, tabardRear;
	
	public ModelPart ponytail;
	public ModelPart ponytailAnchor;
	public ModelPart ponytailAnchor2;
	
	public ModelPart smileHead;
	public ModelPart jawHead;
	public ModelPart jawBase;
	public ModelPart jawLeft, jawRight;
	
	public ModelPatronWitchHuman(ModelPart partsIn)
	{
		super(partsIn);
		
		this.ponytailAnchor = partsIn.getChild("ponytail_anchor_A");
		this.ponytailAnchor2 = this.ponytailAnchor.getChild("ponytail_anchor_B");
        this.ponytail = this.ponytailAnchor2.getChild("ponytail");
        
        this.smileHead = partsIn.getChild("head_smiling");
		this.jawHead = partsIn.getChild("head_jaw");
		this.jawBase = this.jawHead.getChild("jaw_base");
		this.jawLeft = this.jawBase.getChild("left_jaw");
		this.jawRight = this.jawBase.getChild("right_jaw");
        
		this.rightSleeve = this.rightArm.getChild("sleeve");
		this.leftSleeve = this.leftArm.getChild("sleeve");
        
        this.tabardFront = partsIn.getChild("tabard_front");
        this.tabardRear = partsIn.getChild("tabard_rear");
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0F);
		PartDefinition part = mesh.getRoot();
		
		PartDefinition ponytailAnchor = part.addOrReplaceChild("ponytail_anchor_A", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 1, 1), PartPose.ZERO);
		PartDefinition ponytailAnchor2 = ponytailAnchor.addOrReplaceChild("ponytail_anchor_B", CubeListBuilder.create().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 1, 1), PartPose.offset(0, -5, 4));
			ponytailAnchor2.addOrReplaceChild("ponytail", CubeListBuilder.create().texOffs(64, 32).addBox(-5F, 0F, 0F, 10, 16, 1), PartPose.ZERO);
		
		addEars(part.addOrReplaceChild("head", CubeListBuilder.create()
			.texOffs(0, 0).addBox(-4F, -8F, -4F, 8, 8, 8)
			.texOffs(32, 0).addBox(-4F, -8F, -4F, 8, 8, 8, deformation.extend(0.5F)), PartPose.ZERO));
		
		addEars(part.addOrReplaceChild("head_smiling", CubeListBuilder.create()
	        .texOffs(16, 48).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8)
	        .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, deformation.extend(0.5F)), PartPose.ZERO));
		
		addEars(part.addOrReplaceChild("head_jaw", CubeListBuilder.create()
	        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 6, 8)
	        .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, deformation.extend(0.5F))
	        .texOffs(36, 32).addBox(-4F, -2F, 2F, 8, 2, 2), PartPose.ZERO)
				.addOrReplaceChild("jaw_base", CubeListBuilder.create(), PartPose.offset(0F, -2F, 4F))
					.addOrReplaceChild("right_jaw", CubeListBuilder.create().texOffs(16, 32).addBox(-4F, 0F, -8F, 4, 2, 6), PartPose.ZERO)
					.addOrReplaceChild("left_jaw", CubeListBuilder.create().mirror().texOffs(16, 32).addBox(0F, 0F, -8F, 4, 2, 6), PartPose.ZERO));
		
		part.addOrReplaceChild("body", CubeListBuilder.create()
			.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4)
			.texOffs(16, 40).addBox(-4F, 0F, -2.2F, 8, 8, 0), PartPose.ZERO);
		
		PartDefinition rightArm = part.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4), PartPose.offset(-5F, 2F, 0F));
			rightArm.addOrReplaceChild("sleeve", CubeListBuilder.create().texOffs(0, 32).addBox(-3F, -4F, -2F, 4, 5, 4, deformation.extend(0.2F)), PartPose.offset(0F, 8F, 0F));
		
		PartDefinition leftArm = part.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4), PartPose.offset(5F, 2F, 0F));
			leftArm.addOrReplaceChild("sleeve", CubeListBuilder.create().mirror().texOffs(0, 32).addBox(-1F, -4F, -2F, 4, 5, 4, deformation.extend(0.2F)), PartPose.offset(0F, 8F, 0F));
		
		part.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4), PartPose.offset(-1.9F, 12F, 0F));
		part.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4), PartPose.offset(1.9F, 12F, 0F));
		
		part.addOrReplaceChild("tabard_front", CubeListBuilder.create().texOffs(36, 36).addBox(-4F, 0F, -2.2F, 8, 6, 0), PartPose.offset(0F, 12F, 0F));
		part.addOrReplaceChild("tabard_rear", CubeListBuilder.create().texOffs(36, 42).addBox(-4F, 0F, 2.2F, 8, 6, 0), PartPose.offset(0F, 12F, 0F));
		
		return LayerDefinition.create(mesh, 64, 64);
	}
    
    private static void addEars(PartDefinition head)
    {
    	head.addOrReplaceChild("ears", CubeListBuilder.create()
			.texOffs(24, 0).addBox(-4.5F, -6F, 0F, 1, 3, 2)
			.texOffs(30, 0).addBox(-5F, -8F, 2F, 1, 3, 1)
			.texOffs(24, 5).addBox(-5F, -7F, 1F, 1, 1, 1)
			.texOffs(24, 0).addBox(3.5F, -6F, 0F, 1, 3, 2)
			.texOffs(30, 0).addBox(4F, -8F, 2F, 1, 3, 1)
			.texOffs(24, 5).addBox(4F, -7F, 1F, 1, 1, 1), PartPose.ZERO);
    }
	
	protected Iterable<ModelPart> headParts()
	{
	   return ImmutableList.of(this.head, this.smileHead, this.jawHead);
	}
	
	protected Iterable<ModelPart> bodyParts()
	{
		return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.tabardFront, this.tabardRear);
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
    
    public ModelPart getHeadForWitch(EntityPatronWitch witch)
    {
    	if(witch.isJawOpen())
    		return witch.isJawSplit() ? this.jawHead : this.smileHead;
    	return this.head;
    }
    
    public void setupAnim(EntityPatronWitch entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
    	super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	
    	ponytailAnchor.copyFrom(head);
    	ponytail.xRot = Math.max(body.xRot - head.xRot, -0.259F);
    	
    	jawHead.copyFrom(head);
    	smileHead.copyFrom(head);
		
		this.tabardFront.xRot = Math.min(Math.min(this.leftLeg.xRot, this.rightLeg.xRot), ModelUtils.toRadians(-2D));
		this.tabardRear.xRot = Math.max(Math.max(this.leftLeg.xRot, this.rightLeg.xRot), ModelUtils.toRadians(2D));
		
		if(entityIn.isJawOpen())
		{
    		if(entityIn.isJawSplit())
    		{
    			setVisibleHead(this.jawHead);
        		float rot = entityIn.getJawState(0F);
    			
		    	this.jawBase.xRot = rot * ModelUtils.toRadians(10D);
		    	
		    	float jawAng = ModelUtils.toRadians(15D);
		    	this.jawLeft.yRot = -rot * jawAng;
		    	this.jawRight.yRot = rot * jawAng;
    		}
    		else
    			setVisibleHead(this.smileHead);
    	}
		else
			setVisibleHead(this.head);
    }
    
    public void setVisibleHead(ModelPart head)
    {
		this.head.visible = false;
		this.jawHead.visible = false;
		this.smileHead.visible = false;
		head.visible = true;
    }
}
