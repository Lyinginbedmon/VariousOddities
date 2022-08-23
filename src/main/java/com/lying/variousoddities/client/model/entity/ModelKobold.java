package com.lying.variousoddities.client.model.entity;

import java.util.ArrayList;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.passive.EntityKobold;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;

public class ModelKobold extends HumanoidModel<EntityKobold>
{
	ModelPart snout;
	ModelPart horns;
	private final float JAW_RANGE = ModelUtils.toRadians(6D);
	ModelPart jaw;
	ModelPart belly;
	ModelPart tail;
	ArrayList<ModelPart> tailSegments = new ArrayList<ModelPart>();
	
	public ModelKobold()
	{
		this(0F);
	}
	
	public ModelKobold(float size)
	{
		super(RenderType::getEntityCutoutNoCull, 1F, 0.0F, 80, 96);
		
		/** Head */
		this.bipedHeadwear = ModelUtils.freshRenderer(this);
		this.bipedHeadwear.setTextureOffset(30, 0).addBox(-4, -6, -5, 8, 6, 7, 0.5F);
		
		this.bipedHead = ModelUtils.freshRenderer(this).setTextureOffset(0, 0).addBox(-4, -6, -5, 8, 6, 7);
		
		// Snout & jaw
		snout = ModelUtils.freshRenderer(this);
		snout.setTextureOffset(0, 13).addBox(-3F, -3.5F, -10, 6, 2, 5);
		snout.setTextureOffset(39, 13).addBox(-2F, -4.5F, -7.5F, 4, 1, 3);
    	snout.rotateAngleX = (float)(Math.toRadians(3D));
		
		jaw = ModelUtils.freshRenderer(this).setTextureOffset(22, 13).addBox(-1.5F, -1.5F, -9, 3, 1, 6);
			snout.addChild(jaw);
		
		// Horns
			int hornX = 45, hornY = 13;
		horns = ModelUtils.freshRenderer(this);
		ModelPart hornRight = ModelUtils.freshRenderer(this).setTextureOffset(hornX, hornY).addBox(-4.2F, -6.5F, -2F, 2, 2, 8);
		hornRight.rotateAngleX = ModelUtils.degree10;
		hornRight.rotateAngleY = -ModelUtils.degree5;
			horns.addChild(hornRight);
		
		ModelPart hornLeft = ModelUtils.freshRenderer(this);
		hornLeft.mirror = true;
		hornLeft.setTextureOffset(hornX, hornY).addBox(2.2F, -6.5F, -2F, 2, 2, 8);
		hornLeft.rotateAngleX = ModelUtils.degree10;
		hornLeft.rotateAngleY = ModelUtils.degree5;
			horns.addChild(hornLeft);
		
		this.bipedHead.addChild(snout);
		this.bipedHead.addChild(horns);
		
		/** Body */
		this.bipedBody = ModelUtils.freshRenderer(this);
		this.bipedBody.setTextureOffset(0, 23).addBox(-4F, 0F, -2F, 8, 5, 4);
		
		this.belly = ModelUtils.freshRenderer(this).setTextureOffset(24, 23).addBox(-3.5F, 4.8F, -1.5F, 7, 7, 3);
		this.bipedBody.addChild(this.belly);
		
		// Tail
		tail = ModelUtils.freshRenderer(this).setTextureOffset(0, 33).addBox(-1F, -1F, 2F, 2, 2, 8);
		tail.setRotationPoint(0F, 9F, -1F);
		tailSegments.add(tail);
		
		for(int i=0; i<2; i++)
		{
			ModelPart segment = makeTailSegment(i);
			tailSegments.get(i).addChild(segment);
			tailSegments.add(segment);
		}
		
		this.bipedBody.addChild(tail);
		
		/** Arms */
        ModelPart upper = ModelUtils.freshRenderer(this);
        upper.setTextureOffset(0, 43).addBox(-2.0F, -2.0F, -1.5F, 3, 6, 3, 0.01F);
        upper.rotateAngleX = ModelUtils.degree10;

        ModelPart lower = ModelUtils.freshRenderer(this).setTextureOffset(12, 43);
        lower.addBox(-2.0F, 2.5F, 1.0F, 3, 6, 3, 0.2F);
        lower.rotateAngleX = (float)(Math.toRadians(-30));

		this.bipedRightArm = ModelUtils.freshRenderer(this);
        this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        this.bipedRightArm.addChild(upper);
        this.bipedRightArm.addChild(lower);
        
        upper = ModelUtils.freshRenderer(this);
        upper.mirror=true;
        upper.setTextureOffset(24, 43).addBox(-1.0F, -2.0F, -1.5F, 3, 6, 3, 0.01F);
        upper.rotateAngleX = ModelUtils.degree10;

        lower = ModelUtils.freshRenderer(this);
        lower.mirror=true;
        lower.setTextureOffset(36, 43).addBox(-1.0F, 2.5F, 1.0F, 3, 6, 3, 0.2F);
        lower.rotateAngleX = (float)(Math.toRadians(-30));
        
        this.bipedLeftArm = ModelUtils.freshRenderer(this);
        this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        this.bipedLeftArm.addChild(upper);
        this.bipedLeftArm.addChild(lower);
		
		/** Legs */
	        // Thigh
	        ModelPart thigh = ModelUtils.freshRenderer(this).setTextureOffset(0, 52);
	        thigh.addBox(-2F, -2F, -6.5F, 4, 3, 9);
	        thigh.rotateAngleX = (float)(Math.toRadians(35D));
			
	        // Ankle
	        ModelPart ankle = ModelUtils.freshRenderer(this).setTextureOffset(26, 52);
	        ankle.addBox(-1.5F, 3.5F, -2F, 3, 2, 7);
	        ankle.rotateAngleX = (float)(Math.toRadians(-30D));
	        
	        // Foot
	        ModelPart foot = ModelUtils.freshRenderer(this).setTextureOffset(46, 52);
	        foot.addBox(-1.5F, 10F, -3F, 3, 2, 5);
	        ModelPart bridge = ModelUtils.freshRenderer(this).setTextureOffset(46, 59);
	        bridge.addBox(-1F, 3.5F, -9.75F, 2, 2, 6);
	        bridge.rotateAngleX = (float)(Math.toRadians(70D));
	        foot.addChild(bridge);
		this.bipedRightLeg = ModelUtils.freshRenderer(this);
        this.bipedRightLeg.setRotationPoint(-2.4F, 12.0F, 0.0F);
        this.bipedRightLeg.addChild(thigh);
        this.bipedRightLeg.addChild(ankle);
        this.bipedRightLeg.addChild(foot);

	        // Thigh
	        thigh = ModelUtils.freshRenderer(this).setTextureOffset(0, 67);
	        thigh.mirror=true;
	        thigh.addBox(-2F, -2F, -6.5F, 4, 3, 9);
	        thigh.rotateAngleX = (float)(Math.toRadians(35D));
			
	        // Ankle
	        ankle = ModelUtils.freshRenderer(this).setTextureOffset(26, 67);
	        ankle.mirror=true;
	        ankle.addBox(-1.5F, 3.5F, -2F, 3, 2, 7);
	        ankle.rotateAngleX = (float)(Math.toRadians(-30D));
	        
	        // Foot
	        foot = ModelUtils.freshRenderer(this).setTextureOffset(46, 67);
	        foot.mirror=true;
	        foot.addBox(-1.5F, 10F, -3F, 3, 2, 5);
	        bridge = ModelUtils.freshRenderer(this).setTextureOffset(46, 74);
	        bridge.mirror=true;
	        bridge.addBox(-1F, 3.5F, -9.75F, 2, 2, 6);
	        bridge.rotateAngleX = (float)(Math.toRadians(70D));
	        foot.addChild(bridge);
		this.bipedLeftLeg = ModelUtils.freshRenderer(this);
		this.bipedLeftLeg.addChild(thigh);
		this.bipedLeftLeg.addChild(ankle);
		this.bipedLeftLeg.addChild(foot);
		this.bipedLeftLeg.setRotationPoint(2.4F, 12F, 0F);
	}
    
