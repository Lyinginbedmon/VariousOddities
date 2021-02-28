package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.model.entity.ModelCrab;
import com.lying.variousoddities.client.model.entity.ModelCrabBarnacles;
import com.lying.variousoddities.entity.AbstractCrab;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class LayerCrabBarnacles extends LayerRenderer<AbstractCrab, ModelCrab> 
{
	private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/crab/barnacles.png");
	private final ModelCrabBarnacles model;
	
	public LayerCrabBarnacles(IEntityRenderer<AbstractCrab, ModelCrab> entityRendererIn)
	{
		super(entityRendererIn);
		this.model = new ModelCrabBarnacles();
	}
	
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractCrab crabIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(crabIn.hasBarnacles())
		{
			model.setLivingAnimations(crabIn, limbSwing, limbSwingAmount, partialTicks);
			this.getEntityModel().copyModelAttributesTo(model);
			IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getArmorCutoutNoCull(getEntityTexture(crabIn)));
			model.setRotationAngles(crabIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
		}
	}
	
	public ResourceLocation getEntityTexture(AbstractCrab entitylivingbaseIn)
	{
		return TEXTURE;
	}
}
