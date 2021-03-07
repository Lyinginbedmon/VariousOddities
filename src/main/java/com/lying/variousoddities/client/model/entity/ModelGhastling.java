package com.lying.variousoddities.client.model.entity;

import java.util.Arrays;
import java.util.Random;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.passive.EntityGhastling;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.SegmentedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class ModelGhastling extends SegmentedModel<EntityGhastling>
{
	private final ModelRenderer body;
	private final ModelRenderer[] tentacles = new ModelRenderer[6];
	
	private final float TENT_SPREAD = 2F;
	
	public ModelGhastling()
	{
		this.textureHeight = 32;
		this.textureWidth = 32;
		
		body = ModelUtils.freshRenderer(this);
		body.addBox(-4F, -4F, -4F, 8, 8, 8);
		body.rotationPointY = 21F;
		
		Random rand = new Random(1660L);
		for(int i=0; i< this.tentacles.length; ++i)
		{
			this.tentacles[i] = ModelUtils.freshRenderer(this);
			this.tentacles[i].addBox(-0.5F, 0F, -0.5F, 1, rand.nextInt(3) + 4, 1, 0.7F);
			this.tentacles[i].rotationPointX = (((float)(i % 3) - (float)(i / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * TENT_SPREAD;
			this.tentacles[i].rotationPointZ = ((float)(i / 3) / 2.0F * 2.0F - 1.0F) * TENT_SPREAD;
			this.tentacles[i].rotationPointY = 2F;
			body.addChild(this.tentacles[i]);
		}
	}
	
	public Iterable<ModelRenderer> getParts()
	{
		return Arrays.asList(body);
	}
	
	public void setRotationAngles(EntityGhastling entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		this.body.rotateAngleX = headPitch * ((float)Math.PI / 180F);
		this.body.rotateAngleY = netHeadYaw * ((float)Math.PI / 180F);
		for(int i = 0; i < this.tentacles.length; ++i)
			this.tentacles[i].rotateAngleX = 0.2F * MathHelper.sin(ageInTicks * 0.3F + (float)i) + 0.4F;
	}
	
	public void renderOnShoulder(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, int ageInTicks)
	{
		this.setRotationAngles(null, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		this.getParts().forEach((p_228285_4_) -> {
			p_228285_4_.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
			});
	}
}
