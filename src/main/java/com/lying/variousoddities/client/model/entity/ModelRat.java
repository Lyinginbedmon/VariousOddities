package com.lying.variousoddities.client.model.entity;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.lying.variousoddities.client.model.EnumLimbPosition;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.AbstractRat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ModelRat<T extends AbstractRat> extends EntityModel<T>
{
	ModelPart head;
	ModelPart body;
	
	ModelLeg legLeftRear;
	ModelLeg legRightRear;
	ModelLeg legLeftFront;
	ModelLeg legRightFront;
	
	List<ModelPart> tail = Lists.newArrayList();
	
	public ModelRat(ModelPart partsIn)
	{
		head = partsIn.getChild("head");
		body = partsIn.getChild("body");
		
		ModelPart root = body.getChild("tail");
		while(root.hasChild("child"))
			tail.add(root = root.getChild("child"));
		
		legLeftRear = new ModelLeg(EnumLimbPosition.LEFT, EnumLimbPosition.REAR, partsIn.getChild("rear_left_leg"));
		legRightRear = new ModelLeg(EnumLimbPosition.RIGHT, EnumLimbPosition.REAR, partsIn.getChild("rear_right_leg"));
		legLeftFront = new ModelLeg(EnumLimbPosition.LEFT, EnumLimbPosition.FRONT, partsIn.getChild("front_left_leg"));
		legRightFront = new ModelLeg(EnumLimbPosition.RIGHT, EnumLimbPosition.FRONT, partsIn.getChild("front_right_leg"));
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		PartDefinition head = part.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 18.5F, -4.5F));
			head.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-2F, -6F, -2F, 4F, 6F, 4F), PartPose.rotation(ModelUtils.degree90, 0F, 0F));
			head.addOrReplaceChild("ear_left", CubeListBuilder.create().mirror().texOffs(16, 0).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, deformation.extend(0.2F)), PartPose.offsetAndRotation(2.5F * EnumLimbPosition.LEFT.getX(), 1.5F * EnumLimbPosition.UP.getY(), 1F * EnumLimbPosition.FRONT.getZ(), -ModelUtils.toRadians(15D), -ModelUtils.toRadians(30D), 0F));
			head.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(24, 0).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, deformation.extend(0.2F)), PartPose.offsetAndRotation(2.5F * EnumLimbPosition.RIGHT.getX(), 1.5F * EnumLimbPosition.UP.getY(), 1F * EnumLimbPosition.FRONT.getZ(), -ModelUtils.toRadians(15D), ModelUtils.toRadians(30D), 0F));
			head.addOrReplaceChild("whisker_left", CubeListBuilder.create().texOffs(16, 4).addBox(0F, 1.5F, 0F, 5F, 3F, 0F), PartPose.offsetAndRotation(2F * EnumLimbPosition.LEFT.getX(), 3F * EnumLimbPosition.UP.getY(), 5F * EnumLimbPosition.FRONT.getZ(), 0F, -ModelUtils.toRadians(40D), 0F));
			head.addOrReplaceChild("whisker_right", CubeListBuilder.create().mirror().texOffs(16, 7).addBox(-5F, 1.5F, 0F, 5F, 3F, 0F), PartPose.offsetAndRotation(2F * EnumLimbPosition.RIGHT.getX(), 3F * EnumLimbPosition.UP.getY(), 5F * EnumLimbPosition.FRONT.getZ(), 0F, ModelUtils.toRadians(40D), 0F));
		
		PartDefinition body = part.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 21F, 0F));
			body.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 10).addBox(-3F, -4F, -2.5F, 6F, 10F, 5F), PartPose.rotation(0F, ModelUtils.degree90, 0F));
			PartDefinition prev = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, 0F, 1.85F, ModelUtils.degree90, 0F, 0F));
			for(int i=0; i<3; i++)
				prev = prev.addOrReplaceChild("child", CubeListBuilder.create().texOffs(22, 10 + (4 * i)).addBox(-0.5F, 0F, -0.5F, 1, 3, 1, deformation.extend(0.2F-(0.01F * i))), PartPose.offset(0F, 3.3F, 0F));
		
		ModelLeg.addLeg(EnumLimbPosition.LEFT, EnumLimbPosition.REAR, part);
		ModelLeg.addLeg(EnumLimbPosition.RIGHT, EnumLimbPosition.REAR, part);
		ModelLeg.addLeg(EnumLimbPosition.LEFT, EnumLimbPosition.FRONT, part);
		ModelLeg.addLeg(EnumLimbPosition.RIGHT, EnumLimbPosition.FRONT, part);
			
		return LayerDefinition.create(mesh, 64, 32);
	}
	
    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
