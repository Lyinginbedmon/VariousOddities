package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.model.entity.ModelWorg;
import com.lying.variousoddities.entity.passive.EntityWorg;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerHeldItemWorg extends LayerRenderer<EntityWorg, ModelWorg>
{
	public LayerHeldItemWorg(IEntityRenderer<EntityWorg, ModelWorg> p_i50934_1_)
	{
		super(p_i50934_1_);
	}
	
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityWorg worgIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(worgIn.getHeldItemMainhand().isEmpty())
			return;
		
		matrixStackIn.push();
			getEntityModel().translateHand(HandSide.RIGHT, matrixStackIn);
			matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90.0F));
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F));
			matrixStackIn.translate(0D, 1.5D / 16D, 0.75D / 16D);
			ItemStack heldItem = worgIn.getItemStackFromSlot(EquipmentSlotType.MAINHAND);
			Minecraft.getInstance().getFirstPersonRenderer().renderItemSide(worgIn, heldItem, ItemCameraTransforms.TransformType.GROUND, false, matrixStackIn, bufferIn, packedLightIn);
		matrixStackIn.pop();
	}
}
