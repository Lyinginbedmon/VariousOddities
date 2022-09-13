package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelWarg;
import com.lying.variousoddities.client.model.entity.ModelWargChest;
import com.lying.variousoddities.entity.mount.EntityWarg;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class LayerWargChest extends RenderLayer<EntityWarg, ModelWarg>
{
	private final ModelWargChest model;
//	private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/mount_chest.png");
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/horse/horse_white.png");
	
	public LayerWargChest(RenderLayerParent<EntityWarg, ModelWarg> rendererIn, EntityModelSet modelsIn)
	{
		super(rendererIn);
		this.model = new ModelWargChest(modelsIn.bakeLayer(VOModelLayers.WARG_CHEST));
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, EntityWarg entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(entitylivingbaseIn.hasChest())
		{
	         this.getParentModel().copyPropertiesTo(model);
	         model.prepareMobModel(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
	         model.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
	         VertexConsumer vertexBuilder = bufferIn.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
	         model.renderToBuffer(matrixStackIn, vertexBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1.0F);
		}
	}
}