//    	this.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	
    	head.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    	body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    	legLeftRear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    	legRightRear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    	legLeftFront.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    	legRightFront.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
    
    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        this.head.xRot = headPitch * 0.017453292F;
        this.head.yRot = netHeadYaw * 0.017453292F;
        
        Vec3 motion = entityIn.getDeltaMovement();
        boolean isMoving = Math.sqrt((motion.x * motion.x) + (motion.z * motion.z)) > 0.01D;
        float tailRot = isMoving ? 0F : (float)Math.cos(entityIn.tickCount / 20F) * ModelUtils.toRadians(25D);
        for(ModelPart segment : tail)
        {
        	segment.xRot = isMoving ? ModelUtils.toRadians(10D) : 0F;
        	segment.zRot = tailRot;
        }
        
    	float standTime = entityIn.getStand();
    	this.head.y = 18.5F - (6.5F * standTime);
    	this.head.z = -4.5F + (4.5F * standTime);
    	this.body.xRot = -(ModelUtils.toRadians(60D) * standTime);
		this.body.y = 20F - (3F * standTime);
		this.body.z = -0.5F + (1.5F * standTime);
    	this.tail.get(0).xRot += ModelUtils.degree90 * standTime;
        
		legLeftRear.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		legRightRear.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		legLeftFront.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		legRightFront.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
    
    public static class ModelLeg
    {
    	ModelPart theLeg;
    	
    	private final EnumLimbPosition posX;
    	private final EnumLimbPosition posZ;
    	
    	public ModelLeg(EnumLimbPosition leftRight, EnumLimbPosition frontRear, ModelPart theModel)
    	{
    		posX = leftRight;
    		posZ = frontRear;
    		theLeg = theModel;
    	}
    	
    	public static PartDefinition addLeg(EnumLimbPosition leftRight, EnumLimbPosition frontRear, PartDefinition theModel)
    	{
    		float xPoint = (frontRear == EnumLimbPosition.FRONT ? 2.75F : 3.5F) * leftRight.getX();
    		float zPoint = (frontRear == EnumLimbPosition.FRONT ? -3.5F : 4.8F);
    		
    		CubeListBuilder legBase = CubeListBuilder.create();
    		if(leftRight == EnumLimbPosition.LEFT)
    			legBase.mirror();
    		
    		PartDefinition theLeg = theModel.addOrReplaceChild(frontRear.name().toLowerCase()+"_"+leftRight.name().toLowerCase()+"_leg", legBase, PartPose.offset(xPoint, 23F * EnumLimbPosition.DOWN.getY(), zPoint));
    			theLeg.addOrReplaceChild("foot", CubeListBuilder.create().texOffs(leftRight == EnumLimbPosition.LEFT ? 0 : 12, frontRear == EnumLimbPosition.FRONT ? 25 : 31).addBox(-1.5F, -4F, -0.5F, 3, 4, 2), PartPose.rotation(ModelUtils.degree90, 0F, 0F));
    		
    		return theLeg;
    	}
    	
        /**
         * Sets the models various rotation angles then renders the model.
         */
        public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
        {
        	theLeg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }
        
        /**
         * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
         * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
         * "far" arms and legs can swing at most.
         */
        public void setRotationAngles(AbstractRat entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
        {
        	float swingRate = 2F;
        	float swingRange = ModelUtils.toRadians(30D);
        	
            theLeg.xRot = Mth.cos(limbSwing * swingRate) * swingRange * limbSwingAmount;
            theLeg.xRot *= posX.getX() * posZ.getZ();
            
            if(posZ == EnumLimbPosition.FRONT)
            {
            	float standTime = entityIn.getStand();
            	
            	this.theLeg.y = 23F - (6.5F * standTime);
            	this.theLeg.z = -3.5F + (1.5F * standTime);
            	
            	this.theLeg.xRot += (ModelUtils.toRadians(60D) * standTime);
            }
        }
    }
}
