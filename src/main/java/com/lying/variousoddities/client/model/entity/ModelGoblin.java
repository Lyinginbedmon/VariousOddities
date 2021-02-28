package com.lying.variousoddities.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class ModelGoblin extends BipedModel<EntityGoblin>
{
	ModelHead head;
	
	public ModelGoblin()
	{
		this(0F);
	}
	
	public ModelGoblin(float size)
	{
		super(RenderType::getEntityCutoutNoCull, 1F, 0.0F, 80, 96);
		
		this.textureWidth = 64;
		this.textureHeight = 64;
		
		this.bipedHeadwear = ModelUtils.freshRenderer(this);
		this.bipedHeadwear.setTextureOffset(32, 0).addBox(-4.0F, -7.0F, -3.5F, 8, 8, 8, 0.5F);
		
		this.bipedHead = ModelUtils.freshRenderer(this);
		this.head = new ModelHead(this);
		
		/** Body */
		this.bipedBody = ModelUtils.freshRenderer(this);
		this.bipedBody.setTextureOffset(0, 24).addBox(-4F, 0F, -2F, 8, 7, 4);
		this.bipedBody.setTextureOffset(24, 24).addBox(-3F, 6.8F, -1.5F, 6, 5, 3);
		
		/** Arms */
		this.bipedLeftArm = ModelUtils.freshRenderer(this);
		
		ModelRenderer upper = ModelUtils.freshRenderer(this);
		upper.setTextureOffset(52, 16).addBox(-1.5F, -0.5F, -1.5F, 3, 7, 3, -0.2F);
		upper.rotateAngleX = ModelUtils.degree10 * 1.5F;
		upper.rotateAngleZ = -ModelUtils.degree10 * 2F;
		
		ModelRenderer lower = ModelUtils.freshRenderer(this).setTextureOffset(48, 26).addBox(-2F, 0F, -2F, 4, 8, 4);
		lower.rotateAngleX = -ModelUtils.degree5;
		lower.setRotationPoint(2F, 5F, 1.5F);
		
		this.bipedLeftArm.addChild(upper);
		this.bipedLeftArm.addChild(lower);
		
		this.bipedRightArm = ModelUtils.freshRenderer(this);
		
		upper = ModelUtils.freshRenderer(this);
		upper.mirror = true;
		upper.setTextureOffset(52, 38).addBox(-1.5F, -0.5F, -1.5F, 3, 7, 3, -0.2F);
		upper.rotateAngleX = ModelUtils.degree10 * 1.5F;
		upper.rotateAngleZ = ModelUtils.degree10 * 2F;
		
		lower = ModelUtils.freshRenderer(this);
		lower.mirror = true;
		lower.setTextureOffset(48, 48).addBox(-2F, 0F, -2F, 4, 8, 4);
		lower.rotateAngleX = -ModelUtils.degree5;
		lower.setRotationPoint(-2F, 5F, 1.5F);
		
		this.bipedRightArm.addChild(upper);
		this.bipedRightArm.addChild(lower);
		
		/** Legs */
		this.bipedLeftLeg = ModelUtils.freshRenderer(this);
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
		
		ModelRenderer thigh = ModelUtils.freshRenderer(this);
		thigh.setTextureOffset(0, 35).addBox(-1.5F, -2F, -1.5F, 3, 7, 3, -0.1F);
		thigh.rotationPointX = 1F;
		thigh.rotateAngleZ = -ModelUtils.degree5;
		
		ModelRenderer shin = ModelUtils.freshRenderer(this);
		shin.setTextureOffset(0, 45).addBox(-2.5F, 0F, -2.5F, 5, 8, 5);
		shin.setRotationPoint(1.5F, 4F, 0F);
		
		this.bipedLeftLeg.addChild(thigh);
		this.bipedLeftLeg.addChild(shin);
		
		this.bipedRightLeg = ModelUtils.freshRenderer(this);
        this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
		
		thigh = ModelUtils.freshRenderer(this);
		thigh.mirror = true;
		thigh.setTextureOffset(20, 35).addBox(-1.5F, -2F, -1.5F, 3, 7, 3, -0.1F);
		thigh.rotationPointX = -1F;
		thigh.rotateAngleZ = ModelUtils.degree5;
		
		shin = ModelUtils.freshRenderer(this);
		shin.mirror = true;
		shin.setTextureOffset(20, 45).addBox(-2.5F, 0F, -2.5F, 5, 8, 5);
		shin.setRotationPoint(-1.5F, 4F, 0F);
		
		this.bipedRightLeg.addChild(thigh);
		this.bipedRightLeg.addChild(shin);
	}
	
	protected Iterable<ModelRenderer> getHeadParts()
	{
	   return ImmutableList.of(this.head);
	}
	
    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(EntityGoblin entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
		super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	
    	this.head.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	this.head.setEars(entityIn.getEars());
    	this.head.setNose(entityIn.getNose());
    }
	
	public class ModelHead extends ModelRenderer
	{
		private final float earFlare = ModelUtils.toRadians(30D);
		
		ModelRenderer head;
		ModelRenderer ears;
		ModelRenderer nose1, nose2;
		
		public ModelHead(Model par1ModelBase)
		{
			super(par1ModelBase);
			
			this.head = ModelUtils.freshRenderer(par1ModelBase);
			this.head.setTextureOffset(0, 0).addBox(-4.0F, -7.0F, -3.5F, 8, 7, 7);
			this.head.setRotationPoint(0.0F, 0.0F, 0.0F);
			
			this.ears = ModelUtils.freshRenderer(par1ModelBase);
				ModelRenderer earLeft = ModelUtils.freshRenderer(par1ModelBase);
				earLeft.setTextureOffset(21, 16).addBox(-0.5F, 0F, -0.5F, 1, 7, 1);
				earLeft.setTextureOffset(25, 11).addBox(0F, -0.5F, -5F, 0, 7, 5);
				earLeft.rotateAngleX = ModelUtils.degree90 + ModelUtils.toRadians(15D);
				earLeft.rotateAngleY = -earFlare;
				earLeft.setRotationPoint(-3.5F, -6F, -2F);
			this.ears.addChild(earLeft);
				ModelRenderer earRight = ModelUtils.freshRenderer(par1ModelBase);
				earRight.setTextureOffset(35, 16).addBox(-0.5F, 0F, -0.5F, 1, 7, 1);
				earRight.setTextureOffset(39, 11).addBox(0F, -0.5F, -5F, 0, 7, 5);
				earRight.rotateAngleX = ModelUtils.degree90 + ModelUtils.toRadians(15D);
				earRight.rotateAngleY = earFlare;
				earRight.setRotationPoint(3.5F, -6F, -2F);
			this.ears.addChild(earRight);
				this.head.addChild(this.ears);

			this.nose1 = ModelUtils.freshRenderer(par1ModelBase);
			this.nose1.setTextureOffset(0, 16).addBox(-2.5F, -4F, -3.7F, 5, 4, 1);
			this.nose1.rotateAngleX = ModelUtils.degree10;
			this.nose1.rotationPointY = -0.5F;
				this.head.addChild(this.nose1);
			
			this.nose2 = ModelUtils.freshRenderer(par1ModelBase);
			this.nose2.setTextureOffset(12, 16).addBox(-1F, 0F, -5F, 2, 6, 2);
			this.nose2.rotateAngleX = -ModelUtils.toRadians(30D);
			this.nose2.rotationPointY = -3F;
			this.nose2.rotationPointZ = 0.5F;
				this.head.addChild(this.nose2);
		}
		
	    public void setRotationAngles(EntityGoblin entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
		{
	        float f = 0.01F * (float)(entityIn.getEntityId() % 10);
	        this.nose2.rotateAngleX = -(float)(Math.toRadians(30D)) + (MathHelper.sin((float)entityIn.ticksExisted * f) * 4.5F * 0.017453292F);
	        this.nose2.rotateAngleY = 0.0F;
	        this.nose2.rotateAngleZ = MathHelper.cos((float)entityIn.ticksExisted * f) * 2.5F * 0.017453292F;
	        
	        ModelUtils.cloneRotation(bipedHead, this.head);
	        ModelUtils.clonePosition(bipedHead, this.head);
	        this.head.rotationPointY = bipedHead.rotationPointY * 4F;
		}
		
		public void setEars(boolean par1Bool){ ears.showModel = par1Bool; }
		public void setNose(boolean par1Bool)
		{
			nose1.showModel = par1Bool;
			nose2.showModel = !par1Bool;
		}
		
		public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
		{
			this.head.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		}
	}
}
