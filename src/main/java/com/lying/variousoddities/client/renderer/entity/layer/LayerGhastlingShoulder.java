package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.model.entity.ModelGhastling;
import com.lying.variousoddities.entity.passive.EntityGhastling;
import com.lying.variousoddities.init.VOEntities;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class LayerGhastlingShoulder<T extends PlayerEntity> extends LayerRenderer<T, PlayerModel<T>>
{
	private final ModelGhastling ghastlingModel = new ModelGhastling();
	
	public LayerGhastlingShoulder(IEntityRenderer<T, PlayerModel<T>> entityRendererIn)
	{
		super(entityRendererIn);
	}
	
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		this.renderGhastling(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, netHeadYaw, headPitch, true);
		this.renderGhastling(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, netHeadYaw, headPitch, false);
	}
	
	private void renderGhastling(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, boolean leftShoulderIn)
	{
		CompoundNBT shoulderNBT = leftShoulderIn ? entitylivingbaseIn.getLeftShoulderEntity() : entitylivingbaseIn.getRightShoulderEntity();
		EntityType.byKey(shoulderNBT.getString("id")).filter((entityType) -> {
				return entityType == VOEntities.GHASTLING;
			}).ifPresent((entityType) -> {
				matrixStackIn.push();
					matrixStackIn.translate(leftShoulderIn ? (double)0.4F : (double)-0.4F, entitylivingbaseIn.isCrouching() ? (double)-1.3F : -1.5D, 0.0D);
					IVertexBuilder ivertexbuilder = bufferIn.getBuffer(this.ghastlingModel.getRenderType(EntityGhastling.Emotion.SLEEP.texture()));
					this.ghastlingModel.renderOnShoulder(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, limbSwing, limbSwingAmount, netHeadYaw, headPitch, entitylivingbaseIn.ticksExisted);
				matrixStackIn.pop();
			});
	}
}
