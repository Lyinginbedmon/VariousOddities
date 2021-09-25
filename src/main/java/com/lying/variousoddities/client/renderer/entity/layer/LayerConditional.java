package com.lying.variousoddities.client.renderer.entity.layer;

import com.google.common.base.Predicate;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;

public class LayerConditional<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M>
{
	private final LayerRenderer<T, M> childLayer;
	private final Predicate<LivingEntity> predicate;
	
	public LayerConditional(IEntityRenderer<T, M> entityRendererIn, LayerRenderer<T, M> childLayerIn, Predicate<LivingEntity> predicateIn)
	{
		super(entityRendererIn);
		this.childLayer = childLayerIn;
		this.predicate = predicateIn;
	}
	
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(predicate.apply(entitylivingbaseIn))
			childLayer.render(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
	}
}
