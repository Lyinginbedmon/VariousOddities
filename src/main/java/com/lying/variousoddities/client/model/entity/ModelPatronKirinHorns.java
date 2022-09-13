package com.lying.variousoddities.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.wip.EntityPatronKirin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelPatronKirinHorns extends HumanoidModel<EntityPatronKirin>
{
	public ModelPatronKirinHorns(ModelPart partsIn)
	{
		super(partsIn);
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		PartDefinition head = part.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
			// Antlers
			head.addOrReplaceChild("right_root", CubeListBuilder.create().texOffs(0, 0).addBox(-2F, -11F, -4.1F, 1, 4, 1), PartPose.rotation(ModelUtils.toRadians(-7D), 0F, ModelUtils.toRadians(-7D)));
			head.addOrReplaceChild("right_branch", CubeListBuilder.create().texOffs(4, 0).addBox(7F, -9.5F, -5.4F, 1, 5, 1), PartPose.rotation(ModelUtils.toRadians(-26D), 0F, ModelUtils.toRadians(-67D)));
			head.addOrReplaceChild("right_branch_2", CubeListBuilder.create().texOffs(8, 0).addBox(-6F, -15.5F, -5.3F, 1, 7, 1), PartPose.rotation(ModelUtils.toRadians(-22D), 0F, ModelUtils.toRadians(5D)));
			head.addOrReplaceChild("right_point", CubeListBuilder.create().texOffs(12, 0).addBox(3F, -16F, -2F, 1, 3, 1), PartPose.rotation(ModelUtils.toRadians(-6D), 0F, ModelUtils.toRadians(-35D)));
			head.addOrReplaceChild("left_root", CubeListBuilder.create().mirror().texOffs(0, 0).addBox(1F, -11F, -4.1F, 1, 4, 1), PartPose.rotation(ModelUtils.toRadians(-7D), 0F, ModelUtils.toRadians(7D)));
			head.addOrReplaceChild("left_branch", CubeListBuilder.create().mirror().texOffs(4, 0).addBox(-8F, -9.5F, -5.4F, 1, 5, 1), PartPose.rotation(ModelUtils.toRadians(-26D), 0F, ModelUtils.toRadians(67D)));
			head.addOrReplaceChild("left_branch_2", CubeListBuilder.create().mirror().texOffs(8, 0).addBox(5F, -15.5F, -5.3F, 1, 7, 1), PartPose.rotation(ModelUtils.toRadians(-22D), 0F, ModelUtils.toRadians(-5D)));
			head.addOrReplaceChild("left_poiont", CubeListBuilder.create().mirror().texOffs(12, 0).addBox(-4F, -16F, -2F, 1, 3, 1), PartPose.rotation(ModelUtils.toRadians(-6D), 0F, ModelUtils.toRadians(35D)));
			// Ram horns
			float size = 0.5F;
			PartDefinition rightRam = head.addOrReplaceChild("right_ram", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, 1F, 1F, ModelUtils.degree5, 0F, 0F));
				rightRam.addOrReplaceChild("ram_1", CubeListBuilder.create()
					.texOffs(0, 8).addBox(-5.5F, -7.5F, -2F, 1, 2, 3, deformation.extend(size))
					.texOffs(0, 18).addBox(-5.9F, -5F, 1F, 1, 2, 2, deformation.extend(size * 0.5F)), PartPose.ZERO);
				rightRam.addOrReplaceChild("ram_2", CubeListBuilder.create().texOffs(0, 13).addBox(-5.7F, -6F, -4.5F, 1, 2, 3, deformation.extend(size * 0.75F)), PartPose.rotation(ModelUtils.toRadians(-45D), 0F, 0F));
				rightRam.addOrReplaceChild("ram_3", CubeListBuilder.create().texOffs(0, 22).addBox(-6.1F, -1.5F, 1.5F, 1, 1, 2, deformation.extend(size * 0.25F)), PartPose.rotation(ModelUtils.toRadians(45D), 0F, 0F));
				rightRam.addOrReplaceChild("ram_4", CubeListBuilder.create().texOffs(0, 25).addBox(-6.3F, -2F, -1.5F, 1, 1, 2, deformation.extend(0F)), PartPose.rotation(ModelUtils.toRadians(-7D), 0F, 0F));
			PartDefinition leftRam = head.addOrReplaceChild("left_ram", CubeListBuilder.create().mirror(), PartPose.offsetAndRotation(0F, 1F, 1F, ModelUtils.degree5, 0F, 0F));
				leftRam.addOrReplaceChild("ram_1", CubeListBuilder.create().mirror()
					.texOffs(0, 8).addBox(4.5F, -7.5F, -2F, 1, 2, 3, deformation.extend(size))
					.texOffs(0, 18).addBox(4.7F, -5F, 1F, 1, 2, 2, deformation.extend(size * 0.5F)), PartPose.ZERO);
				leftRam.addOrReplaceChild("ram_2", CubeListBuilder.create().mirror().texOffs(0, 13).addBox(4.7F, -6F, -4.5F, 1, 2, 3, deformation.extend(size * 0.75F)), PartPose.rotation(ModelUtils.toRadians(-45D), 0F, 0F));
				leftRam.addOrReplaceChild("ram_3", CubeListBuilder.create().mirror().texOffs(0, 22).addBox(4.9F, -1.5F, 1.5F, 1, 1, 2, deformation.extend(size * 0.25F)), PartPose.rotation(ModelUtils.toRadians(45D), 0F, 0F));
				leftRam.addOrReplaceChild("ram_4", CubeListBuilder.create().mirror().texOffs(0, 25).addBox(5.1F, -2F, -1.5F, 1, 1, 2, deformation.extend(0F)), PartPose.rotation(ModelUtils.toRadians(-7D), 0F, 0F));
		
		return LayerDefinition.create(mesh, 16, 16);
	}
	
	protected Iterable<ModelPart> headParts()
	{
	   return ImmutableList.of(this.head);
	}
	
	protected Iterable<ModelPart> bodyParts()
	{
	   return ImmutableList.of();
	}
}
