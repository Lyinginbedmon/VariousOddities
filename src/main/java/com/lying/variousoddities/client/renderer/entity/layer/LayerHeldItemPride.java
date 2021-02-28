package com.lying.variousoddities.client.renderer.entity.layer;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.lying.variousoddities.item.ItemHeldFlag;
import com.lying.variousoddities.item.ItemHeldFlag.EnumPrideType;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;

public class LayerHeldItemPride<T extends LivingEntity, M extends EntityModel<T> & IHasArm> extends HeldItemLayer<T, M>
{
	public LayerHeldItemPride(IEntityRenderer<T, M> p_i50934_1_)
	{
		super(p_i50934_1_);
	}

	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(!isJune())
			super.render(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
		else
		{
			boolean flag = entitylivingbaseIn.getPrimaryHand() == HandSide.RIGHT;
			ItemStack itemLeft = flag ? entitylivingbaseIn.getHeldItemOffhand() : entitylivingbaseIn.getHeldItemMainhand();
			ItemStack itemRight = flag ? entitylivingbaseIn.getHeldItemMainhand() : entitylivingbaseIn.getHeldItemOffhand();
			
			if(flag && itemRight.isEmpty())
				itemRight = ItemHeldFlag.getItem(EnumPrideType.getRandomType(entitylivingbaseIn.getUniqueID())).getDefaultInstance();
			else if(!flag && itemLeft.isEmpty())
				itemLeft = ItemHeldFlag.getItem(EnumPrideType.getRandomType(entitylivingbaseIn.getUniqueID())).getDefaultInstance();
			
			if(!itemLeft.isEmpty() || !itemRight.isEmpty())
			{
				matrixStackIn.push();
					if (this.getEntityModel().isChild)
					{
						matrixStackIn.translate(0.0D, 0.75D, 0.0D);
						float f = 0.5F;
						matrixStackIn.scale(f, f, f);
					}
					
					this.renderItem(entitylivingbaseIn, itemRight, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, matrixStackIn, bufferIn, packedLightIn);
					this.renderItem(entitylivingbaseIn, itemLeft, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, matrixStackIn, bufferIn, packedLightIn);
				matrixStackIn.pop();
			}
		}
	}
	
	private void renderItem(LivingEntity livingEntity, ItemStack itemStack, ItemCameraTransforms.TransformType camera, HandSide handSide, MatrixStack matrix, IRenderTypeBuffer buffer, int packedLight)
	{
		if (!itemStack.isEmpty())
		{
			matrix.push();
			this.getEntityModel().translateHand(handSide, matrix);
			matrix.rotate(Vector3f.XP.rotationDegrees(-90.0F));
			matrix.rotate(Vector3f.YP.rotationDegrees(180.0F));
			boolean flag = handSide == HandSide.LEFT;
			matrix.translate((double)((float)(flag ? -1 : 1) / 16.0F), 0.125D, -0.625D);
			Minecraft.getInstance().getFirstPersonRenderer().renderItemSide(livingEntity, itemStack, camera, flag, matrix, buffer, packedLight);
			matrix.pop();
		}
	}
    
    public boolean isJune()
    {
    	Calendar calendar = new GregorianCalendar();
        return calendar.get(Calendar.MONTH) == Calendar.JUNE;
    }
}
