package com.lying.variousoddities.client.renderer.entity.layer;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.item.ItemHeldFlag;
import com.lying.variousoddities.item.ItemHeldFlag.EnumPrideType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerHeldItemPride<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends ItemInHandLayer<T, M>
{
	private final ItemInHandRenderer itemInHandRenderer;
	
	public LayerHeldItemPride(RenderLayerParent<T, M> parentRenderer, ItemInHandRenderer itemRenderer)
	{
		super(parentRenderer, itemRenderer);
		this.itemInHandRenderer = itemRenderer;
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(isJune() || ConfigVO.CLIENT.eternalPride.get())
		{
			boolean flag = entityIn.getMainArm() == HumanoidArm.RIGHT;
			ItemStack itemLeft = flag ? entityIn.getOffhandItem() : entityIn.getMainHandItem();
			ItemStack itemRight = flag ? entityIn.getMainHandItem() : entityIn.getOffhandItem();
			
			if(flag && itemRight.isEmpty())
				itemRight = ItemHeldFlag.getItem(EnumPrideType.getRandomType(entityIn.getUUID())).getDefaultInstance();
			else if(!flag && itemLeft.isEmpty())
				itemLeft = ItemHeldFlag.getItem(EnumPrideType.getRandomType(entityIn.getUUID())).getDefaultInstance();
			
			if(!itemLeft.isEmpty() || !itemRight.isEmpty())
			{
				matrixStackIn.pushPose();
					if (this.getParentModel().young)
					{
						matrixStackIn.translate(0.0D, 0.75D, 0.0D);
						float f = 0.5F;
						matrixStackIn.scale(f, f, f);
					}
					
					this.renderItem(entityIn, itemRight, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, matrixStackIn, bufferIn, packedLightIn);
					this.renderItem(entityIn, itemLeft, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, matrixStackIn, bufferIn, packedLightIn);
				matrixStackIn.popPose();
			}
		}
		else
			super.render(matrixStackIn, bufferIn, packedLightIn, entityIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
	}
	
	private void renderItem(LivingEntity livingEntity, ItemStack itemStack, ItemTransforms.TransformType camera, HumanoidArm handSide, PoseStack matrix, MultiBufferSource buffer, int packedLight)
	{
		if (!itemStack.isEmpty())
		{
			matrix.pushPose();
				this.getParentModel().translateToHand(handSide, matrix);
				matrix.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
				matrix.mulPose(Vector3f.YP.rotationDegrees(180.0F));
				boolean flag = handSide == HumanoidArm.LEFT;
				matrix.translate((double)((float)(flag ? -1 : 1) / 16.0F), 0.125D, -0.625D);
				this.itemInHandRenderer.renderItem(livingEntity, itemStack, camera, flag, matrix, buffer, packedLight);
			matrix.popPose();
		}
	}
    
    public boolean isJune()
    {
    	Calendar calendar = new GregorianCalendar();
        return calendar.get(Calendar.MONTH) == Calendar.JUNE;
    }
}
