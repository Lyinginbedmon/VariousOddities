package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.model.entity.ModelWarg;
import com.lying.variousoddities.client.model.entity.ModelWargChest;
import com.lying.variousoddities.entity.mount.EntityWarg;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class LayerWargChest extends LayerRenderer<EntityWarg, ModelWarg>
{
	private final ModelWargChest MODEL = new ModelWargChest(0F);
//	private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/mount_chest.png");
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/horse/horse_white.png");
	
	public LayerWargChest(IEntityRenderer<EntityWarg, ModelWarg> rendererIn)
	{
		super(rendererIn);
	}
	
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityWarg entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(entitylivingbaseIn.hasChest())
		{
	         this.getEntityModel().copyModelAttributesTo(this.MODEL);
	         MODEL.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
	         MODEL.setRotationAngles(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
	         IVertexBuilder vertexBuilder = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(TEXTURE));
	         MODEL.render(matrixStackIn, vertexBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1.0F);
		}
	}
}