    private ModelPart makeTailSegment(int tailPosition)
    {
    	ModelPart segment = ModelUtils.freshRenderer(this).setTextureOffset(20+(20*tailPosition), 33).addBox(-1F, -1F, 0F, 2, 2, 8 - (2*tailPosition));
    	segment.rotationPointZ = 9.5F - (2*tailPosition);
    	
    	return segment;
    }
	
	public void setRotationAngles(EntityKobold entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		
		float partialTicks = 0F;
		jaw.rotateAngleX = entityIn.getJawState(partialTicks) * JAW_RANGE;
		
		snout.rotationPointZ = (entityIn.getShortSnout() ? 2F : 0.5F);
		horns.showModel = entityIn.getHorns();
		
		float time = ((float)Math.sin(ageInTicks / 20)) * 0.5F;
		for(ModelPart segment : tailSegments)
		{
			segment.rotateAngleY = time / 3;
			segment.rotateAngleX = (time * Math.signum(time)) / 8;
		}
		
		float dif = this.bipedBody.rotateAngleY - this.bipedHead.rotateAngleY;
		this.bipedHead.rotateAngleZ = Math.max(-0.6F, Math.min(0.6F, dif/4));
		this.bipedHeadwear.copyModelAngles(this.bipedHead);
		
		belly.rotationPointY = (this.isSneak ? -1.3F : 0F);
		
//	    if(VOHelper.isCreatureAttribute(entityIn, EnumCreatureAttribute.UNDEAD))
//	    {
//	    	this.bipedLeftArm.rotateAngleX -= ModelUtils.degree90;
//	    	this.bipedRightArm.rotateAngleX -= ModelUtils.degree90;
//	    }
	}
}
