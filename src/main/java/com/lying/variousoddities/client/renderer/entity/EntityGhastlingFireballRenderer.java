package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.entity.projectile.EntityFireballGhastling;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class EntityGhastlingFireballRenderer extends EntityRenderer<EntityFireballGhastling>
{
	private final ItemRenderer itemRenderer;
	
	public EntityGhastlingFireballRenderer(EntityRendererManager renderManager, ItemRenderer itemRendererIn)
	{
		super(renderManager);
		this.itemRenderer = itemRendererIn;
	}
	
	public void render(EntityFireballGhastling entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		if(entityIn.ticksExisted >= 2 || !(this.renderManager.info.getRenderViewEntity().getDistanceSq(entityIn) < 12.25D))
		{
			matrixStackIn.push();
				matrixStackIn.scale(1f, 1f, 1f);
				matrixStackIn.rotate(this.renderManager.getCameraOrientation());
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F));
				this.itemRenderer.renderItem(entityIn.getItem(), ItemCameraTransforms.TransformType.GROUND, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn);
			matrixStackIn.pop();
			super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		}
	}
	
	@SuppressWarnings("deprecation")
	public ResourceLocation getEntityTexture(EntityFireballGhastling entity)
	{
		return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
	}
}
