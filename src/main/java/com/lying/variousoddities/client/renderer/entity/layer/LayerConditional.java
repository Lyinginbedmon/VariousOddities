package com.lying.variousoddities.client.renderer.entity.layer;

import com.google.common.base.Predicate;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

public class LayerConditional<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M>
{
	private final RenderLayer<T, M> childLayer;
	private final Predicate<LivingEntity> predicate;
	
	public LayerConditional(RenderLayerParent<T, M> entityRendererIn, RenderLayer<T, M> childLayerIn, Predicate<LivingEntity> predicateIn)
	{
		super(entityRendererIn);
		this.childLayer = childLayerIn;
		this.predicate = predicateIn;
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(predicate.apply(entitylivingbaseIn))
			childLayer.render(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
	}
}
