package com.lying.variousoddities.client.model.entity;

import java.util.Random;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.entity.animal.Fox;

public class ModelFoxAccessories<T extends Fox> extends AgeableModel<T>
{
	private final ModelRenderer head;
	private final ModelRenderer scarfTail;
	private final ModelRenderer body;
	private final ModelRenderer legBackRight;
	private final ModelRenderer legBackLeft;
	private final ModelRenderer legFrontRight;
	private final ModelRenderer legFrontLeft;
	private float legWiggle;
	
	public ModelFoxAccessories()
	{
		super(true, 8.0F, 3.35F);
		this.textureWidth = 32;
		this.textureHeight = 32;
		
		this.head = new ModelRenderer(this);
		this.head.setRotationPoint(-1.0F, 16.5F, -3.0F);
			ModelRenderer hat1 = new ModelRenderer(this, 0, 7);
			hat1.rotateAngleZ = (float)Math.toRadians(-6D);
			hat1.addBox(-3F, -2.8F, -3.5F, 5, 1, 5);
			ModelRenderer hat2 = new ModelRenderer(this, 0, 13);
			hat2.rotateAngleX = (float)Math.toRadians(-5D);
			hat2.addBox(-2.5F, -4.3F, -3F, 4, 2, 4);
			hat1.addChild(hat2);
			ModelRenderer hat3 = new ModelRenderer(this, 0, 19);
			hat3.rotateAngleX = (float)Math.toRadians(-9D);
			hat3.rotateAngleY = (float)Math.toRadians(-7D);
			hat3.addBox(-1.5F, -4.9F, -1F, 1, 1, 3, 0.2F);
			hat1.addChild(hat3);
		this.head.addChild(hat1);
		
		this.body = new ModelRenderer(this, 0, 0);
		this.body.setRotationPoint(0.0F, 16.0F, -6.0F);
		this.body.addBox(-3F, 4F, -3.5F, 6, 1, 6, 0.3F);
			this.scarfTail = new ModelRenderer(this, -4, 0);
			this.scarfTail.setRotationPoint(0F, 4F, -3F);
			this.scarfTail.addBox(-1F, 0.1F, -4F, 2, 0, 4);
		this.body.addChild(scarfTail);
		
		float f = 0.501F;
		this.legBackRight = new ModelRenderer(this, 13, 24);
		this.legBackRight.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, f);
		this.legBackRight.setRotationPoint(-5.0F, 17.5F, 7.0F);
		this.legBackLeft = new ModelRenderer(this, 4, 24);
		this.legBackLeft.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, f);
		this.legBackLeft.setRotationPoint(-1.0F, 17.5F, 7.0F);
		this.legFrontRight = new ModelRenderer(this, 13, 24);
		this.legFrontRight.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, f);
		this.legFrontRight.setRotationPoint(-5.0F, 17.5F, 0.0F);
		this.legFrontLeft = new ModelRenderer(this, 4, 24);
		this.legFrontLeft.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, f);
		this.legFrontLeft.setRotationPoint(-1.0F, 17.5F, 0.0F);
	}
	
	public void setLivingAnimations(T entityIn, float limbSwing, float limbSwingAmount, float partialTick)
	{
		this.body.rotateAngleX = ((float)Math.PI / 2F);
		this.scarfTail.rotateAngleX = 0F;
		this.legBackRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.legBackLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.legFrontRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.legFrontLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.head.setRotationPoint(-1.0F, 16.5F, -3.0F);
		this.head.rotateAngleY = 0.0F;
		this.head.rotateAngleZ = entityIn.func_213475_v(partialTick);
		this.legBackRight.showModel = true;
		this.legBackLeft.showModel = true;
		this.legFrontRight.showModel = true;
		this.legFrontLeft.showModel = true;
		this.body.setRotationPoint(0.0F, 16.0F, -6.0F);
		this.body.rotateAngleZ = 0.0F;
		this.legBackRight.setRotationPoint(-5.0F, 17.5F, 7.0F);
		this.legBackLeft.setRotationPoint(-1.0F, 17.5F, 7.0F);
		if(entityIn.isCrouching())
		{
			this.body.rotateAngleX = 1.6755161F;
			float f = entityIn.func_213503_w(partialTick);
			this.body.setRotationPoint(0.0F, 16.0F + entityIn.func_213503_w(partialTick), -6.0F);
			this.head.setRotationPoint(-1.0F, 16.5F + f, -3.0F);
			this.head.rotateAngleY = 0.0F;
		}
		else if(entityIn.isSleeping())
		{
			this.body.rotateAngleZ = (-(float)Math.PI / 2F);
			this.body.setRotationPoint(0.0F, 21.0F, -6.0F);
			if(this.isChild)
				this.body.setRotationPoint(0.0F, 21.0F, -2.0F);
			
			this.head.setRotationPoint(1.0F, 19.49F, -3.0F);
			this.head.rotateAngleX = 0.0F;
			this.head.rotateAngleY = -2.0943952F;
			this.head.rotateAngleZ = 0.0F;
			this.legBackRight.showModel = false;
			this.legBackLeft.showModel = false;
			this.legFrontRight.showModel = false;
			this.legFrontLeft.showModel = false;
		}
		else if(entityIn.isSitting())
		{
			this.body.rotateAngleX = ((float)Math.PI / 6F);
			this.body.setRotationPoint(0.0F, 9.0F, -3.0F);
			this.scarfTail.rotateAngleX = -this.body.rotateAngleX + (float)Math.toRadians(90D);
			this.head.setRotationPoint(-1.0F, 10.0F, -0.25F);
			this.head.rotateAngleX = 0.0F;
			this.head.rotateAngleY = 0.0F;
			if(this.isChild)
				this.head.setRotationPoint(-1.0F, 13.0F, -3.75F);
		
			this.legBackRight.rotateAngleX = -1.3089969F;
			this.legBackRight.setRotationPoint(-5.0F, 21.5F, 6.75F);
			this.legBackLeft.rotateAngleX = -1.3089969F;
			this.legBackLeft.setRotationPoint(-1.0F, 21.5F, 6.75F);
			this.legFrontRight.rotateAngleX = -0.2617994F;
			this.legFrontLeft.rotateAngleX = -0.2617994F;
		}
	}
	
	protected Iterable<ModelRenderer> getHeadParts()
	{
		return ImmutableList.of(this.head);
	}
	
	protected Iterable<ModelRenderer> getBodyParts()
	{
		return ImmutableList.of(this.body, this.legBackRight, this.legBackLeft, this.legFrontRight, this.legFrontLeft);
	}

	/**
	 * Sets this entity's model rotation angles
	 */
	public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		Random rand = new Random(entityIn.getUniqueID().getLeastSignificantBits());
		int outfit = 1 + rand.nextInt(7);
		
		this.head.showModel = Boolean.valueOf((outfit & 1) > 0);
		this.body.showModel = Boolean.valueOf((outfit & 2) > 0);
		this.legBackRight.showModel = Boolean.valueOf((outfit & 4) > 0);
		this.legBackLeft.showModel = Boolean.valueOf((outfit & 4) > 0);
		this.legFrontRight.showModel = Boolean.valueOf((outfit & 4) > 0);
		this.legFrontLeft.showModel = Boolean.valueOf((outfit & 4) > 0);
		
		if(!entityIn.isSleeping() && !entityIn.isStuck() && !entityIn.isCrouching())
		{
			this.head.rotateAngleX = headPitch * ((float)Math.PI / 180F);
			this.head.rotateAngleY = netHeadYaw * ((float)Math.PI / 180F);
		}
		
		if(entityIn.isSleeping())
		{
			this.head.rotateAngleX = 0.0F;
			this.head.rotateAngleY = -2.0943952F;
			this.head.rotateAngleZ = MathHelper.cos(ageInTicks * 0.027F) / 22.0F;
		}
		
		if(entityIn.isCrouching())
		{
			float f = MathHelper.cos(ageInTicks) * 0.01F;
			this.body.rotateAngleY = f;
			this.legBackRight.rotateAngleZ = f;
			this.legBackLeft.rotateAngleZ = f;
			this.legFrontRight.rotateAngleZ = f / 2.0F;
			this.legFrontLeft.rotateAngleZ = f / 2.0F;
		}
		
		if(entityIn.isStuck())
		{
			float magnitude = 0.1F;
			this.legWiggle += 0.67F;
			this.legBackRight.rotateAngleX = MathHelper.cos(this.legWiggle * 0.4662F) * magnitude;
			this.legBackLeft.rotateAngleX = MathHelper.cos(this.legWiggle * 0.4662F + (float)Math.PI) * magnitude;
			this.legFrontRight.rotateAngleX = MathHelper.cos(this.legWiggle * 0.4662F + (float)Math.PI) * magnitude;
			this.legFrontLeft.rotateAngleX = MathHelper.cos(this.legWiggle * 0.4662F) * magnitude;
		}
	}
}
