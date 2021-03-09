package com.lying.variousoddities.client.renderer.entity;

import javax.annotation.Nullable;

import com.lying.variousoddities.client.model.entity.ModelMarimo;
import com.lying.variousoddities.entity.passive.EntityMarimo;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@OnlyIn(Dist.CLIENT)
public class EntityMarimoRenderer extends EntityRenderer<EntityMarimo>
{
	private final ModelMarimo<EntityMarimo> entityModel;
	
	public EntityMarimoRenderer(EntityRendererManager manager) 
	{
		super(manager);
		this.entityModel = new ModelMarimo<EntityMarimo>();
		this.shadowSize = 0F;
	}
	
	public ResourceLocation getEntityTexture(EntityMarimo entity) 
	{
		return new ResourceLocation(Reference.ModInfo.MOD_ID,"textures/entity/marimo.png");
	}
	
	public void render(EntityMarimo entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
		matrixStackIn.push();
	    	float[] color = entityIn.getColor().getColorComponentValues();
	    	float ageInTicks = handleRotationFloat(entityIn, partialTicks);
	        float renderYaw = MathHelper.interpolateAngle(partialTicks, entityIn.prevRenderYawOffset, entityIn.renderYawOffset);
	        float headYaw = MathHelper.interpolateAngle(partialTicks, entityIn.prevRotationYawHead, entityIn.rotationYawHead);
	        float rotationYaw = headYaw - renderYaw;
	    	this.applyRotations(entityIn, matrixStackIn, ageInTicks, rotationYaw, partialTicks);
	        matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
	        matrixStackIn.translate(0.0D, (double)-1.501F, 0.0D);
	    	this.entityModel.setRotationAngles(entityIn, 0F, 0F, ageInTicks, entityYaw, MathHelper.lerp(partialTicks, entityIn.prevRotationPitch, entityIn.rotationPitch));
	    	
	        boolean visible = !entityIn.isInvisible();
	        boolean visibleForPlayer = !visible && !entityIn.isInvisibleToPlayer(Minecraft.getInstance().player);
	        boolean isGlowing = Minecraft.getInstance().isEntityGlowing(entityIn);
	        
	        RenderType rendertype = getRenderType(entityIn, visible, visibleForPlayer, isGlowing);
	        if(rendertype != null)
	        {
	           IVertexBuilder ivertexbuilder = bufferIn.getBuffer(rendertype);
	           int i = getPackedOverlay(entityIn, 0F);
	           this.entityModel.render(matrixStackIn, ivertexbuilder, packedLightIn, i, color[0], color[1], color[2], visibleForPlayer ? 0.15F : 1.0F);
	        }
        matrixStackIn.pop();
    }

	   /**
	    * Defines what float the third param in setRotationAngles of ModelBase is
	    */
	   protected float handleRotationFloat(EntityMarimo livingBase, float partialTicks)
	   {
	      return (float)livingBase.ticksExisted + partialTicks;
	   }
	   
	   protected void applyRotations(EntityMarimo entityLiving, MatrixStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks)
	   {
	      Pose pose = entityLiving.getPose();
	      if(pose != Pose.SLEEPING)
	      {
	         matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F - rotationYaw));
	      }
	      
	      if(entityLiving.deathTime > 0)
	      {
	         float f = Math.min(1F, MathHelper.sqrt(((float)entityLiving.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F));
	         matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(f * 90F));
	      }
	      else if (entityLiving.isSpinAttacking())
	      {
	         matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90.0F - entityLiving.rotationPitch));
	         matrixStackIn.rotate(Vector3f.YP.rotationDegrees(((float)entityLiving.ticksExisted + partialTicks) * -75.0F));
	      }
	   }
	   
	   public static int getPackedOverlay(LivingEntity livingEntityIn, float uIn)
	   {
	      return OverlayTexture.getPackedUV(OverlayTexture.getU(uIn), OverlayTexture.getV(livingEntityIn.hurtTime > 0 || livingEntityIn.deathTime > 0));
	   }
	   
	   @Nullable
	   protected RenderType getRenderType(EntityMarimo entityIn, boolean visible, boolean visibleForPlayer, boolean isGlowing)
	   {
	      ResourceLocation resourcelocation = this.getEntityTexture(entityIn);
	      if(visibleForPlayer)
	         return RenderType.getItemEntityTranslucentCull(resourcelocation);
	      else if (visible)
	         return this.entityModel.getRenderType(resourcelocation);
	      else
	         return isGlowing ? RenderType.getOutline(resourcelocation) : null;
	   }
	
	public static class RenderFactory implements IRenderFactory<EntityMarimo>
	{
		public EntityRenderer<? super EntityMarimo> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityMarimoRenderer(manager);
		}
	}
}
