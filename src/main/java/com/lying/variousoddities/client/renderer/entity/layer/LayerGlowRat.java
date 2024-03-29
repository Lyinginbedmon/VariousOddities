package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.model.entity.ModelRat;
import com.lying.variousoddities.entity.AbstractRat;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerGlowRat<T extends AbstractRat> extends LayerOddityGlow<T, ModelRat<T>>
{
	String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/rat/rat_";
	
	public LayerGlowRat(RenderLayerParent<T, ModelRat<T>> entityRendererIn)
	{
		super(entityRendererIn, null);
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(entitylivingbaseIn.getEyesGlow())
			super.render(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
	}
	
	protected ResourceLocation getTexture(T ratIn)
    {
		return new ResourceLocation(resourceBase+ratIn.getRatBreed().getName()+"_glow.png");
    }
}
