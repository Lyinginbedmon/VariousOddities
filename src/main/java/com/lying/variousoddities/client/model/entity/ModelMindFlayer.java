package com.lying.variousoddities.client.model.entity;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.hostile.EntityMindFlayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ModelMindFlayer extends HumanoidModel<EntityMindFlayer>
{
	private static Random rand = new Random(365724349000342664L);
	ModelPart rightEye, leftEye;
	List<Tendril> tendrils = Lists.newArrayList();
	
	public ModelMindFlayer(ModelPart modelPart)
	{
		super(modelPart);
		
		this.rightEye = this.body.getChild("right_eye");
		this.leftEye = this.body.getChild("left_eye");
		
		tendrils.add(new Tendril(rand, this.body.getChild("tendril_0")));
		tendrils.add(new Tendril(rand, this.body.getChild("tendril_1")));
		tendrils.add(new Tendril(rand, this.body.getChild("tendril_2")));
		tendrils.add(new Tendril(rand, this.body.getChild("tendril_3")));
		tendrils.add(new Tendril(rand, this.body.getChild("tendril_4")));
		tendrils.add(new Tendril(rand, this.body.getChild("tendril_5")));
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation, float sizeIn)
	{
		MeshDefinition mesh = HumanoidModel.createMesh(deformation, sizeIn);
		PartDefinition part = mesh.getRoot();
		
		PartDefinition head = part.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F + sizeIn, 0F));
			head.addOrReplaceChild("brain", CubeListBuilder.create().texOffs(16, 0).addBox(-2.5F, -7F, -2.5F, 5F, 4F, 6F, deformation), PartPose.rotation(ModelUtils.toRadians(4D), 0F, 0F));
			head.addOrReplaceChild("brain_stem", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -5F, 0F, 1F, 5F, 1F, deformation), PartPose.rotation(ModelUtils.toRadians(-35D), 0F, 0F));
		
		PartDefinition body = part.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(16, 16).addBox(-4F, 0F, -2F, 8F, 12F, 4F, deformation)
				.texOffs(16, 32).addBox(-4F, 0F, -2F, 8F, 12F, 4F, deformation.extend(0.25F)), PartPose.offset(0F, 0F + sizeIn, 0F));
			body.addOrReplaceChild("right_eye", CubeListBuilder.create().texOffs(4, 0).addBox(-1.5F, 0F, -2.5F, 3F, 3F, 3F, deformation), PartPose.offsetAndRotation(-2.5F, 7F, -0.5F, 0F, ModelUtils.toRadians(20D), 0F));
			body.addOrReplaceChild("left_eye", CubeListBuilder.create().mirror().texOffs(4, 0).addBox(-1.5F, 0F, -2.5F, 3F, 3F, 3F, deformation), PartPose.offsetAndRotation(2.5F, 7F, -0.5F, 0F, -ModelUtils.toRadians(20D), 0F));
			Tendril.create(0, 3, body, rand, -0.5F, 0F);
			Tendril.create(1, 2, body, rand, 1.75F, 1F);
			Tendril.create(2, 2, body, rand, -1.5F, -1F);
			Tendril.create(3, 3, body, rand, -2.5F, 0.5F);
			Tendril.create(4, 2, body, rand, 0.8F, -0.5F);
			Tendril.create(5, 3, body, rand, 2.5F, -0.8F);
		
		part.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(40, 16).addBox(-3F, -2F, -2F, 4F, 12F, 4F, deformation)
				.texOffs(40, 32).addBox(-3F, -2F, -2F, 4F, 12F, 4F, deformation.extend(0.25F)), PartPose.offset(-5F, 2F + sizeIn, 0F));
		part.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(32, 48).addBox(-1F, -2F, -2F, 4F, 12F, 4F, deformation)
				.texOffs(48, 48).addBox(-1F, -2F, -2F, 4F, 12F, 4F, deformation.extend(0.25F)), PartPose.offset(5F, 2F + sizeIn, 0F));
		
		part.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(0, 16).addBox(-2F, 0F, -2F, 4F, 12F, 4F, deformation)
				.texOffs(0, 32).addBox(-2F, 0F, -2F, 4F, 12F, 4F, deformation.extend(0.25F)), PartPose.offset(-1.9F, 12F + sizeIn, 0F));
		part.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(16, 48).addBox(-2F, 0F, -2F, 4F, 12F, 4F, deformation)
				.texOffs(0, 48).addBox(-2F, 0F, -2F, 4F, 12F, 4F, deformation.extend(0.25F)), PartPose.offset(1.9F, 12F + sizeIn, 0F));
		return LayerDefinition.create(mesh, 64, 64);
	}
	
	protected Iterable<ModelPart> headParts()
	{
		return ImmutableList.of(this.head);
	}

	protected Iterable<ModelPart> bodyParts()
	{
		return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg);
	}
	
	public void setupAnim(EntityMindFlayer entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		
		if(entityIn.hasHead())
		{
			this.rightEye.visible = this.leftEye.visible = false;
			this.head.visible = true;
		}
		else
		{
			this.rightEye.visible = this.leftEye.visible = true;
			this.head.visible = false;
			
			Entity entity = entityIn.getTarget();
			if(entity != null)
			{
				this.rightArm.z = 0F;
			    this.rightArm.x = -5F;
			    this.leftArm.z = 0F;
			    this.leftArm.x = 5F;
			    
			    this.rightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
			    this.leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
			    this.rightArm.zRot = 2.3561945F;
			    this.leftArm.zRot = -2.3561945F;
			    this.rightArm.yRot = 0F;
			    this.leftArm.yRot = 0F;
			}
			
			if(entity == null)
				entity = Minecraft.getInstance().getCameraEntity();
			
			if(entity != null)
			{
				Vec3 entityEye = entity.getEyePosition(0F);
				Vec3 squidEye = entityIn.getEyePosition(0F);
				
				// TODO Limit eye rotation to +/- of body rotation
				Vec3 offset = entityEye.subtract(squidEye).normalize();
				this.rightEye.xRot = this.leftEye.xRot = (float)Math.asin(-offset.y);
				this.rightEye.yRot = this.rightEye.yRot = -(float)Math.atan2(offset.x, offset.z);
			}
		}
		
		for(Tendril tendril : tendrils)
			tendril.setupAnim(entityIn, ageInTicks);
	}
	
	private class Tendril
	{
		private final ModelPart[] parts;
		private final float offset;
		
		public Tendril(Random rand, ModelPart root)
		{
			offset = rand.nextInt(6) + rand.nextFloat();
			
			List<ModelPart> elements = Lists.newArrayList();
			elements.add(root);
			while(root.hasChild("child"))
			{
				elements.add(root = root.getChild("child"));
			}
			parts = elements.toArray(new ModelPart[0]);
		}
		
		public static PartDefinition create(int index, int count, PartDefinition body, Random rand, float xOff, float zOff)
		{
			float prevHeight = 0F;
			PartDefinition prevPart = null;
			for(int i=0; i<count; i++)
			{
				int height = 1 + rand.nextInt(3);
				CubeListBuilder part = CubeListBuilder.create();
				if(rand.nextBoolean())
					part.mirror();
				part.texOffs(0, 0).addBox(-0.5F, -(float)height, -0.5F, 1, height, 1);
				if(i > 0)
					prevPart.addOrReplaceChild("child", part, PartPose.offset(0F, -prevHeight, 0F));
				else
					prevPart = body.addOrReplaceChild("tendril_"+index, part, PartPose.offset(xOff, -prevHeight, zOff));
				prevHeight = (float)height;
			}
			return prevPart;
		}
		
		public void setupAnim(EntityMindFlayer entityIn, float ageInTicks)
		{
			float tick = (ageInTicks * 0.4F) + offset;
			double mag = 1D;
			for(ModelPart part : parts)
				part.xRot = ModelUtils.toRadians((3D * mag++) * Math.sin(tick++));
		}
	}
}
