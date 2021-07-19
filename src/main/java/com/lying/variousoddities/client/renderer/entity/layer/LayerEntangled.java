package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerEntangled<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> 
{
	private final ResourceLocation entangledTexture = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/entangled.png");
	
	public LayerEntangled(IEntityRenderer<T, M> entityRendererIn)
	{
		super(entityRendererIn);
	}
	
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		LivingData data = LivingData.forEntity(entitylivingbaseIn);
		if(data == null || !data.getVisualPotion(VOPotions.ENTANGLED))
			return;
		
		matrixStackIn.push();
			float scale = 1.01F;
			matrixStackIn.scale(scale, scale, scale);
			matrixStackIn.translate(0F, -0.01F, 0F);
			float f = ageInTicks + partialTicks;
			IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEnergySwirl(entangledTexture, MathHelper.cos(f * 0.01F) * 0.2F, f * 0.01F));
			EntityModel<T> model = getEntityModel();
			model.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
			model.setRotationAngles(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
		matrixStackIn.pop();
	}
}
