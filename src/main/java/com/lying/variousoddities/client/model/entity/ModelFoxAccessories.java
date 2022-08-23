package com.lying.variousoddities.client.model.entity;

import java.util.Random;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Fox;

public class ModelFoxAccessories<T extends Fox> extends AgeableListModel<T>
{
	private final ModelPart head;
	private final ModelPart scarfTail;
	private final ModelPart body;
	private final ModelPart legBackRight;
	private final ModelPart legBackLeft;
	private final ModelPart legFrontRight;
	private final ModelPart legFrontLeft;
	private float legWiggle;
	
	public ModelFoxAccessories(ModelPart partsIn)
	{
		super(true, 8.0F, 3.35F);
		
		this.head = partsIn.getChild("head");
		
		this.body = partsIn.getChild("body");
		this.scarfTail = body.getChild("scarf_tail");
		
		this.legBackRight = partsIn.getChild("right_hind_leg");
		this.legBackLeft = partsIn.getChild("left_hind_leg");
		this.legFrontRight = partsIn.getChild("right_front_leg");
		this.legFrontLeft = partsIn.getChild("left_front_leg");
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation, float scaleIn)
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		PartDefinition head = part.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(-1.0F, 16.5F, -3.0F));
		PartDefinition hat = head.addOrReplaceChild("hat_0", CubeListBuilder.create().texOffs(0, 7).addBox(-3F, -2.8F, -3.5F, 5, 1, 5), PartPose.rotation(0F, 0F, (float)Math.toRadians(-6D)));
			hat.addOrReplaceChild("hat_1", CubeListBuilder.create().texOffs(0, 13).addBox(-2.5F, -4.3F, -3F, 4, 2, 4), PartPose.rotation((float)Math.toRadians(-5D), 0F, 0F));
			hat.addOrReplaceChild("hat_2", CubeListBuilder.create().texOffs(0, 19).addBox(-1.5F, -4.9F, -1F, 1, 1, 3, deformation.extend(0.2F)), PartPose.rotation((float)Math.toRadians(-9D), (float)Math.toRadians(-7D), 0F));
		
		PartDefinition body = part.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3F, 4F, -3.5F, 6, 1, 6, deformation.extend(0.3F)), PartPose.offset(0.0F, 16.0F, -6.0F));
			body.addOrReplaceChild("scarf_tail", CubeListBuilder.create().texOffs(-4, 0).addBox(-1F, 0.1F, -4F, 2, 0, 4), PartPose.offset(0F, 4F, -3F));
		
		float f = 0.501F;
		part.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(13, 24).addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, deformation.extend(f)), PartPose.offset(-5.0F, 17.5F, 7.0F));
		part.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(4, 24).addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, deformation.extend(f)), PartPose.offset(-1.0F, 17.5F, 7.0F));
		part.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(13, 24).addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, deformation.extend(f)), PartPose.offset(-5.0F, 17.5F, 0.0F));
		part.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(4, 24).addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, deformation.extend(f)), PartPose.offset(-1.0F, 17.5F, 0.0F));
		return LayerDefinition.create(mesh, 32, 32);
	}
	
	public void setLivingAnimations(T entityIn, float limbSwing, float limbSwingAmount, float partialTick)
	{
		this.body.xRot = ((float)Math.PI / 2F);
		this.scarfTail.xRot = 0F;
		this.legBackRight.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.legBackLeft.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.legFrontRight.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.legFrontLeft.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		
		this.head.setPos(-1.0F, 16.5F, -3.0F);
		this.head.yRot = 0.0F;
		this.head.zRot = entityIn.getHeadRollAngle(partialTick);
		this.legBackRight.visible = true;
		this.legBackLeft.visible = true;
		this.legFrontRight.visible = true;
		this.legFrontLeft.visible = true;
		this.body.setPos(0.0F, 16.0F, -6.0F);
		this.body.zRot = 0.0F;
		this.legBackRight.setPos(-5.0F, 17.5F, 7.0F);
		this.legBackLeft.setPos(-1.0F, 17.5F, 7.0F);
		if(entityIn.isCrouching())
		{
			this.body.xRot = 1.6755161F;
			float f = entityIn.getCrouchAmount(partialTick);
			this.body.setPos(0.0F, 16.0F + entityIn.getCrouchAmount(partialTick), -6.0F);
			this.head.setPos(-1.0F, 16.5F + f, -3.0F);
			this.head.yRot = 0.0F;
		}
		else if(entityIn.isSleeping())
		{
			this.body.zRot = (-(float)Math.PI / 2F);
			this.body.setPos(0.0F, 21.0F, -6.0F);
			if(this.young)
				this.body.setPos(0.0F, 21.0F, -2.0F);
			
			this.head.setPos(1.0F, 19.49F, -3.0F);
			this.head.xRot = 0.0F;
			this.head.yRot = -2.0943952F;
			this.head.zRot = 0.0F;
			this.legBackRight.visible = false;
			this.legBackLeft.visible = false;
			this.legFrontRight.visible = false;
			this.legFrontLeft.visible = false;
		}
		else if(entityIn.isSitting())
		{
			this.body.xRot = ((float)Math.PI / 6F);
			this.body.setPos(0.0F, 9.0F, -3.0F);
			this.scarfTail.xRot = -this.body.xRot + (float)Math.toRadians(90D);
			this.head.setPos(-1.0F, 10.0F, -0.25F);
			this.head.xRot = 0.0F;
			this.head.yRot = 0.0F;
			if(this.young)
				this.head.setPos(-1.0F, 13.0F, -3.75F);
			
			this.legBackRight.xRot = -1.3089969F;
			this.legBackRight.setPos(-5.0F, 21.5F, 6.75F);
			this.legBackLeft.xRot = -1.3089969F;
			this.legBackLeft.setPos(-1.0F, 21.5F, 6.75F);
			this.legFrontRight.xRot = -0.2617994F;
			this.legFrontLeft.xRot = -0.2617994F;
		}
	}
	
	protected Iterable<ModelPart> headParts()
	{
		return ImmutableList.of(this.head);
	}
	
	protected Iterable<ModelPart> bodyParts()
	{
		return ImmutableList.of(this.body, this.legBackRight, this.legBackLeft, this.legFrontRight, this.legFrontLeft);
	}
	
	/**
	 * Sets this entity's model rotation angles
	 */
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		Random rand = new Random(entityIn.getUUID().getLeastSignificantBits());
		int outfit = 1 + rand.nextInt(7);
		
		this.head.visible = Boolean.valueOf((outfit & 1) > 0);
		this.body.visible = Boolean.valueOf((outfit & 2) > 0);
		this.legBackRight.visible = Boolean.valueOf((outfit & 4) > 0);
		this.legBackLeft.visible = Boolean.valueOf((outfit & 4) > 0);
		this.legFrontRight.visible = Boolean.valueOf((outfit & 4) > 0);
		this.legFrontLeft.visible = Boolean.valueOf((outfit & 4) > 0);
		
		if(!entityIn.isSleeping() && !entityIn.isFaceplanted() && !entityIn.isCrouching())
		{
			this.head.xRot = headPitch * ((float)Math.PI / 180F);
			this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
		}
		
		if(entityIn.isSleeping())
		{
			this.head.xRot = 0.0F;
			this.head.yRot = -2.0943952F;
			this.head.zRot = Mth.cos(ageInTicks * 0.027F) / 22.0F;
		}
		
		if(entityIn.isCrouching())
		{
			float f = Mth.cos(ageInTicks) * 0.01F;
			this.body.yRot = f;
			this.legBackRight.zRot = f;
			this.legBackLeft.zRot = f;
			this.legFrontRight.zRot = f / 2.0F;
			this.legFrontLeft.zRot = f / 2.0F;
		}
		
		if(entityIn.isFaceplanted())
		{
			float magnitude = 0.1F;
			this.legWiggle += 0.67F;
			this.legBackRight.xRot = Mth.cos(this.legWiggle * 0.4662F) * magnitude;
			this.legBackLeft.xRot = Mth.cos(this.legWiggle * 0.4662F + (float)Math.PI) * magnitude;
			this.legFrontRight.xRot = Mth.cos(this.legWiggle * 0.4662F + (float)Math.PI) * magnitude;
			this.legFrontLeft.xRot = Mth.cos(this.legWiggle * 0.4662F) * magnitude;
		}
	}
}
