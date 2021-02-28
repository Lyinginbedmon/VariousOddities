package com.lying.variousoddities.client.model.entity;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.client.model.EnumLimbPosition;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.AbstractRat;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class ModelRat extends EntityModel<AbstractRat>
{
	ModelRenderer head;
	ModelRenderer body;
	
	ModelLeg legLeftRear;
	ModelLeg legRightRear;
	ModelLeg legLeftFront;
	ModelLeg legRightFront;
	
	List<ModelRenderer> tail = new ArrayList<ModelRenderer>();
	
	public ModelRat()
	{
		this.textureHeight = 64;
		this.textureWidth = 32;
		
		head = ModelUtils.freshRenderer(this);
		head.rotationPointY = 18.5F * EnumLimbPosition.DOWN.getY();
		head.rotationPointZ = 4.5F * EnumLimbPosition.FRONT.getZ();
			ModelRenderer headMain = ModelUtils.freshRenderer(this);
			headMain.setTextureOffset(0, 0).addBox(-2F, -6F, -2F, 4, 6, 4);
			headMain.rotateAngleX = ModelUtils.degree90;
		head.addChild(headMain);
		
		ModelRenderer earLeft = ModelUtils.freshRenderer(this);
		earLeft.setRotationPoint(2.5F * EnumLimbPosition.LEFT.getX(), 1.5F * EnumLimbPosition.UP.getY(), 1F * EnumLimbPosition.FRONT.getZ());
		earLeft.mirror = true;
		earLeft.rotateAngleX = -ModelUtils.toRadians(15D);
		earLeft.rotateAngleY = -ModelUtils.toRadians(30D);
		earLeft.setTextureOffset(16, 0).addBox(-1.5F, -1.5F, -0.5F, 3, 3, 1, 0.2F);
		head.addChild(earLeft);
		
		ModelRenderer earRight = ModelUtils.freshRenderer(this);
		earRight.setRotationPoint(2.5F * EnumLimbPosition.RIGHT.getX(), 1.5F * EnumLimbPosition.UP.getY(), 1F * EnumLimbPosition.FRONT.getZ());
		earRight.rotateAngleX = -ModelUtils.toRadians(15D);
		earRight.rotateAngleY = ModelUtils.toRadians(30D);
		earRight.setTextureOffset(24, 0).addBox(-1.5F, -1.5F, -0.5F, 3, 3, 1, 0.2F);
		head.addChild(earRight);
		
		ModelRenderer whiskersLeft = ModelUtils.freshRenderer(this);
		whiskersLeft.setRotationPoint(2F * EnumLimbPosition.LEFT.getX(), 3F * EnumLimbPosition.UP.getY(), 5F * EnumLimbPosition.FRONT.getZ());
		whiskersLeft.rotateAngleY = -ModelUtils.toRadians(40D);
		whiskersLeft.setTextureOffset(16, 4).addBox(0F, 1.5F, 0F, 5, 3, 0);
		head.addChild(whiskersLeft);

		ModelRenderer whiskersRight = ModelUtils.freshRenderer(this);
		whiskersRight.setRotationPoint(2F * EnumLimbPosition.RIGHT.getX(), 3F * EnumLimbPosition.UP.getY(), 5F * EnumLimbPosition.FRONT.getZ());
		whiskersRight.mirror = true;
		whiskersRight.rotateAngleY = ModelUtils.toRadians(40D);
		whiskersRight.setTextureOffset(16, 7).addBox(-5F, 1.5F, 0F, 5, 3, 0);
		head.addChild(whiskersRight);
		
		body = ModelUtils.freshRenderer(this);
		body.rotationPointY = 21F * EnumLimbPosition.DOWN.getY();
			ModelRenderer bodyMain = ModelUtils.freshRenderer(this);
			bodyMain.setTextureOffset(0, 10).addBox(-3F, -4F, -2.5F, 6, 10, 5);
			bodyMain.rotateAngleX = ModelUtils.degree90;
		body.addChild(bodyMain);
		
		for(int i=0; i<3; i++)
		{
			ModelRenderer segment = createTailSegment(i, this); 
			tail.add(segment);
			if(i > 0)
			{
				tail.get(i - 1).addChild(segment);
			}
		}
		ModelRenderer root = ModelUtils.freshRenderer(this);
		root.rotationPointY = 0F;
		root.rotationPointZ = 1.85F * EnumLimbPosition.REAR.getZ();
		root.rotateAngleX = ModelUtils.degree90;
		root.addChild(tail.get(0));
		body.addChild(root);
		
		legLeftRear = new ModelLeg(EnumLimbPosition.LEFT, EnumLimbPosition.REAR, this);
		legRightRear = new ModelLeg(EnumLimbPosition.RIGHT, EnumLimbPosition.REAR, this);
		legLeftFront = new ModelLeg(EnumLimbPosition.LEFT, EnumLimbPosition.FRONT, this);
		legRightFront = new ModelLeg(EnumLimbPosition.RIGHT, EnumLimbPosition.FRONT, this);
	}
	
    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
//    	this.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	
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
    public void setRotationAngles(AbstractRat entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        this.head.rotateAngleX = headPitch * 0.017453292F;
        this.head.rotateAngleY = netHeadYaw * 0.017453292F;
        
        Vector3d motion = entityIn.getMotion();
        boolean isMoving = Math.sqrt((motion.x * motion.x) + (motion.z * motion.z)) > 0.01D;
        float tailRot = isMoving ? 0F : (float)Math.cos(entityIn.ticksExisted / 20F) * ModelUtils.toRadians(25D);
        for(ModelRenderer segment : tail)
        {
        	segment.rotateAngleX = isMoving ? ModelUtils.toRadians(10D) : 0F;
        	segment.rotateAngleZ = tailRot;
        }
        
    	float standTime = entityIn.getStand();
    	this.head.rotationPointY = 18.5F - (6.5F * standTime);
    	this.head.rotationPointZ = -4.5F + (4.5F * standTime);
    	this.body.rotateAngleX = -(ModelUtils.toRadians(60D) * standTime);
		this.body.rotationPointY = 20F - (3F * standTime);
		this.body.rotationPointZ = -0.5F + (1.5F * standTime);
    	this.tail.get(0).rotateAngleX += ModelUtils.degree90 * standTime;
        
		legLeftRear.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		legRightRear.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		legLeftFront.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		legRightFront.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
    
    private ModelRenderer createTailSegment(int i, Model model)
    {
    	ModelRenderer segment = ModelUtils.freshRenderer(model);
    	segment.rotationPointY = 3.3F * EnumLimbPosition.DOWN.getY();
    	segment.setTextureOffset(22, 10 + (4 * i)).addBox(-0.5F, 0F, -0.5F, 1, 3, 1, 0.2F-(0.01F * i));
    	return segment;
    }
    
    public class ModelLeg extends ModelRenderer
    {
    	ModelRenderer theLeg;
    	
    	private final EnumLimbPosition posX;
    	private final EnumLimbPosition posZ;
    	
    	public ModelLeg(EnumLimbPosition leftRight, EnumLimbPosition frontRear, Model theModel)
    	{
    		super(theModel);
    		this.posX = leftRight;
    		this.posZ = frontRear;
    		
    		float xPoint = (posZ == EnumLimbPosition.FRONT ? 2.75F : 3.5F) * posX.getX();
    		float zPoint = (posZ == EnumLimbPosition.FRONT ? -3.5F : 4.8F);
    		
    		theLeg = ModelUtils.freshRenderer(theModel);
    		theLeg.setRotationPoint(xPoint, 23F * EnumLimbPosition.DOWN.getY(), zPoint);
    		theLeg.mirror = posX == EnumLimbPosition.LEFT;
    			ModelRenderer legBase = ModelUtils.freshRenderer(theModel);
    			legBase.setTextureOffset(posX==EnumLimbPosition.LEFT ? 0 : 12, posZ==EnumLimbPosition.FRONT ? 25 : 31).addBox(-1.5F, -4F, -0.5F, 3, 4, 2);
    			legBase.rotateAngleX = ModelUtils.degree90;
    		theLeg.addChild(legBase);
    	}
    	
        /**
         * Sets the models various rotation angles then renders the model.
         */
        public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
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
        	
            theLeg.rotateAngleX = MathHelper.cos(limbSwing * swingRate) * swingRange * limbSwingAmount;
            theLeg.rotateAngleX *= posX.getX() * posZ.getZ();
            
            if(posZ == EnumLimbPosition.FRONT)
            {
            	float standTime = entityIn.getStand();
            	
            	this.theLeg.rotationPointY = 23F - (6.5F * standTime);
            	this.theLeg.rotationPointZ = -3.5F + (1.5F * standTime);
            	
            	this.theLeg.rotateAngleX += (ModelUtils.toRadians(60D) * standTime);
            }
        }
    }
}
