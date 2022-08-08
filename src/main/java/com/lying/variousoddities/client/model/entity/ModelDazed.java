package com.lying.variousoddities.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.client.model.ModelUtils;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;

public class ModelDazed<T extends LivingEntity> extends HumanoidModel<T>
{
	private static final int BIRDS = 8;
	ModelPart origin;
	
	public ModelDazed()
	{
		super(1F);
		this.textureHeight = 16;
		this.textureWidth = 16;
		
		this.bipedHead = ModelUtils.freshRenderer(this);
		this.bipedHead.y = 0F;
		
		this.origin = ModelUtils.freshRenderer(this);
		
		Vec2 vec = new Vec2(0F, -7.2F);
		for(int i=0; i<BIRDS; i++)
		{
			float angle = ModelUtils.toRadians(i * (360F / BIRDS));
			Vec2 vector = rotateVector(vec, angle);
			
			ModelPart face = ModelUtils.freshRenderer(this);
			face.setPos(vector.x, -6F, vector.y);
			face.yRot = -angle;
			
			ModelPart face2 = ModelUtils.freshRenderer(this).setTextureOffset(0, -2).addBox(0F, -3F, -1F, 0, 6, 2);
			face2.xRot = -ModelUtils.degree90;
			face2.yRot = ModelUtils.degree90;
			face.addChild(face2);
			
			this.origin.addChild(face);
		}
		this.bipedHead.addChild(origin);
	}
	
	protected Iterable<ModelPart> getBodyParts()
	{
		return ImmutableList.of();
	}
	
	private Vec2 rotateVector(Vec2 vec, double angle)
	{
		double x = vec.x * Math.cos(angle) - vec.y * Math.sin(angle);
		double y = vec.x * Math.sin(angle) + vec.y * Math.cos(angle);
		return new Vec2((float)x, (float)y);
	}
	
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		this.origin.yRot = -(ageInTicks / 9F);
	}
}
