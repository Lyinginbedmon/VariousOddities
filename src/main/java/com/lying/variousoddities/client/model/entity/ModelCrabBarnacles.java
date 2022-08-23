package com.lying.variousoddities.client.model.entity;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.AbstractCrab;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelCrabBarnacles extends EntityModel<AbstractCrab>
{
	ModelPart body;
	
	public ModelCrabBarnacles(ModelPart partsIn)
	{
		this.body = partsIn.getChild("body");
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		PartDefinition body = part.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
			body.addOrReplaceChild("barnacles_0", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1F, 0F, -1F, 2, 3, 2, deformation)
				.texOffs(0, 5).addBox(0.25F, 1F, -3F, 2, 2, 2, deformation)
				.texOffs(0, 10).addBox(-2F, 0.5F, 0.5F, 2, 2, 2, deformation)
				.texOffs(0, 0).addBox(-0.5F, 1.5F, -0.5F, 2, 1, 2, deformation.extend(0.2F)), PartPose.offsetAndRotation(6.5F, 9.25F, 6F, -ModelUtils.degree10, 0F, 0F));
			body.addOrReplaceChild("barnacles_1", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.75F, -1F, -0.75F, 2, 2, 2, deformation)
				.texOffs(0, 5).addBox(-3F, 0F, -2.5F, 2, 3, 2, deformation.extend(0.2F))
				.texOffs(0, 10).addBox(1F, 0F, 0F, 2, 3, 2), PartPose.offsetAndRotation(-5F, 9F, -3F, -ModelUtils.degree10, -ModelUtils.degree10 * 1.5F, 0F));
			body.addOrReplaceChild("barnacles_2", CubeListBuilder.create()
				.texOffs(0, 0).addBox(1F, 0F, 2F, 2, 3, 2, deformation)
				.texOffs(0, 5).addBox(-3F, -1F, 5F, 2, 3, 2, deformation.extend(-0.2F)), PartPose.offsetAndRotation(0F, 9F, 0F, -ModelUtils.degree5 * 3F, 0, 0));
		
		return LayerDefinition.create(mesh, 16, 16);
	}
	
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
	{
		this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	public void setupAnim(AbstractCrab entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(entityIn.isPartying())
			this.body.x = (float)Math.sin(ageInTicks);
		else
			this.body.x = 0F;
	}

}
