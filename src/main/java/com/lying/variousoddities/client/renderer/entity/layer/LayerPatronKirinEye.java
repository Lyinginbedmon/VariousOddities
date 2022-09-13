package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.model.entity.ModelPatronKirin;
import com.lying.variousoddities.client.renderer.entity.EntityPatronKirinRenderer;
import com.lying.variousoddities.entity.wip.EntityPatronKirin;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class LayerPatronKirinEye extends RenderLayer<EntityPatronKirin, ModelPatronKirin>
{
	public static final ResourceLocation EYE_TEXTURE = new ResourceLocation(EntityPatronKirinRenderer.RESOURCE_BASE+"third_eye.png");
	private final Minecraft mc;
	
	public LayerPatronKirinEye(RenderLayerParent<EntityPatronKirin, ModelPatronKirin> entityRendererIn)
	{
		super(entityRendererIn);
		mc = Minecraft.getInstance();
	}
	
	@SuppressWarnings("deprecation")
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, EntityPatronKirin kirinIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(kirinIn.isInvisible()) return;
		
        RenderSystem.depthMask(true);
        matrixStackIn.pushPose();
        	RenderSystem.disableLighting();
	        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	        setupEye(matrixStackIn, kirinIn, ageInTicks, partialTicks);
	        RenderSystem.enableLighting();
        matrixStackIn.popPose();
//        GlStateManager.depthMask(false);
	}
	
	public boolean shouldCombineTextures()
	{
		return false;
	}
	
	@SuppressWarnings("deprecation")
	private void setupEye(PoseStack matrixStack, EntityPatronKirin kirinIn, float ageInTicks, float partialTicks)
	{
		double eyePos = 0.75D + Math.sin(ageInTicks / 23) * 0.05D;
    	Vec3 viewPos = mc.getCameraEntity().getEyePosition(0F);
    	Vec3 offset = new Vec3(viewPos.x - kirinIn.getX(), viewPos.y - (kirinIn.getEyeY() + eyePos), viewPos.z - kirinIn.getZ());
    	
    	float viewYaw = (float)(Math.atan2(offset.x, offset.z) * 180/Math.PI) + 180F;
    	float viewPit = (float)(Math.asin(offset.y/offset.length()) * 180/Math.PI);
    	
    	double sin = (Math.sin(ageInTicks / 20) + 1) / 2;
    	
    	int frame = 0;
    	if(sin > 0.95D)
    		frame = (int)((sin - 0.95D) / 0.05D * 6);
		
		matrixStack.pushPose();
			matrixStack.mulPose(Vector3f.YP.rotation(interpolateRotation(kirinIn.prevRenderYawOffset, kirinIn.renderYawOffset, partialTicks)));
			matrixStack.translate(0.0D, -eyePos, 0D);
			matrixStack.pushPose();
				matrixStack.pushPose();
		    	matrixStack.mulPose(Vector3f.YP.rotation(viewYaw));
		    	matrixStack.mulPose(Vector3f.XP.rotation(viewPit));
					matrixStack.mulPose(Vector3f.YP.rotationDegrees(180F));
					float brightness = 1.0F;
					RenderSystem.color3f(brightness, brightness, brightness);
		    		mc.getTextureManager().bindTexture(EYE_TEXTURE);
		    		drawThirdEye(matrixStack, 0.25D, 0.25D, 0.001D, frame);
	    		matrixStack.popPose();
	        matrixStack.popPose();
	    matrixStack.popPose();
	}
    
	private void drawThirdEye(PoseStack matrixStack, double width, double height, double zLevel, int step)
	{
		width /= 2;
		height /= 2;
		step *= 16;
		matrixStack.pushPose();
	        float f = 1F / 16F;
	        float f2 = 1F / 96F;
	        Tesselator tessellator = Tesselator.getInstance();
	        BufferBuilder bufferbuilder = tessellator.getBuffer();
	        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		        bufferbuilder.pos(-width,	+height,	(double)zLevel).tex(((float)(0) * f),	((float)(step + 16) * f2)).endVertex();
		        bufferbuilder.pos(+width,	+height,	(double)zLevel).tex(((float)(16) * f),	((float)(step + 16) * f2)).endVertex();
		        bufferbuilder.pos(+width,	-height,	(double)zLevel).tex(((float)(16) * f),	((float)(step) * f2)).endVertex();
		        bufferbuilder.pos(-width,	-height,	(double)zLevel).tex(((float)(0) * f),	((float)(step) * f2)).endVertex();
	        tessellator.draw();
        matrixStack.popPose();
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
