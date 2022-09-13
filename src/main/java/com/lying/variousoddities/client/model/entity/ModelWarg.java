package com.lying.variousoddities.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.AbstractGoblinWolf.Genetics;
import com.lying.variousoddities.entity.mount.EntityWarg;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.ColorableAgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModelWarg extends ColorableAgeableListModel<EntityWarg>
{
    public ModelPart head;
    public ModelPart earLeft, earRight;
    public ModelPart muzzle;
    public ModelPart jaw;
    public ModelPart tongue;
    
    public ModelPart body;
    public ModelPart mane;
    public ModelPart mane2;
    
    public RearLegHandler legRearRight;
    public RearLegHandler legRearLeft;
    public ModelPart legFrontRight;
    public ModelPart legFrontLeft;
    
    public ModelPart tail;
    
	private final float JAW_RANGE = ModelUtils.toRadians(15D);
	private final float TONGUE_GAP = ModelUtils.toRadians(3D);
	private final float LEG_SPACE = 2.5F;
	private float scaleFactor = 0F;
    
    public ModelWarg(ModelPart partsIn)
    {
        this.head = partsIn.getChild("head");
        this.earRight = this.head.getChild("right_ear");
        this.earLeft = this.head.getChild("left_ear");
        
        this.muzzle = this.head.getChild("muzzle");
        this.jaw = this.muzzle.getChild("jaw");
		this.tongue = this.jaw.getChild("tongue");
        
        this.body = partsIn.getChild("body");
        
        this.mane = partsIn.getChild("mane");
    	this.mane2 = this.mane.getChild("child");
        
        this.legFrontRight = partsIn.getChild("right_front_leg");
        this.legFrontLeft = partsIn.getChild("left_front_leg");
        
        this.legRearRight = new RearLegHandler(partsIn.getChild("right_hind_leg"));
        this.legRearLeft = new RearLegHandler(partsIn.getChild("left_rear_leg"));
        
        this.tail = partsIn.getChild("tail");
    }
	
	public static LayerDefinition createBodyLayer(float scaleIn, CubeDeformation deformation)
	{
		MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0F);
		PartDefinition part = mesh.getRoot();
		
        PartDefinition head = part.addOrReplaceChild("head", CubeListBuilder.create()
        	.texOffs(0, 0).addBox(-2.5F, -2.0F, -3.0F, 5, 5, 5, deformation.extend(scaleIn))
        	.texOffs(20, 0).addBox(-2.5F, -2.0F, -3.0F, 5, 5, 5, deformation.extend(scaleIn + 0.25F))
        	.texOffs(14, 10).addBox(-1.5F, 2.0F, -6.0F, 3, 1, 4, deformation.extend(scaleIn + 0.01F)), PartPose.offset(0.0F, 14.0F, -7.5F));
        float earSpace = 2.75F;
    	// Right ear
    	head.addOrReplaceChild("right_ear", CubeListBuilder.create()
    		.texOffs(28, 10).addBox(-1.0F, -1.0F, -0.5F, 2, 3, 1, deformation.extend(scaleIn)), PartPose.offsetAndRotation(-earSpace, -2.5F, 0.5F, ModelUtils.toRadians(-20D), ModelUtils.toRadians(20D), ModelUtils.toRadians(-30D)));
    	// Left ear
    	head.addOrReplaceChild("left_ear", CubeListBuilder.create().mirror()
    		.texOffs(28, 14).addBox(-1.0F, -1.0F, -0.5F, 2, 3, 1, deformation.extend(scaleIn)), PartPose.offsetAndRotation(earSpace, -2.25F, 0.5F, ModelUtils.toRadians(-20D), ModelUtils.toRadians(-20D), ModelUtils.toRadians(30D)));
        
        PartDefinition muzzle = head.addOrReplaceChild("muzzle", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5F, 0.0F, -6.0F, 3, 2, 4, deformation.extend(scaleIn)), PartPose.ZERO);  
        PartDefinition jaw = muzzle.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 16).addBox(-1.5F, -0.5F, -4, 3, 1, 3, deformation.extend(scaleIn)), PartPose.offset(0F, 2.5F, -2F));
        	jaw.addOrReplaceChild("tongue", CubeListBuilder.create().texOffs(16, 0).addBox(-2F, -0.7F, -3.75F, 1, 2, 2, deformation.extend(scaleIn - 0.25F)), PartPose.rotation(0F, 0F, ModelUtils.toRadians(10D)));
        
        part.addOrReplaceChild("body", CubeListBuilder.create()
	        .texOffs(0, 20).addBox(-3.0F, 2.5F, -2F, 6, 4, 5, deformation.extend(scaleIn))
	        .texOffs(0, 29).addBox(-2.5F, -1F, -1.5F, 5, 4, 4, deformation.extend(scaleIn)), PartPose.offset(0.0F, 14.0F, 2.0F));
        
        PartDefinition mane = part.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(22, 20).addBox(-3.0F, -3.0F, -3F, 8, 5, 8, deformation.extend(scaleIn)), PartPose.offset(-1.0F, 14.0F, 2F));
        	mane.addOrReplaceChild("child", CubeListBuilder.create().texOffs(22, 33).addBox(-2.0F, -1.0F, -7F, 6, 2, 7, deformation.extend(scaleIn)), PartPose.offset(0F, 3F, 4F));
        
        part.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
	        .texOffs(20, 42).addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, deformation.extend(scaleIn))
	        .texOffs(20, 52).addBox(-1.0F, 4.0F, -1.0F, 2, 4, 2, deformation.extend(scaleIn + 0.25F)), PartPose.offset(-3F, 16F, -4F));
        
        part.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
	        .texOffs(0, 42).addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, deformation.extend(scaleIn))
	        .texOffs(0, 52).addBox(-1.0F, 4.0F, -1.0F, 2, 4, 2, deformation.extend(scaleIn + 0.25F)), PartPose.offset(1F, 16F, -4F));
        
        RearLegHandler.createRearLeg("right_hind_leg", part, scaleIn, deformation, 20, 58, -2.5F);
        RearLegHandler.createRearLeg("left_hind_leg", part, scaleIn, deformation, 0, 58, 0.5F);
        
        part.addOrReplaceChild("tail", CubeListBuilder.create()
              .texOffs(12, 42).addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2, deformation.extend(scaleIn))
              .texOffs(12, 50).addBox(-1.0F, 3.0F, -1.0F, 2, 4, 2, deformation.extend(scaleIn + 0.2F)), PartPose.offset(0F, 12.5F, 8F));
		
		return LayerDefinition.create(mesh, 64, 128);
	}
    
	protected Iterable<ModelPart> bodyParts()
	{
		return ImmutableList.of(this.body, this.mane, this.legFrontLeft, this.legFrontRight, this.legRearLeft.upperLeg, this.legRearRight.upperLeg, this.tail);
	}
	
	protected Iterable<ModelPart> headParts()
	{
		return ImmutableList.of(this.head);
	}
	
	public void prepareMobModel(EntityWarg entityIn, float limbSwing, float limbSwingAmount, float partialTickTime)
	{
		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTickTime);
		this.scaleFactor = partialTickTime;
		
        if(entityIn.getTarget() != null) tail.yRot = 0.0F;
        else tail.yRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        
        Genetics genetics = entityIn.getGenetics();
        this.earRight.xRot = genetics.gene(0) ? ModelUtils.toRadians(20D) : -ModelUtils.toRadians(20D);
        this.earLeft.xRot = genetics.gene(1) ? ModelUtils.toRadians(20D) : -ModelUtils.toRadians(20D);
        this.muzzle.z = genetics.gene(2) ? 1F : 0F;
        this.tongue.visible = genetics.gene(3);
        
        float frontLegSpace = LEG_SPACE;
        float rearLegSpace = LEG_SPACE * 0.6F + 1F;
        if(entityIn.isSleeping())
        {
            mane.setPos(-1.0F, 16.0F, -3.0F);
            mane.xRot = ((float)Math.PI * 2F / 5F);
            mane.yRot = 0.0F;
            
            mane2.xRot = -ModelUtils.toRadians(12.5D);
            
            body.setPos(0.0F, 18.0F, 0.0F);
            body.xRot = ((float)Math.PI / 4F);
            
            tail.setPos(0.0F, 21.0F, 6.0F);
            tail.yRot = (float)Math.cos(entityIn.tickCount / 10F) * (float)Math.toRadians(25D);
            
            legRearRight.setPos(-rearLegSpace, 22.0F, 2.0F);
            legRearRight.xRot(ModelUtils.toRadians(-115D));
            legRearLeft.setPos(rearLegSpace, 22.0F, 2.0F);
            legRearLeft.xRot(ModelUtils.toRadians(-115D));
            
            legFrontRight.setPos(-frontLegSpace, 17.0F, -4.0F);
            legFrontRight.xRot = 5.811947F;
            legFrontLeft.setPos(frontLegSpace, 17.0F, -4.0F);
            legFrontLeft.xRot = 5.811947F;
        }
        else
        {
            body.setPos(0.0F, 14.0F, 2.0F);
            body.xRot = ((float)Math.PI / 2F);
            mane.setPos(-1.0F, 15F, -3.0F);
            mane.xRot = body.xRot;
            
            mane2.xRot = 0F;
            
            tail.setPos(0.0F, 12.5F, 8.0F);
            
            legRearRight.setPos(-rearLegSpace, 14.0F, 6.5F);
            legRearRight.xRot(Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount);
            legRearLeft.setPos(rearLegSpace, 14.0F, 6.5F);
            legRearLeft.xRot(Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount);
            
            legFrontRight.setPos(-frontLegSpace, 16.0F, -4.0F);
            legFrontRight.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
            legFrontLeft.setPos(frontLegSpace, 16.0F, -4.0F);
            legFrontLeft.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        }
        
        head.y = 14.0F + limbSwingAmount / 2F;
        
        head.zRot = entityIn.getInterestedAngle(partialTickTime) + entityIn.getShakeAngle(partialTickTime, 0.0F);
        mane.zRot = entityIn.getShakeAngle(partialTickTime, -0.08F);
        body.zRot = entityIn.getShakeAngle(partialTickTime, -0.16F);
        tail.zRot = entityIn.getShakeAngle(partialTickTime, -0.2F);
	}
	
	public void setupAnim(EntityWarg entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
        head.xRot = headPitch * 0.017453292F;
        head.yRot = netHeadYaw * 0.017453292F;
        
        boolean tongue = entityIn.getGenetics().gene(3);
        jaw.xRot = (tongue ? TONGUE_GAP : 0F) + entityIn.getJawState(scaleFactor) * (JAW_RANGE - (tongue ? TONGUE_GAP : 0F));
        jaw.z = -2F + jaw.xRot;
        
        tail.xRot = entityIn.getTailRotation() + (entityIn.isSleeping() ? (float)Math.toRadians(45D) : 0F);
	}
    
    public class RearLegHandler
    {
    	public ModelPart upperLeg;
    	private ModelPart lowerLeg;
    	
    	public RearLegHandler(ModelPart upperLegIn)
    	{
    		this.upperLeg = upperLegIn;
    		this.lowerLeg = upperLegIn.getChild("lower_leg");
    	}
    	
    	public static void createRearLeg(String name, PartDefinition base, float scaleIn, CubeDeformation deformation, int textureX, int textureY, float xOffset)
    	{
			PartDefinition upperLeg = base.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.offset(xOffset, 16F, 7F));
        	PartDefinition thigh = upperLeg.addOrReplaceChild("thigh", CubeListBuilder.create(), PartPose.rotation(ModelUtils.toRadians(35D), 0F, 0F));
        		thigh.addOrReplaceChild("box_rot", CubeListBuilder.create().texOffs(textureX, textureY).addBox(-1.5F, -6F, -2F, 3, 7, 3, deformation.extend(scaleIn - 0.1F)), PartPose.offsetAndRotation(0F, -1F, 0F, ModelUtils.degree90, 0F, 0F));
	        
	        PartDefinition lowerLeg = upperLeg.addOrReplaceChild("lower_leg", CubeListBuilder.create(), PartPose.offset(0F, 3.5F, -4F));
        	PartDefinition foot = lowerLeg.addOrReplaceChild("foot", CubeListBuilder.create().texOffs(textureX, textureY + 17).addBox(-1F, 3F, -1.5F, 2, 2, 4, deformation.extend(scaleIn)), PartPose.offset(0F, 0F, -1.5F));
	        PartDefinition bridge = foot.addOrReplaceChild("bridge", CubeListBuilder.create(), PartPose.rotation(ModelUtils.toRadians(70D), 0F, 0F));
		    	bridge.addOrReplaceChild("box_rot", CubeListBuilder.create().texOffs(textureX, textureY + 10).addBox(-1F, -3.75F, 1.5F, 2, 5, 2, deformation.extend(scaleIn - 0.1F)), PartPose.offsetAndRotation(0F, 5F, 0F, ModelUtils.degree90, 0F, 0F));
    	}
    	
    	public void setPos(float x, float y, float z)
    	{
    		upperLeg.setPos(x, y, z);
    	}
    	
    	public void xRot(float x)
    	{
    		x += ModelUtils.degree10 * 4;
    		upperLeg.xRot = x;
    		
    		float lowerLegX = Math.max(-ModelUtils.degree10 * 4, -x);
    		lowerLeg.xRot = lowerLegX;
    	}
    	
    	public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    	{
    		this.upperLeg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    	}
    }
}
