package com.lying.variousoddities.client.renderer.entity.layer;

import com.google.common.base.Predicate;
import com.lying.variousoddities.client.model.entity.IPonytailModel;
import com.lying.variousoddities.entity.wip.EntityPatronWitch;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class LayerPatronWitchPonytail extends RenderLayer<EntityPatronWitch, HumanoidModel<EntityPatronWitch>>
{
    private final Predicate<LivingEntity> conditional;
    private final ResourceLocation texture;
    
	public LayerPatronWitchPonytail(RenderLayerParent<EntityPatronWitch, HumanoidModel<EntityPatronWitch>> par1RenderWitch, Predicate<LivingEntity> condition, ResourceLocation textureIn)
	{
		super(par1RenderWitch);
		conditional = condition;
		texture = textureIn;
	}
	
	@SuppressWarnings("deprecation")
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, EntityPatronWitch par1Witch, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
        if(par1Witch.isInvisible() || !conditional.apply(par1Witch) || !(this.getParentModel() instanceof IPonytailModel)) return;
        
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        matrixStackIn.pushPose();
        double d0 = par1Witch.prevChasingPosX + (par1Witch.chasingPosX - par1Witch.prevChasingPosX) * (double)partialTicks - (par1Witch.prevPosX + (par1Witch.getX() - par1Witch.prevPosX) * (double)partialTicks);
        double d1 = par1Witch.prevChasingPosY + (par1Witch.chasingPosY - par1Witch.prevChasingPosY) * (double)partialTicks - (par1Witch.prevPosY + (par1Witch.getY() - par1Witch.prevPosY) * (double)partialTicks);
        double d2 = par1Witch.prevChasingPosZ + (par1Witch.chasingPosZ - par1Witch.prevChasingPosZ) * (double)partialTicks - (par1Witch.prevPosZ + (par1Witch.getZ() - par1Witch.prevPosZ) * (double)partialTicks);
            float f7 = par1Witch.prevRenderYawOffset + (par1Witch.renderYawOffset - par1Witch.prevRenderYawOffset) * partialTicks;
            double d3 = (double)Math.sin(f7 * (float)Math.PI / 180.0F);
            double d4 = (double)(-Math.cos(f7 * (float)Math.PI / 180.0F));
            float f8 = (float)d1 * 10.0F;
            f8 = Math.max(-6.0F, Math.min(f8, 32.0F));
            float f9 = (float)(d0 * d3 + d2 * d4) * 100.0F;
            float f10 = (float)(d0 * d4 - d2 * d3) * 100.0F;
            
            if (f9 < 0.0F){ f9 = 0.0F; }
            
            float f11 = par1Witch.prevCameraYaw + (par1Witch.cameraYaw - par1Witch.prevCameraYaw) * partialTicks;
            f8 += Math.sin((par1Witch.prevDistanceWalkedModified + (par1Witch.distanceWalkedModified - par1Witch.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * f11;
            
            matrixStackIn.mulPose(Vector3f.XP.rotation(6.0F + f9 / 2.0F + f8));
            matrixStackIn.mulPose(Vector3f.ZP.rotation(f10 / 2.0F));
            matrixStackIn.mulPose(Vector3f.YP.rotation(-f10 / 2.0F));
            
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.armorCutoutNoCull(this.texture));
            IPonytailModel model = (IPonytailModel)this.getParentModel();
            model.setPonytailHeight(-2F);
	        model.setPonytailRotation(0.0625F, f9, par1Witch.isCrouching());
	        model.renderPonytail(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY);
        matrixStackIn.popPose();
    }
    
	public boolean shouldCombineTextures()
	{
		return false;
	}
}
