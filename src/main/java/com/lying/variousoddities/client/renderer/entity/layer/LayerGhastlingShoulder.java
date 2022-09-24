package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelGhastling;
import com.lying.variousoddities.entity.passive.EntityGhastling;
import com.lying.variousoddities.init.VOEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerGhastlingShoulder<T extends Player> extends RenderLayer<T, PlayerModel<T>>
{
	private final ModelGhastling ghastlingModel;
	
	public LayerGhastlingShoulder(RenderLayerParent<T, PlayerModel<T>> entityRendererIn, EntityModelSet modelsIn)
	{
		super(entityRendererIn);
		this.ghastlingModel = new ModelGhastling(modelsIn.bakeLayer(VOModelLayers.GHASTLING));
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		this.renderGhastling(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, netHeadYaw, headPitch, true);
		this.renderGhastling(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, netHeadYaw, headPitch, false);
	}
	
	private void renderGhastling(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, boolean leftShoulderIn)
	{
		CompoundTag shoulderNBT = leftShoulderIn ? entitylivingbaseIn.getShoulderEntityLeft() : entitylivingbaseIn.getShoulderEntityRight();
		EntityType.byString(shoulderNBT.getString("id")).filter((entityType) -> {
				return entityType == VOEntities.GHASTLING.get();
			}).ifPresent((entityType) -> {
				matrixStackIn.pushPose();
					matrixStackIn.translate(leftShoulderIn ? (double)0.4F : (double)-0.4F, entitylivingbaseIn.isCrouching() ? (double)-1.3F : -1.5D, 0.0D);
					VertexConsumer ivertexbuilder = bufferIn.getBuffer(this.ghastlingModel.renderType(EntityGhastling.Emotion.SLEEP.texture()));
					this.ghastlingModel.renderOnShoulder(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, limbSwing, limbSwingAmount, netHeadYaw, headPitch, entitylivingbaseIn.tickCount);
				matrixStackIn.popPose();
			});
	}
}
