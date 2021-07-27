package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.model.entity.ModelWarg;
import com.lying.variousoddities.client.renderer.entity.EntityWargRenderer;
import com.lying.variousoddities.entity.mount.EntityWarg;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;

public class LayerWargDecor extends LayerRenderer<EntityWarg, ModelWarg>
{
	private final ModelWarg MODEL = new ModelWarg(0.25F);
	private static final ResourceLocation[] TEXTURES = new ResourceLocation[16];
	
	public LayerWargDecor(IEntityRenderer<EntityWarg, ModelWarg> entityRendererIn)
	{
		super(entityRendererIn);
	}
	
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityWarg entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(entitylivingbaseIn.getCarpetColor() == null)
			return;
		
        this.getEntityModel().copyModelAttributesTo(this.MODEL);
        MODEL.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
        MODEL.setRotationAngles(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        IVertexBuilder vertexBuilder = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(TEXTURES[entitylivingbaseIn.getCarpetColor().getId()]));
        MODEL.render(matrixStackIn, vertexBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1.0F);
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
