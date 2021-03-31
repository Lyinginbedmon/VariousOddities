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
	
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityWorg entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(entitylivingbaseIn.getHeldItemMainhand().isEmpty())
			return;
		
		matrixStackIn.push();
			if(entitylivingbaseIn.isChild())
			{
				float f = 0.75F;
				matrixStackIn.scale(f, f, f);
				matrixStackIn.translate(0.0D, 0.5D, (double)0.209375F);
			}
			
			// Translate to back of jaw, then rotate, then translate forwards into mouth
			// TODO Ensure accurate rotation origin
			matrixStackIn.translate((double)((this.getEntityModel()).jaw.rotationPointX / 16.0F), (double)((this.getEntityModel()).jaw.rotationPointY / 16.0F), (double)((this.getEntityModel()).jaw.rotationPointZ / 16.0F));
			if(entitylivingbaseIn.isChild())
				matrixStackIn.translate((double)0.06F, (double)0.99F, -0.5D);
			else
				matrixStackIn.translate((double)0.06F, (double)0.78F, -0.5D);
			matrixStackIn.push();
				float headTilt = entitylivingbaseIn.getInterestedAngle(partialTicks);
				matrixStackIn.rotate(Vector3f.ZP.rotation(headTilt));
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(netHeadYaw));
				matrixStackIn.rotate(Vector3f.XP.rotationDegrees(headPitch + (float)Math.toDegrees(this.getEntityModel().jaw.rotateAngleX)));
				matrixStackIn.translate(-0.03F, 0F, entitylivingbaseIn.isChild() ? -0.5F : -0.25F);
				matrixStackIn.push();
					matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90.0F));
					matrixStackIn.push();
						matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-30.0F));
						ItemStack heldItem = entitylivingbaseIn.getItemStackFromSlot(EquipmentSlotType.MAINHAND);
						Minecraft.getInstance().getFirstPersonRenderer().renderItemSide(entitylivingbaseIn, heldItem, ItemCameraTransforms.TransformType.GROUND, false, matrixStackIn, bufferIn, packedLightIn);
					matrixStackIn.pop();
				matrixStackIn.pop();
			matrixStackIn.pop();
		matrixStackIn.pop();
	}
}
