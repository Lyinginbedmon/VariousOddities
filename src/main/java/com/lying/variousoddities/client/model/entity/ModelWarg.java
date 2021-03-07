package com.lying.variousoddities.client.model.entity;

import java.util.Arrays;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.mount.EntityWarg;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.TintedAgeableModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class ModelWarg extends TintedAgeableModel<EntityWarg>
{
    public ModelRenderer head;
    public ModelRenderer jaw;
    
    public ModelRenderer body;
    public ModelRenderer mane;
    public ModelRenderer mane2;
    
    public ModelRearLeg legRearRight;
    public ModelRearLeg legRearLeft;
    public ModelRenderer legFrontRight;
    public ModelRenderer legFrontLeft;
    
    public ModelRenderer tail;
    
	private final float JAW_RANGE = ModelUtils.toRadians(15D);
	private final float LEG_SPACE = 2.5F;
	private float scaleFactor = 0F;
    
    public ModelWarg()
    {
    	this.textureWidth = 64;
    	this.textureHeight = 128;
    	
        this.head = ModelUtils.freshRenderer(this);
        this.head.setRotationPoint(0.0F, 14.0F, -7.5F);
        this.head.setTextureOffset(0, 0).addBox(-2.5F, -2.0F, -3.0F, 5, 5, 5, 0.0F);
        	// Headwear
        this.head.setTextureOffset(20, 0).addBox(-2.5F, -2.0F, -3.0F, 5, 5, 5, 0.25F);
        	// Muzzle
        this.head.setTextureOffset(0, 10).addBox(-1.5F, 0.0F, -6.0F, 3, 2, 4, 0.0F);
        	// Fangs
        this.head.setTextureOffset(14, 10).addBox(-1.5F, 2.0F, -6.0F, 3, 1, 4, 0.01F);
        float earAlt = -2.25F;
        float earDepth = 0.5F;
        float earSpace = 2.75F;
        	// Right ear
	        ModelRenderer earRight = ModelUtils.freshRenderer(this);
	        earRight.setRotationPoint(-earSpace, earAlt, earDepth);
	        earRight.rotateAngleZ = -ModelUtils.degree90 / 3F;
	        earRight.rotateAngleY = ModelUtils.degree10 * 2F;
	        earRight.rotateAngleX = -ModelUtils.degree10 * 2F;
	        earRight.setTextureOffset(28, 10).addBox(-1.0F, -1.0F, -0.5F, 2, 3, 1, 0.0F);
        this.head.addChild(earRight);
        	// Left ear
	        ModelRenderer earLeft = ModelUtils.freshRenderer(this);
	        earLeft.setRotationPoint(earSpace, earAlt, earDepth);
	        earLeft.mirror = true;
	        earLeft.rotateAngleZ = ModelUtils.degree90 / 3F;
	        earLeft.rotateAngleY = -ModelUtils.degree10 * 2F;
	        earLeft.rotateAngleX = -ModelUtils.degree10 * 2F;
	        earLeft.setTextureOffset(28, 14).addBox(-1.0F, -1.0F, -0.5F, 2, 3, 1, 0.0F);
        this.head.addChild(earLeft);
        
        this.jaw = ModelUtils.freshRenderer(this);
        this.jaw.setRotationPoint(0F, 2.5F, -2F);
        this.jaw.setTextureOffset(0, 16).addBox(-1.5F, -0.5F, -4, 3, 1, 3);
		this.head.addChild(jaw);
        
        this.body = ModelUtils.freshRenderer(this);
        this.body.setRotationPoint(0.0F, 14.0F, 2.0F);
        this.body.setTextureOffset(0, 20).addBox(-3.0F, 2.5F, -2F, 6, 4, 5, 0.0F);
        this.body.setTextureOffset(0, 29).addBox(-2.5F, -1F, -1.5F, 5, 4, 4, 0.0F);
        
        this.mane = ModelUtils.freshRenderer(this);
        this.mane.setRotationPoint(-1.0F, 14.0F, 2F);
        this.mane.setTextureOffset(22, 20).addBox(-3.0F, -3.0F, -3F, 8, 5, 8, 0.0F);
        	this.mane2 = ModelUtils.freshRenderer(this);
        	this.mane2.setRotationPoint(0F, 3F, 4F);
        	this.mane2.setTextureOffset(22, 33).addBox(-2.0F, -1.0F, -7F, 6, 2, 7, 0.0F);
    	this.mane.addChild(mane2);
//        this.mane.setTextureOffset(22, 33).addBox(-2.0F, 2.0F, -2.5F, 6, 2, 7, 0.0F);
        
        this.legFrontRight = ModelUtils.freshRenderer(this);
        this.legFrontRight.setRotationPoint(-3F, 16F, -4F);
        this.legFrontRight.setTextureOffset(20, 42).addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        this.legFrontRight.setTextureOffset(20, 42 + 10).addBox(-1.0F, 4.0F, -1.0F, 2, 4, 2, 0.25F);
        
        this.legFrontLeft = ModelUtils.freshRenderer(this);
        this.legFrontLeft.setRotationPoint(1F, 16F, -4F);
        this.legFrontLeft.setTextureOffset(0, 42).addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        this.legFrontLeft.setTextureOffset(0, 42 + 10).addBox(-1.0F, 4.0F, -1.0F, 2, 4, 2, 0.25F);
        
        this.legRearRight = new ModelRearLeg(this, 20, 58);
        this.legRearRight.setRotationPoint(-2.5F, 16.0F, 7.0F);
        
        this.legRearLeft = new ModelRearLeg(this, 0, 58);
        this.legRearLeft.setRotationPoint(0.5F, 16.0F, 7.0F);
        
        this.tail = ModelUtils.freshRenderer(this);
        this.tail.setRotationPoint(0.0F, 12.5F, 8.0F);
        this.tail.setTextureOffset(12, 42).addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2, 0.0F);
        this.tail.setTextureOffset(12, 50).addBox(-1.0F, 3.0F, -1.0F, 2, 4, 2, 0.2F);
    }
    
	protected Iterable<ModelRenderer> getBodyParts()
	{
		return Arrays.asList(this.body, this.mane, this.legFrontLeft, this.legFrontRight, this.legRearLeft, this.legRearRight, this.tail);
	}
	
	protected Iterable<ModelRenderer> getHeadParts()
	{
		return Arrays.asList(this.head);
	}
	
	public void setLivingAnimations(EntityWarg entityIn, float limbSwing, float limbSwingAmount, float partialTickTime)
	{
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTickTime);
		this.scaleFactor = partialTickTime;
		
        if(entityIn.getAttackTarget() != null) tail.rotateAngleY = 0.0F;
        else tail.rotateAngleY = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        
        float frontLegSpace = LEG_SPACE;
        float rearLegSpace = LEG_SPACE * 0.6F + 1F;
        if(entityIn.isSitting())
        {
            mane.setRotationPoint(-1.0F, 16.0F, -3.0F);
            mane.rotateAngleX = ((float)Math.PI * 2F / 5F);
            mane.rotateAngleY = 0.0F;
            
            mane2.rotateAngleX = -ModelUtils.toRadians(12.5D);
            
            body.setRotationPoint(0.0F, 18.0F, 0.0F);
            body.rotateAngleX = ((float)Math.PI / 4F);
            
            tail.setRotationPoint(0.0F, 21.0F, 6.0F);
            tail.rotateAngleY = (float)Math.cos(entityIn.ticksExisted / 10F) * (float)Math.toRadians(25D);
            
            legRearRight.setRotationPoint(-rearLegSpace, 22.0F, 2.0F);
            legRearRight.rotateAngleX(ModelUtils.toRadians(-115D));
            legRearLeft.setRotationPoint(rearLegSpace, 22.0F, 2.0F);
            legRearLeft.rotateAngleX(ModelUtils.toRadians(-115D));
            
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
            
            mane2.rotateAngleX = 0F;
            
            tail.setRotationPoint(0.0F, 12.5F, 8.0F);
            
            legRearRight.setRotationPoint(-rearLegSpace, 14.0F, 6.5F);
            legRearRight.rotateAngleX(MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount);
            legRearLeft.setRotationPoint(rearLegSpace, 14.0F, 6.5F);
            legRearLeft.rotateAngleX(MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount);
            
            legFrontRight.setRotationPoint(-frontLegSpace, 16.0F, -4.0F);
            legFrontRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
            legFrontLeft.setRotationPoint(frontLegSpace, 16.0F, -4.0F);
            legFrontLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        }
        
        head.rotationPointY = 14.0F + limbSwingAmount / 2F;
        
        head.rotateAngleZ = entityIn.getInterestedAngle(partialTickTime) + entityIn.getShakeAngle(partialTickTime, 0.0F);
        mane.rotateAngleZ = entityIn.getShakeAngle(partialTickTime, -0.08F);
        body.rotateAngleZ = entityIn.getShakeAngle(partialTickTime, -0.16F);
        tail.rotateAngleZ = entityIn.getShakeAngle(partialTickTime, -0.2F);
	}
	
	public void setRotationAngles(EntityWarg entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
        head.rotateAngleX = headPitch * 0.017453292F;
        head.rotateAngleY = netHeadYaw * 0.017453292F;
        
        jaw.rotateAngleX = entityIn.getJawState(scaleFactor) * JAW_RANGE;
        jaw.rotationPointZ = -2F + jaw.rotateAngleX;
        
        tail.rotateAngleX = entityIn.getTailRotation();
	}
    
    public class ModelRearLeg extends ModelRenderer
    {
    	ModelRenderer upperLeg, lowerLeg;
    	
    	public ModelRearLeg(Model base, int textureX, int textureY)
    	{
    		super(base);
			this.upperLeg = ModelUtils.freshRenderer(base);
	        this.upperLeg.setRotationPoint(-2.4F, 12.0F, 0.0F);
		        // Thigh
		        ModelRenderer thigh = ModelUtils.freshRenderer(base);
		        thigh.rotateAngleX = (float)(Math.toRadians(35D));
			        ModelRenderer boxRotThigh = ModelUtils.freshRenderer(base);
			        boxRotThigh.rotationPointY = -1F;
			        boxRotThigh.setTextureOffset(textureX, textureY).addBox(-1.5F, -6F, -2F, 3, 7, 3, -0.1F);
			        boxRotThigh.rotateAngleX = ModelUtils.degree90;
		        thigh.addChild(boxRotThigh);
	        this.upperLeg.addChild(thigh);
	        
	        this.lowerLeg = ModelUtils.freshRenderer(base);
	        this.lowerLeg.setRotationPoint(0F, 3.5F, -4F);
		        // Foot
		        ModelRenderer foot = ModelUtils.freshRenderer(base);
		        foot.rotationPointZ = -1.5F;
		        foot.setTextureOffset(textureX, textureY + 17).addBox(-1F, 3F, -1.5F, 2, 2, 4);
		        ModelRenderer bridge = ModelUtils.freshRenderer(base);
		        bridge.rotateAngleX = (float)(Math.toRadians(70D));
			        ModelRenderer boxRotBridge = ModelUtils.freshRenderer(base);
			        boxRotBridge.rotationPointY = 5F;
			        boxRotBridge.setTextureOffset(textureX, textureY + 10).addBox(-1F, -3.75F, 1.5F, 2, 5, 2, -0.1F);
			        boxRotBridge.rotateAngleX = ModelUtils.degree90;
		        bridge.addChild(boxRotBridge);
		        foot.addChild(bridge);
	        this.lowerLeg.addChild(foot);
	        this.upperLeg.addChild(lowerLeg);
    	}
    	
    	public void setRotationPoint(float x, float y, float z)
    	{
    		upperLeg.setRotationPoint(x, y, z);
    	}
    	    	
    	public void rotateAngleX(float x)
    	{
    		x += ModelUtils.degree10 * 4;
    		upperLeg.rotateAngleX = x;
    		
    		float lowerLegX = Math.max(-ModelUtils.degree10 * 4, -x);
    		lowerLeg.rotateAngleX = lowerLegX;
    	}
    	
    	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    	{
    		this.upperLeg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    	}
    }
}
