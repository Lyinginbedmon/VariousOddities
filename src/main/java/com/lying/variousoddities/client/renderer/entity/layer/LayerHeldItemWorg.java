package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.model.entity.ModelWorg;
import com.lying.variousoddities.entity.passive.EntityWorg;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerHeldItemWorg extends RenderLayer<EntityWorg, ModelWorg>
{
	private final ItemInHandRenderer itemRenderer;
	
	public LayerHeldItemWorg(RenderLayerParent<EntityWorg, ModelWorg> p_i50934_1_, ItemInHandRenderer itemRendererIn)
	{
		super(p_i50934_1_);
		this.itemRenderer = itemRendererIn;
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, EntityWorg worgIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(worgIn.getMainHandItem().isEmpty())
			return;
		
		matrixStackIn.pushPose();
			getParentModel().translateToHand(HumanoidArm.RIGHT, matrixStackIn);
			matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
			matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180.0F));
			matrixStackIn.translate(0D, 1.5D / 16D, 0.75D / 16D);
			ItemStack heldItem = worgIn.getItemBySlot(EquipmentSlot.MAINHAND);
			this.itemRenderer.renderItem(worgIn, heldItem, ItemTransforms.TransformType.GROUND, false, matrixStackIn, bufferIn, packedLightIn);
		matrixStackIn.popPose();
	}
}
