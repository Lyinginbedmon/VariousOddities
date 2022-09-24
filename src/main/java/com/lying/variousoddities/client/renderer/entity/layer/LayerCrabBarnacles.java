package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelCrab;
import com.lying.variousoddities.client.model.entity.ModelCrabBarnacles;
import com.lying.variousoddities.entity.AbstractCrab;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerCrabBarnacles<T extends AbstractCrab> extends RenderLayer<T, ModelCrab<T>> 
{
	private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/crab/barnacles.png");
	private final ModelCrabBarnacles<T> model;
	
	public LayerCrabBarnacles(RenderLayerParent<T, ModelCrab<T>> entityRendererIn, EntityModelSet modelsIn)
	{
		super(entityRendererIn);
		this.model = new ModelCrabBarnacles<T>(modelsIn.bakeLayer(VOModelLayers.CRAB_BARNACLES));
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T crabIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(crabIn.hasBarnacles())
		{
			model.prepareMobModel(crabIn, limbSwing, limbSwingAmount, partialTicks);
			this.getParentModel().copyPropertiesTo(model);
			VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.armorCutoutNoCull(getEntityTexture(crabIn)));
			model.setupAnim(crabIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
		}
	}
	
	public ResourceLocation getEntityTexture(T entitylivingbaseIn)
	{
		return TEXTURE;
	}
}
