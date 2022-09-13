package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelWarg;
import com.lying.variousoddities.client.renderer.entity.EntityWargRenderer;
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
import net.minecraft.world.item.DyeColor;

public class LayerWargDecor extends RenderLayer<EntityWarg, ModelWarg>
{
	private final ModelWarg model;
	private static final ResourceLocation[] TEXTURES = new ResourceLocation[16];
	
	public LayerWargDecor(RenderLayerParent<EntityWarg, ModelWarg> entityRendererIn, EntityModelSet modelsIn)
	{
		super(entityRendererIn);
		this.model = new ModelWarg(modelsIn.bakeLayer(VOModelLayers.WARG_DECOR));
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, EntityWarg entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(entitylivingbaseIn.getCarpetColor() == null)
			return;
		
        this.getParentModel().copyPropertiesTo(this.model);
        model.prepareMobModel(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer vertexBuilder = bufferIn.getBuffer(RenderType.entityCutoutNoCull(TEXTURES[entitylivingbaseIn.getCarpetColor().getId()]));
        model.renderToBuffer(matrixStackIn, vertexBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1.0F);
	}
	
	static
	{
		TEXTURES[DyeColor.WHITE.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/white.png");
		TEXTURES[DyeColor.ORANGE.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/orange.png");
		TEXTURES[DyeColor.MAGENTA.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/magenta.png");
		TEXTURES[DyeColor.LIGHT_BLUE.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/light_blue.png");
		TEXTURES[DyeColor.YELLOW.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/yellow.png");
		TEXTURES[DyeColor.LIME.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/lime.png");
		TEXTURES[DyeColor.PINK.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/pink.png");
		TEXTURES[DyeColor.GRAY.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/gray.png");
		TEXTURES[DyeColor.LIGHT_GRAY.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/light_gray.png");
		TEXTURES[DyeColor.CYAN.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/cyan.png");
		TEXTURES[DyeColor.PURPLE.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/purple.png");
		TEXTURES[DyeColor.BLUE.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/blue.png");
		TEXTURES[DyeColor.BROWN.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/brown.png");
		TEXTURES[DyeColor.GREEN.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/green.png");
		TEXTURES[DyeColor.RED.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/red.png");
		TEXTURES[DyeColor.BLACK.getId()] = new ResourceLocation(EntityWargRenderer.resourceBase+"decor/black.png");
	}
}
