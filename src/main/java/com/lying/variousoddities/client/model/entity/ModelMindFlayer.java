package com.lying.variousoddities.client.model.entity;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.hostile.EntityMindFlayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class ModelMindFlayer extends BipedModel<EntityMindFlayer>
{
	private static Random rand = new Random(365724349000342664L);
	ModelRenderer rightEye, leftEye;
	List<Tendril> tendrils = Lists.newArrayList();
	
	public ModelMindFlayer(float modelSize)
	{
		super(modelSize);
		this.textureWidth = 64;
		this.textureHeight = 64;
		
		this.bipedHead = ModelUtils.freshRenderer(this);
			ModelRenderer brain = ModelUtils.freshRenderer(this);
			brain.setTextureOffset(16, 0).addBox(-2.5F, -7F, -2.5F, 5, 4, 6);
			brain.rotateAngleX = ModelUtils.toRadians(4D);
			this.bipedHead.addChild(brain);
			ModelRenderer brainStem = ModelUtils.freshRenderer(this);
			brainStem.setTextureOffset(0, 0).addBox(-0.5F, -5F, 0F, 1, 5, 1);
			brainStem.rotateAngleX = ModelUtils.toRadians(-35D);
			this.bipedHead.addChild(brainStem);
		
		this.bipedBody = ModelUtils.freshRenderer(this);
		this.bipedBody.setRotationPoint(0F, 0F, 0F);
		this.bipedBody.setTextureOffset(16, 16).addBox(-4F, 0F, -2F, 8, 12, 4);
		this.bipedBody.setTextureOffset(16, 32).addBox(-4F, 0F, -2F, 8, 12, 4, 0.25F);
		
		this.rightEye = ModelUtils.freshRenderer(this);
		rightEye.setRotationPoint(-2.5F, 7F, -0.5F);
		rightEye.setTextureOffset(4, 0).addBox(-1.5F, 0F, -2.5F, 3, 3, 3);
		rightEye.rotateAngleY = ModelUtils.toRadians(20D);
			this.bipedBody.addChild(rightEye);
		
		this.leftEye = ModelUtils.freshRenderer(this);
		leftEye.mirror = true;
		leftEye.setRotationPoint(2.5F, 7F, -0.5F);
		leftEye.setTextureOffset(4, 0).addBox(-1.5F, 0F, -2.5F, 3, 3, 3);
		leftEye.rotateAngleY = -ModelUtils.toRadians(20D);
			this.bipedBody.addChild(leftEye);
		
		tendrils.add(new Tendril(3, this, this.bipedBody, rand, -0.5F, 0F));
		tendrils.add(new Tendril(2, this, this.bipedBody, rand, 1.7F, 1F));
		tendrils.add(new Tendril(2, this, this.bipedBody, rand, -1.5F, -1F));
		tendrils.add(new Tendril(3, this, this.bipedBody, rand, -2.5F, 0.5F));
		tendrils.add(new Tendril(2, this, this.bipedBody, rand, 0.8F, -0.5F));
		tendrils.add(new Tendril(3, this, this.bipedBody, rand, 2.5F, -0.8F));
		
		this.bipedRightLeg = ModelUtils.freshRenderer(this);
		this.bipedRightLeg.setRotationPoint(-1.9F, 12F, 0F);
		this.bipedRightLeg.setTextureOffset(0, 16).addBox(-2F, 0F, -2F, 4, 12, 4);
		this.bipedRightLeg.setTextureOffset(0, 32).addBox(-2F, 0F, -2F, 4, 12, 4, 0.25F);
		
		this.bipedRightArm = ModelUtils.freshRenderer(this);
		this.bipedRightArm.setRotationPoint(-5F, 2F, 0F);
		this.bipedRightArm.setTextureOffset(40, 16).addBox(-3F, -2F, -2F, 4, 12, 4);
		this.bipedRightArm.setTextureOffset(40, 32).addBox(-3F, -2F, -2F, 4, 12, 4, 0.25F);
		
		this.bipedLeftLeg = ModelUtils.freshRenderer(this);
		this.bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
		this.bipedLeftLeg.setTextureOffset(16, 48).addBox(-2F, 0F, -2F, 4, 12, 4);
		this.bipedLeftLeg.setTextureOffset(0, 48).addBox(-2F, 0F, -2F, 4, 12, 4, 0.25F);
		
		this.bipedLeftArm = ModelUtils.freshRenderer(this);
		this.bipedLeftArm.setRotationPoint(5F, 2F, 0F);
		this.bipedLeftArm.setTextureOffset(32, 48).addBox(-1F, -2F, -2F, 4, 12, 4);
		this.bipedLeftArm.setTextureOffset(48, 48).addBox(-1F, -2F, -2F, 4, 12, 4, 0.25F);
	}
	
	protected Iterable<ModelRenderer> getHeadParts()
	{
		return ImmutableList.of(this.bipedHead);
	}

	protected Iterable<ModelRenderer> getBodyParts()
	{
		return ImmutableList.of(this.bipedBody, this.bipedRightArm, this.bipedLeftArm, this.bipedRightLeg, this.bipedLeftLeg);
	}
	
	public void setRotationAngles(EntityMindFlayer entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		
		if(entityIn.hasHead())
		{
			this.rightEye.showModel = this.leftEye.showModel = false;
			this.bipedHead.showModel = true;
		}
		else
		{
			this.rightEye.showModel = this.leftEye.showModel = true;
			this.bipedHead.showModel = false;
			
			Entity entity = entityIn.getAttackTarget();
			if(entity != null)
			{
				this.bipedRightArm.rotationPointZ = 0.0F;
			    this.bipedRightArm.rotationPointX = -5.0F;
			    this.bipedLeftArm.rotationPointZ = 0.0F;
			    this.bipedLeftArm.rotationPointX = 5.0F;
			    this.bipedRightArm.rotateAngleX = MathHelper.cos(ageInTicks * 0.6662F) * 0.25F;
			    this.bipedLeftArm.rotateAngleX = MathHelper.cos(ageInTicks * 0.6662F) * 0.25F;
			    this.bipedRightArm.rotateAngleZ = 2.3561945F;
			    this.bipedLeftArm.rotateAngleZ = -2.3561945F;
			    this.bipedRightArm.rotateAngleY = 0.0F;
			    this.bipedLeftArm.rotateAngleY = 0.0F;
			}
			
			if(entity == null)
				entity = Minecraft.getInstance().getRenderViewEntity();
			
			if(entity != null)
			{
				Vector3d entityEye = entity.getEyePosition(0F);
				Vector3d squidEye = entityIn.getEyePosition(0F);
				
				// FIXME Limit eye rotation to +/- of body rotation
				Vector3d offset = entityEye.subtract(squidEye).normalize();
				this.rightEye.rotateAngleX = this.leftEye.rotateAngleX = (float)Math.asin(-offset.getY());
				this.rightEye.rotateAngleY = this.rightEye.rotateAngleX = -(float)Math.atan2(offset.getX(), offset.getZ());
			}
		}
		
		for(Tendril tendril : tendrils)
			tendril.setRotationAngles(entityIn, ageInTicks);
	}
	
	private class Tendril
	{
		private final ModelRenderer[] parts;
		private final float offset;
		
		public Tendril(int count, Model parent, ModelRenderer body, Random rand, float xOff, float zOff)
		{
			offset = rand.nextInt(6) + rand.nextFloat();
			parts = new ModelRenderer[count];
			
			float prevHeight = 0F;
			for(int i=0; i<count; i++)
			{
				int height = 1 + rand.nextInt(3);
				ModelRenderer part = ModelUtils.freshRenderer(parent);
				part.rotationPointY = -prevHeight;
				part.mirror = rand.nextBoolean();
				part.setTextureOffset(0, 0).addBox(-0.5F, -(float)height, -0.5F, 1, height, 1);
				if(i>0)
					parts[i-1].addChild(part);
				else
				{
					part.setRotationPoint(xOff, 0F, zOff);
					body.addChild(part);
				}
				prevHeight = (float)height;
				parts[i] = part;
			}
		}
		
		public void setRotationAngles(EntityMindFlayer entityIn, float ageInTicks)
		{
			float tick = (ageInTicks * 0.4F) + offset;
			double mag = 1D;
			for(ModelRenderer part : parts)
				part.rotateAngleX = ModelUtils.toRadians((3D * mag++) * Math.sin(tick++));
		}
	}
}
