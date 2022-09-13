package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.entity.projectile.EntityFireballGhastling;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class EntityGhastlingFireballRenderer extends EntityRenderer<EntityFireballGhastling>
{
	private final ItemRenderer itemRenderer;
	
	public EntityGhastlingFireballRenderer(EntityRendererProvider.Context renderManager, ItemRenderer itemRendererIn)
	{
		super(renderManager);
		this.itemRenderer = itemRendererIn;
	}
	
	public void render(EntityFireballGhastling entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
	{
		if(entityIn.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entityIn) < 12.25D))
		{
			matrixStackIn.pushPose();
				matrixStackIn.scale(1f, 1f, 1f);
				matrixStackIn.mulPose(this.entityRenderDispatcher.cameraOrientation());
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180.0F));
				this.itemRenderer.renderStatic(entityIn.getItem(), ItemTransforms.TransformType.GROUND, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, entityIn.getId());
			matrixStackIn.popPose();
			super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		}
	}
	
	@SuppressWarnings("deprecation")
	public ResourceLocation getTextureLocation(EntityFireballGhastling entity)
	{
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
