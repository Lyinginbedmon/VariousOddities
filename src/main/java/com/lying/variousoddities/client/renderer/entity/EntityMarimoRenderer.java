package com.lying.variousoddities.client.renderer.entity;

import javax.annotation.Nullable;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelMarimo;
import com.lying.variousoddities.entity.passive.EntityMarimo;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@OnlyIn(Dist.CLIENT)
public class EntityMarimoRenderer extends EntityRenderer<EntityMarimo>
{
	private final ModelMarimo<EntityMarimo> entityModel;
	
	public EntityMarimoRenderer(EntityRendererProvider.Context manager) 
	{
		super(manager);
		this.entityModel = new ModelMarimo<EntityMarimo>(manager.bakeLayer(VOModelLayers.MARIMO));
		this.shadowRadius = 0F;
	}
	
	public ResourceLocation getTextureLocation(EntityMarimo entity) 
	{
		return new ResourceLocation(Reference.ModInfo.MOD_ID,"textures/entity/marimo.png");
	}
	
	public void render(EntityMarimo entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
    {
		matrixStackIn.pushPose();
	    	float[] color = entityIn.getColor().getColorComponentValues();
	    	float ageInTicks = handleRotationFloat(entityIn, partialTicks);
	        float renderYaw = Mth.interpolateAngle(partialTicks, entityIn.prevRenderYawOffset, entityIn.renderYawOffset);
	        float headYaw = Mth.interpolateAngle(partialTicks, entityIn.prevRotationYawHead, entityIn.rotationYawHead);
	        float rotationYaw = headYaw - renderYaw;
	    	this.applyRotations(entityIn, matrixStackIn, ageInTicks, rotationYaw, partialTicks);
	        matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
	        matrixStackIn.translate(0.0D, (double)-1.501F, 0.0D);
	    	this.entityModel.setupAnim(entityIn, 0F, 0F, ageInTicks, entityYaw, Mth.lerp(partialTicks, entityIn.prevRotationPitch, entityIn.rotationPitch));
	    	
	        boolean visible = !entityIn.isInvisible();
	        boolean visibleForPlayer = !visible && !entityIn.isInvisibleTo(Minecraft.getInstance().player);
	        boolean isGlowing = Minecraft.getInstance().shouldEntityAppearGlowing(entityIn);
	        
	        RenderType rendertype = getRenderType(entityIn, visible, visibleForPlayer, isGlowing);
	        if(rendertype != null)
	        {
	           VertexConsumer ivertexbuilder = bufferIn.getBuffer(rendertype);
	           int i = getPackedOverlay(entityIn, 0F);
	           this.entityModel.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, i, color[0], color[1], color[2], visibleForPlayer ? 0.15F : 1.0F);
	        }
        matrixStackIn.popPose();
    }

	   /**
	    * Defines what float the third param in setRotationAngles of ModelBase is
	    */
	   protected float handleRotationFloat(EntityMarimo livingBase, float partialTicks)
	   {
	      return (float)livingBase.tickCount + partialTicks;
	   }
	   
	   protected void applyRotations(EntityMarimo entityLiving, PoseStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks)
	   {
	      Pose pose = entityLiving.getPose();
	      if(pose != Pose.SLEEPING)
	      {
	         matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180.0F - rotationYaw));
	      }
	      
	      if(entityLiving.deathTime > 0)
	      {
	         float f = Math.min(1F, Mth.sqrt(((float)entityLiving.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F));
	         matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(f * 90F));
	      }
	      else if (entityLiving.isAutoSpinAttack())
	      {
	         matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(-90.0F - entityLiving.rotationPitch));
	         matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(((float)entityLiving.tickCount + partialTicks) * -75.0F));
	      }
	   }
	   
	   public static int getPackedOverlay(LivingEntity livingEntityIn, float uIn)
	   {
	      return OverlayTexture.getPackedUV(OverlayTexture.getU(uIn), OverlayTexture.getV(livingEntityIn.hurtTime > 0 || livingEntityIn.deathTime > 0));
	   }
	   
	   @Nullable
	   protected RenderType getRenderType(EntityMarimo entityIn, boolean visible, boolean visibleForPlayer, boolean isGlowing)
	   {
	      ResourceLocation resourcelocation = this.getTextureLocation(entityIn);
	      if(visibleForPlayer)
	         return RenderType.itemEntityTranslucentCull(resourcelocation);
	      else if (visible)
	         return this.entityModel.renderType(resourcelocation);
	      else
	         return isGlowing ? RenderType.outline(resourcelocation) : null;
	   }
	
	public static class RenderFactory implements IRenderFactory<EntityMarimo>
	{
		public EntityRenderer<? super EntityMarimo> createRenderFor(EntityRendererProvider.Context manager) 
		{
			return new EntityMarimoRenderer(manager);
		}
	}
}
