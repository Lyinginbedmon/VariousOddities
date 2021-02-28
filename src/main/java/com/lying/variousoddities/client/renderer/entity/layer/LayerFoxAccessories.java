package com.lying.variousoddities.client.renderer.entity.layer;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import com.lying.variousoddities.client.model.entity.ModelFoxAccessories;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.FoxModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerFoxAccessories extends LayerRenderer<FoxEntity, FoxModel<FoxEntity>>
{
	private static final ResourceLocation TEXTURE_RED = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/fox_accessories_0.png");
	private static final ResourceLocation TEXTURE_GREEN = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/fox_accessories_1.png");
	private static final ResourceLocation TEXTURE_BLUE = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/fox_accessories_2.png");
	private final ModelFoxAccessories<FoxEntity> model;
	
	public LayerFoxAccessories(IEntityRenderer<FoxEntity, FoxModel<FoxEntity>> entityRendererIn)
	{
		super(entityRendererIn);
		this.model = new ModelFoxAccessories<FoxEntity>();
	}
	
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, FoxEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(isWinter())
		{
			model.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
			this.getEntityModel().copyModelAttributesTo(model);
			IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getArmorCutoutNoCull(getEntityTexture(entitylivingbaseIn)));
			model.setRotationAngles(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
		}
	}
	
	public ResourceLocation getEntityTexture(FoxEntity entitylivingbaseIn)
	{
		Random rand = new Random(entitylivingbaseIn.getUniqueID().getMostSignificantBits());
		switch(rand.nextInt(3))
		{
			case 0:	return TEXTURE_RED;
			case 1:	return TEXTURE_GREEN;
			case 2:	return TEXTURE_BLUE;
		}
		return TEXTURE_RED;
	}
	
	private boolean isWinter()
	{
		/**
		 * Winter defined as the period from December 21st to March 20th (inclusive)
		 */
    	Calendar calendar = new GregorianCalendar();
    	switch(calendar.get(Calendar.MONTH))
    	{
	    	case Calendar.JANUARY:
	    	case Calendar.FEBRUARY:	return true;
	    	case Calendar.MARCH:	return calendar.get(Calendar.DAY_OF_MONTH) <= 20;
	    	case Calendar.APRIL:
	    	case Calendar.MAY:
	    	case Calendar.JUNE:
	    	case Calendar.JULY:
	    	case Calendar.AUGUST:
	    	case Calendar.SEPTEMBER:
	    	case Calendar.OCTOBER:
	    	case Calendar.NOVEMBER: return false;
	    	case Calendar.DECEMBER: return calendar.get(Calendar.DAY_OF_MONTH) >= 21;
    	}
    	return false;
	}
}
