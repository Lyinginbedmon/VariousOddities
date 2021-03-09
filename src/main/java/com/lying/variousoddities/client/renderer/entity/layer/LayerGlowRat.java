package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.model.entity.ModelRat;
import com.lying.variousoddities.entity.AbstractRat;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerGlowRat extends LayerOddityGlow<AbstractRat, ModelRat>
{
	String resourceBase = Reference.ModInfo.MOD_PREFIX+"textures/entity/rat/rat_";
	
	public LayerGlowRat(IEntityRenderer<AbstractRat, ModelRat> entityRendererIn)
	{
		super(entityRendererIn, null);
	}
	
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractRat entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(entitylivingbaseIn.getEyesGlow())
			super.render(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
	}
	
	protected ResourceLocation getTexture(AbstractRat ratIn)
    {
		return new ResourceLocation(resourceBase+ratIn.getRatBreed().getName()+"_glow.png");
    }
}
