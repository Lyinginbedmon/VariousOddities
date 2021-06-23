package com.lying.variousoddities.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.client.model.ModelUtils;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector2f;

public class ModelDazed<T extends LivingEntity> extends BipedModel<T>
{
	private static final int BIRDS = 8;
	ModelRenderer origin;
	
	public ModelDazed()
	{
		super(1F);
		this.textureHeight = 16;
		this.textureWidth = 16;
		
		this.bipedHead = ModelUtils.freshRenderer(this);
		this.bipedHead.rotationPointY = 0F;
		
		this.origin = ModelUtils.freshRenderer(this);
		
		Vector2f vec = new Vector2f(0F, -7.2F);
		for(int i=0; i<BIRDS; i++)
		{
			float angle = ModelUtils.toRadians(i * (360F / BIRDS));
			Vector2f vector = rotateVector(vec, angle);
			
			ModelRenderer face = ModelUtils.freshRenderer(this);
			face.setRotationPoint(vector.x, -6F, vector.y);
			face.rotateAngleY = -angle;
			
			ModelRenderer face2 = ModelUtils.freshRenderer(this).setTextureOffset(0, -2).addBox(0F, -3F, -1F, 0, 6, 2);
			face2.rotateAngleX = -ModelUtils.degree90;
			face2.rotateAngleY = ModelUtils.degree90;
			face.addChild(face2);
			
			this.origin.addChild(face);
		}
		this.bipedHead.addChild(origin);
	}
	
	protected Iterable<ModelRenderer> getBodyParts()
	{
		return ImmutableList.of();
	}
	
	private Vector2f rotateVector(Vector2f vec, double angle)
	{
		double x = vec.x * Math.cos(angle) - vec.y * Math.sin(angle);
		double y = vec.x * Math.sin(angle) + vec.y * Math.cos(angle);
		return new Vector2f((float)x, (float)y);
	}
	
	public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		this.origin.rotateAngleY = -(ageInTicks / 9F);
	}
}
