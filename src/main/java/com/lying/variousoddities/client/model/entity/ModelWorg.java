package com.lying.variousoddities.client.model.entity;

import java.util.Arrays;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.passive.EntityWorg;

import net.minecraft.client.renderer.entity.model.TintedAgeableModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class ModelWorg extends TintedAgeableModel<EntityWorg>
{
    public ModelRenderer head;
    public ModelRenderer jaw;
    
    public ModelRenderer body;
    public ModelRenderer mane;
    
    public ModelRenderer legRearRight;
    public ModelRenderer legRearLeft;
    public ModelRenderer legFrontRight;
    public ModelRenderer legFrontLeft;
    
    public ModelRenderer tail;
    
	private final float JAW_RANGE = ModelUtils.toRadians(15D);
	private final float LEG_SPACE = 2.5F;
	
	private float scaleFactor = 0F;
	
	public ModelWorg()
	{
    	this.textureWidth = 64;
    	this.textureHeight = 64;
    	
        this.head = ModelUtils.freshRenderer(this);
        this.head.setRotationPoint(0.0F, 14.0F, -7.5F);
        this.head.setTextureOffset(0, 0).addBox(-3.0F, -3.0F, -2.0F, 6, 6, 4, 0.0F);
        	// Headwear
        this.head.setTextureOffset(20, 0).addBox(-3.0F, -3.0F, -2.0F, 6, 6, 4, 0.25F);
        	// Muzzle
        this.head.setTextureOffset(0, 10).addBox(-1.5F, 0.0F, -5.0F, 3, 2, 4, 0.0F);
        	// Fangs
        this.head.setTextureOffset(14, 10).addBox(-1.5F, 2.0F, -5.0F, 3, 1, 4, 0.01F);
        float earAlt = -3.25F;
        float earDepth = 0.5F;
        float earSpace = 2.75F;
        	// Right ear
	        ModelRenderer earRight = ModelUtils.freshRenderer(this);
	        earRight.setRotationPoint(-earSpace, earAlt, earDepth);
	        earRight.rotateAngleZ = -ModelUtils.degree90 / 3F;
	        earRight.rotateAngleX = -ModelUtils.degree10 * 2F;
	        earRight.setTextureOffset(28, 10).addBox(-1.0F, -1.0F, -0.5F, 2, 2, 1, 0.0F);
        this.head.addChild(earRight);
        	// Left ear
	        ModelRenderer earLeft = ModelUtils.freshRenderer(this);
	        earLeft.setRotationPoint(earSpace, earAlt, earDepth);
	        earLeft.mirror = true;
	        earLeft.rotateAngleZ = ModelUtils.degree90 / 3F;
	        earLeft.rotateAngleX = -ModelUtils.degree10 * 2F;
	        earLeft.setTextureOffset(28, 13).addBox(-1.0F, -1.0F, -0.5F, 2, 2, 1, 0.0F);
        this.head.addChild(earLeft);
        
        this.jaw = ModelUtils.freshRenderer(this);
        this.jaw.setRotationPoint(0F, 2.5F, -1F);
        this.jaw.setTextureOffset(0, 16).addBox(-1.5F, -0.5F, -4, 3, 1, 3);
		this.head.addChild(jaw);
        
        this.body = ModelUtils.freshRenderer(this);
        this.body.setRotationPoint(0.0F, 14.0F, 2.0F);
        this.body.setTextureOffset(0, 20).addBox(-3.0F, -2.0F, -3.0F, 6, 9, 7, 0.0F);
        
        this.mane = ModelUtils.freshRenderer(this);
        this.mane.setRotationPoint(-1.0F, 14.0F, 2F);
        this.mane.setTextureOffset(26, 20).addBox(-3.0F, -3.0F, -3.0F, 8, 7, 9, 0.0F);
        
        this.legFrontRight = ModelUtils.freshRenderer(this);
        this.legFrontRight.setRotationPoint(-2.5F, 16.0F, -4.0F);
        this.legFrontRight.setTextureOffset(18, 36).addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        
        this.legFrontLeft = ModelUtils.freshRenderer(this);
        this.legFrontLeft.setRotationPoint(0.5F, 16.0F, -4.0F);
        this.legFrontLeft.setTextureOffset(0, 36).addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        
        this.legRearRight = ModelUtils.freshRenderer(this);
        this.legRearRight.setRotationPoint(-2.5F, 16.0F, 7.0F);
        this.legRearRight.setTextureOffset(18, 46).addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        
        this.legRearLeft = ModelUtils.freshRenderer(this);
        this.legRearLeft.setRotationPoint(0.5F, 16.0F, 7.0F);
        this.legRearLeft.setTextureOffset(0, 46).addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        
        this.tail = ModelUtils.freshRenderer(this);
        this.tail.setRotationPoint(0.0F, 12.0F, 8.0F);
        this.tail.setTextureOffset(9, 36).addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2, 0.0F);
        this.tail.setTextureOffset(9, 46).addBox(-1.0F, 3.0F, -1.0F, 2, 4, 2, 0.2F);
	}
	
	protected Iterable<ModelRenderer> getBodyParts()
	{
		return Arrays.asList(this.body, this.mane, this.legFrontLeft, this.legFrontRight, this.legRearLeft, this.legRearRight, this.tail);
	}
	
	protected Iterable<ModelRenderer> getHeadParts()
	{
		return Arrays.asList(this.head);
	}
	
	public void setLivingAnimations(EntityWorg entityIn, float limbSwing, float limbSwingAmount, float partialTickTime)
	{
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTickTime);
		this.scaleFactor = partialTickTime;
		
        if(entityIn.getAttackTarget() != null) tail.rotateAngleY = 0.0F;
        else tail.rotateAngleY = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        
        float frontLegSpace = LEG_SPACE;
        float rearLegSpace = LEG_SPACE * 0.6F;
        if(entityIn.isSitting())
        {
            mane.setRotationPoint(-1.0F, 16.0F, -3.0F);
            mane.rotateAngleX = ((float)Math.PI * 2F / 5F);
            mane.rotateAngleY = 0.0F;
            body.setRotationPoint(0.0F, 18.0F, 0.0F);
            body.rotateAngleX = ((float)Math.PI / 4F);
            
            tail.setRotationPoint(0.0F, 21.0F, 6.0F);
            tail.rotateAngleY = (float)Math.cos(entityIn.ticksExisted / 10F) * (float)Math.toRadians(25D);
            
            legRearRight.setRotationPoint(-rearLegSpace, 22.0F, 2.0F);
            legRearRight.rotateAngleX = ((float)Math.PI * 3F / 2F);
            legRearLeft.setRotationPoint(rearLegSpace, 22.0F, 2.0F);
            legRearLeft.rotateAngleX = ((float)Math.PI * 3F / 2F);
            
            legFrontRight.setRotationPoint(-frontLegSpace, 17.0F, -4.0F);
            legFrontRight.rotateAngleX = 5.811947F;
            legFrontLeft.setRotationPoint(frontLegSpace, 17.0F, -4.0F);
            legFrontLeft.rotateAngleX = 5.811947F;
        }
        else
        {
            body.setRotationPoint(0.0F, 14.0F, 2.0F);
            body.rotateAngleX = ((float)Math.PI / 2F);
            mane.setRotationPoint(-1.0F, 15F, -3.0F);
            mane.rotateAngleX = body.rotateAngleX;
            tail.setRotationPoint(0.0F, 12.0F, 8.0F);
            
            legRearRight.setRotationPoint(-rearLegSpace, 16.0F, 7.0F);
            legRearRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
            legRearLeft.setRotationPoint(rearLegSpace, 16.0F, 7.0F);
            legRearLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
            
            legFrontRight.setRotationPoint(-frontLegSpace, 16.0F, -4.0F);
            legFrontRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
            legFrontLeft.setRotationPoint(frontLegSpace, 16.0F, -4.0F);
            legFrontLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        }
        
        head.rotateAngleZ = entityIn.getInterestedAngle(partialTickTime) + entityIn.getShakeAngle(partialTickTime, 0.0F);
        mane.rotateAngleZ = entityIn.getShakeAngle(partialTickTime, -0.08F);
        body.rotateAngleZ = entityIn.getShakeAngle(partialTickTime, -0.16F);
        tail.rotateAngleZ = entityIn.getShakeAngle(partialTickTime, -0.2F);
	}
	
    public void setRotationAngles(EntityWorg entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
        head.rotateAngleX = headPitch * 0.017453292F;
        head.rotateAngleY = netHeadYaw * 0.017453292F;
        
        jaw.rotateAngleX = entityIn.getJawState(scaleFactor) * JAW_RANGE;
        jaw.rotationPointZ = -1F + jaw.rotateAngleX;
        
        tail.rotateAngleX = entityIn.getTailRotation();
	}

}
