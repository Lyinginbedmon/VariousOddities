package com.lying.variousoddities.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.client.model.ModelUtils;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;

public class ModelDazed<T extends LivingEntity> extends HumanoidModel<T>
{
	private static final int BIRDS = 8;
	ModelPart origin;
	
	public ModelDazed(ModelPart partsIn)
	{
		super(partsIn);
		
		this.origin = this.head.getChild("origin");
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0F);
		PartDefinition part = mesh.getRoot();
		
		PartDefinition head = part.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition origin = head.addOrReplaceChild("origin", CubeListBuilder.create(), PartPose.ZERO);
		
		Vec2 vec = new Vec2(0F, -7.2F);
		for(int i=0; i<BIRDS; i++)
		{
			float angle = ModelUtils.toRadians(i * (360F / BIRDS));
			Vec2 vector = rotateVector(vec, angle);
			
			PartDefinition face = origin.addOrReplaceChild("face_"+i, CubeListBuilder.create(), PartPose.offsetAndRotation(vector.x, -6F, vector.y, 0F, -angle, 0F));
				face.addOrReplaceChild("face_2", CubeListBuilder.create().texOffs(0, -2).addBox(0F, -3F, -1F, 0, 6, 2), PartPose.rotation(-ModelUtils.degree90, ModelUtils.degree90, 0F));
		}
		
		return LayerDefinition.create(mesh, 16, 16);
	}
	
	protected Iterable<ModelPart> getBodyParts()
	{
		return ImmutableList.of();
	}
	
	private static Vec2 rotateVector(Vec2 vec, double angle)
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
