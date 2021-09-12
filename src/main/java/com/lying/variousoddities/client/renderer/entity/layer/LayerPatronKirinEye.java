package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.model.entity.ModelPatronKirin;
import com.lying.variousoddities.client.renderer.entity.EntityPatronKirinRenderer;
import com.lying.variousoddities.entity.wip.EntityPatronKirin;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public class LayerPatronKirinEye extends LayerRenderer<EntityPatronKirin, ModelPatronKirin>
{
	public static final ResourceLocation EYE_TEXTURE = new ResourceLocation(EntityPatronKirinRenderer.RESOURCE_BASE+"third_eye.png");
	private final Minecraft mc;
	
	public LayerPatronKirinEye(IEntityRenderer<EntityPatronKirin, ModelPatronKirin> entityRendererIn)
	{
		super(entityRendererIn);
		mc = Minecraft.getInstance();
	}
	
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityPatronKirin kirinIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(kirinIn.isInvisible()) return;
		
        RenderSystem.depthMask(true);
        matrixStackIn.push();
        	RenderSystem.disableLighting();
	        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	        setupEye(kirinIn, ageInTicks, partialTicks);
	        RenderSystem.enableLighting();
        matrixStackIn.pop();
//        GlStateManager.depthMask(false);
	}
	
	public boolean shouldCombineTextures()
	{
		return false;
	}
	
	private void setupEye(EntityPatronKirin kirinIn, float ageInTicks, float partialTicks)
	{
//    	EntityRendererManager renderManager = mc.getRenderManager();
//		double eyePos = 0.75D + Math.sin(ageInTicks / 23) * 0.05D;
//    	Vec3d viewPos = new Vec3d(renderManager.viewerPosX, renderManager.viewerPosY + renderManager.renderViewEntity.getEyeHeight(), renderManager.viewerPosZ);
//    	Vec3d offset = new Vec3d(viewPos.x - kirinIn.posX, viewPos.y - (kirinIn.posY + kirinIn.getEyeHeight() + eyePos), viewPos.z - kirinIn.posZ);
//    	
//    	float viewYaw = (float)(Math.atan2(offset.x, offset.z) * 180/Math.PI) + 180F;
//    	float viewPit = (float)(Math.asin(offset.y/offset.lengthVector()) * 180/Math.PI);
//    	
//    	double sin = (Math.sin(ageInTicks / 20) + 1) / 2;
//    	
//    	int frame = 0;
//    	if(sin > 0.95D)
//    		frame = (int)((sin - 0.95D) / 0.05D * 6);
//		
//		GlStateManager.pushMatrix();
//			GlStateManager.rotate(interpolateRotation(kirinIn.prevRenderYawOffset, kirinIn.renderYawOffset, partialTicks), 0F, -1F, 0F);
//			GlStateManager.translate(0.0D, -eyePos, 0D);
//			GlStateManager.pushMatrix();
//				GlStateManager.pushMatrix();
//		    	GlStateManager.rotate(viewYaw, 0F, -1F, 0F);
//		    	GlStateManager.rotate(viewPit, 1F, 0F, 0F);
//					GlStateManager.rotate(180F, 0F, 1F, 0F);
//					float brightness = 1.0F;
//					GlStateManager.color(brightness, brightness, brightness);
//		    		mc.getTextureManager().bindTexture(EYE_TEXTURE);
//		    		drawThirdEye(0.25D, 0.25D, 0.001D, frame);
//	    		GlStateManager.popMatrix();
//	        GlStateManager.popMatrix();
//	    GlStateManager.popMatrix();
	}
    
	private void drawThirdEye(double width, double height, double zLevel, int step)
	{
//		width /= 2;
//		height /= 2;
//		step *= 16;
//		GlStateManager.pushMatrix();
//	        float f = 1F / 16F;
//	        float f2 = 1F / 96F;
//	        Tessellator tessellator = Tessellator.getInstance();
//	        BufferBuilder bufferbuilder = tessellator.getBuffer();
//	        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
//		        bufferbuilder.pos(-width,	+height,	(double)zLevel).tex((double)((float)(0) * f),	(double)((float)(step + 16) * f2)).endVertex();
//		        bufferbuilder.pos(+width,	+height,	(double)zLevel).tex((double)((float)(16) * f),	(double)((float)(step + 16) * f2)).endVertex();
//		        bufferbuilder.pos(+width,	-height,	(double)zLevel).tex((double)((float)(16) * f),	(double)((float)(step) * f2)).endVertex();
//		        bufferbuilder.pos(-width,	-height,	(double)zLevel).tex((double)((float)(0) * f),	(double)((float)(step) * f2)).endVertex();
//	        tessellator.draw();
//        GlStateManager.popMatrix();
	}
	
    protected float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks)
    {
        float f;
        for (f = yawOffset - prevYawOffset; f < -180.0F; f += 360.0F){}
        while (f >= 180.0F)
            f -= 360.0F;
        
        return prevYawOffset + partialTicks * f;
    }
}
