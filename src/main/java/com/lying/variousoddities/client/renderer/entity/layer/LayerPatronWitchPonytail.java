package com.lying.variousoddities.client.renderer.entity.layer;

import com.google.common.base.Predicate;
import com.lying.variousoddities.client.model.entity.IPonytailModel;
import com.lying.variousoddities.client.renderer.entity.EntityPatronWitchRenderer;
import com.lying.variousoddities.entity.wip.EntityPatronWitch;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class LayerPatronWitchPonytail extends LayerRenderer<EntityPatronWitch, BipedModel<EntityPatronWitch>>
{
    private final EntityPatronWitchRenderer witchRenderer;
    private final Predicate<LivingEntity> conditional;
    private final ResourceLocation texture;
    
	public LayerPatronWitchPonytail(EntityPatronWitchRenderer par1RenderWitch, Predicate<LivingEntity> condition, ResourceLocation textureIn)
	{
		super(par1RenderWitch);
		witchRenderer = par1RenderWitch;
		conditional = condition;
		texture = textureIn;
	}
	
	@SuppressWarnings("deprecation")
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityPatronWitch par1Witch, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
        if(par1Witch.isInvisible() || !conditional.apply(par1Witch) || !(this.witchRenderer.getEntityModel() instanceof IPonytailModel)) return;
        
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        matrixStackIn.push();
        double d0 = par1Witch.prevChasingPosX + (par1Witch.chasingPosX - par1Witch.prevChasingPosX) * (double)partialTicks - (par1Witch.prevPosX + (par1Witch.getPosX() - par1Witch.prevPosX) * (double)partialTicks);
        double d1 = par1Witch.prevChasingPosY + (par1Witch.chasingPosY - par1Witch.prevChasingPosY) * (double)partialTicks - (par1Witch.prevPosY + (par1Witch.getPosY() - par1Witch.prevPosY) * (double)partialTicks);
        double d2 = par1Witch.prevChasingPosZ + (par1Witch.chasingPosZ - par1Witch.prevChasingPosZ) * (double)partialTicks - (par1Witch.prevPosZ + (par1Witch.getPosZ() - par1Witch.prevPosZ) * (double)partialTicks);
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
            
            matrixStackIn.rotate(Vector3f.XP.rotation(6.0F + f9 / 2.0F + f8));
            matrixStackIn.rotate(Vector3f.ZP.rotation(f10 / 2.0F));
            matrixStackIn.rotate(Vector3f.YP.rotation(-f10 / 2.0F));
            
            IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getArmorCutoutNoCull(this.texture));
            IPonytailModel model = (IPonytailModel)this.witchRenderer.getEntityModel();
            model.setPonytailHeight(-2F);
	        model.setPonytailRotation(0.0625F, f9, par1Witch.isSneaking());
	        model.renderPonytail(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY);
        matrixStackIn.pop();
    }
    
	public boolean shouldCombineTextures()
	{
		return false;
	}
}
