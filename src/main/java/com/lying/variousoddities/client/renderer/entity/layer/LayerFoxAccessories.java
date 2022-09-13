package com.lying.variousoddities.client.renderer.entity.layer;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelFoxAccessories;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Fox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerFoxAccessories extends RenderLayer<Fox, FoxModel<Fox>>
{
	private static final ResourceLocation TEXTURE_RED = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/fox_accessories_0.png");
	private static final ResourceLocation TEXTURE_GREEN = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/fox_accessories_1.png");
	private static final ResourceLocation TEXTURE_BLUE = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/fox_accessories_2.png");
	private final ModelFoxAccessories<Fox> model;
	
	public LayerFoxAccessories(RenderLayerParent<Fox, FoxModel<Fox>> entityRendererIn, EntityModelSet modelsIn)
	{
		super(entityRendererIn);
		this.model = new ModelFoxAccessories<Fox>(modelsIn.bakeLayer(VOModelLayers.FOX_ACCESSORIES));
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Fox entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(isWinter())
		{
			model.prepareMobModel(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
			this.getParentModel().copyPropertiesTo(model);
			VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.armorCutoutNoCull(getEntityTexture(entitylivingbaseIn)));
			model.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
		}
	}
	
	public ResourceLocation getEntityTexture(Fox entitylivingbaseIn)
	{
		Random rand = new Random(entitylivingbaseIn.getUUID().getMostSignificantBits());
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
