package com.lying.variousoddities.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.world.phys.Vec3;

public class RenderUtils
{
	private static RenderType MATTE_COLOUR;
	
	public static RenderType getMatteColour()
	{
		if(MATTE_COLOUR == null)
		{
			RenderState.WriteMaskState mask = new RenderState.WriteMaskState(true, true);
			RenderState.TargetState target = new RenderState.TargetState("weather_target", () -> {
			      if (Minecraft.isFabulousGraphicsEnabled()) {
			          Minecraft.getInstance().worldRenderer.func_239231_t_().bindFramebuffer(false);
			       }

			    }, () -> {
			       if (Minecraft.isFabulousGraphicsEnabled()) {
			          Minecraft.getInstance().getFramebuffer().bindFramebuffer(false);
			       }

			    });
			
			MATTE_COLOUR = RenderType.makeType("matte_colour", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true, 
					RenderType.State.getBuilder().
						writeMask(mask).
						target(target).
						shadeModel(new RenderState.ShadeModelState(true)).build(false));
		}
		
		return MATTE_COLOUR;
	}
	
	public static void drawLine(PoseStack matrixStack, Vec3 posA, Vec3 posB, Vec3 eyePos, float red, float green, float blue, float alpha, double height)
	{
		drawLine(matrixStack, posA, posB, eyePos, red, green, blue, alpha, alpha, height, height);
	}
	
	public static void drawLine(PoseStack matrixStack, Vec3 posA, Vec3 posB, Vec3 eyePos, float red, float green, float blue, float alpha, Vec3 height)
	{
		drawLine(matrixStack, posA, posB, eyePos, red, green, blue, alpha, alpha, height, height);
	}
	
	public static void drawLine(PoseStack matrixStack, Vec3 posA, Vec3 posB, Vec3 eyePos, float red, float green, float blue, float startAlpha, float endAlpha, double heightA, double heightB)
	{
		drawLine(matrixStack, posA, posB, eyePos, red, green, blue, startAlpha, endAlpha, new Vec3(0, heightA, 0), new Vec3(0, heightB, 0));
	}
	
	public static void drawLine(PoseStack matrixStack, Vec3 posA, Vec3 posB, Vec3 viewVec, float red, float green, float blue, float startAlpha, float endAlpha, Vec3 heightVecA, Vec3 heightVecB)
	{
        BufferBuilder buffer = Tesselator.getInstance().getBuffer();
        matrixStack.pushPose();
	    	RenderSystem.enableBlend();
	    	RenderSystem.disableTexture();
	    	RenderSystem.defaultBlendFunc();
        	posA = posA.subtract(viewVec);
	    	posB = posB.subtract(viewVec);
    		
    		drawLineToBuffer(matrixStack, buffer, posA, heightVecA, posB, heightVecB, red, green, blue, startAlpha, endAlpha);
    		drawLineToBuffer(matrixStack, buffer, posB, heightVecB, posA, heightVecA, red, green, blue, endAlpha, startAlpha);
    		RenderSystem.enableTexture();
    		RenderSystem.disableBlend();
    	matrixStack.popPose();
	}
	
	private static void drawLineToBuffer(PoseStack matrixStack, BufferBuilder buffer, Vec3 posA, Vec3 heightVecA, Vec3 posB, Vec3 heightVecB, float red, float green, float blue, float startAlpha, float endAlpha)
	{
		Matrix4f matrix = matrixStack.getLast().getMatrix();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    		buffer.vertex(matrix, (float)(posA.x - heightVecA.x),	(float)(posA.y - heightVecA.y),	(float)(posA.z - heightVecA.z)).color(red, green, blue, startAlpha).endVertex();
    		buffer.vertex(matrix, (float)(posA.x + heightVecA.x),	(float)(posA.y + heightVecA.y),	(float)(posA.z + heightVecA.z)).color(red, green, blue, startAlpha).endVertex();
    		buffer.vertex(matrix, (float)(posB.x + heightVecB.x),	(float)(posB.y + heightVecB.y),	(float)(posB.z + heightVecB.z)).color(red, green, blue, endAlpha).endVertex();
    		buffer.vertex(matrix, (float)(posB.x - heightVecB.x),	(float)(posB.y - heightVecB.y),	(float)(posB.z - heightVecB.z)).color(red, green, blue, endAlpha).endVertex();
		buffer.finishDrawing();
		WorldVertexBufferUploader.draw(buffer);
	}
	
	public static void drawCube(PoseStack matrixStack, Vec3 pos, Vec3 eyePos, float red, float green, float blue, float alpha, double size)
	{
		drawCube(matrixStack, pos, eyePos, red, green, blue, alpha, size, size, size);
	}
	
	public static void drawCube(PoseStack matrixStack, Vec3 pos, Vec3 eyePos, float red, float green, float blue, float alpha, double sizeX, double sizeY, double sizeZ)
	{
		sizeX *= 0.5D;
		sizeY *= 0.5D;
		sizeZ *= 0.5D;
        matrixStack.pushPose();
	        	Vec3 min = new Vec3(-sizeX, -sizeY, -sizeZ);
	        	Vec3 max = new Vec3(sizeX, sizeY, sizeZ);
	        	min = min.add(pos);
	        	max = max.add(pos);
	        	
	        	Vec3 a1b1c1 = min;
	        	Vec3 a2b1c1 = new Vec3(max.x, min.y, min.z);
	        	Vec3 a2b2c1 = new Vec3(max.x, max.y, min.z);
	        	Vec3 a1b2c1 = new Vec3(min.x, max.y, min.z);
	        	
	        	Vec3 a1b1c2 = new Vec3(min.x, min.y, max.z);
	        	Vec3 a2b1c2 = new Vec3(max.x, min.y, max.z);
	        	Vec3 a2b2c2 = max;
	        	Vec3 a1b2c2 = new Vec3(min.x, max.y, max.z);
	        	
	        	// All a1s
	        	drawSquarePlane(matrixStack, a1b1c2, a1b2c2, a1b2c1, a1b1c1, eyePos,red, green, blue, alpha);
	        	// All a2s
	        	drawSquarePlane(matrixStack, a2b1c1, a2b2c1, a2b2c2, a2b1c2, eyePos,red, green, blue, alpha);
	        	// All c1s
	        	drawSquarePlane(matrixStack, a1b2c1, a2b2c1, a2b1c1, a1b1c1, eyePos,red, green, blue, alpha);
	        	// All c2s
	        	drawSquarePlane(matrixStack, a2b1c2, a2b2c2, a1b2c2, a1b1c2, eyePos,red, green, blue, alpha);
	        	// All b1s
	        	drawSquarePlane(matrixStack, a2b1c1, a2b1c2, a1b1c2, a1b1c1, eyePos,red, green, blue, alpha);
	        	// All b2s
	        	drawSquarePlane(matrixStack, a1b2c1, a1b2c2, a2b2c2, a2b2c1, eyePos,red, green, blue, alpha);
    	matrixStack.popPose();
	}
	
	public static void drawSquarePlane(PoseStack matrixStack, Vec3 posA, Vec3 posB, Vec3 posC, Vec3 posD, Vec3 eyePos, float red, float green, float blue, float alpha)
	{
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        matrixStack.pushPose();
        	RenderSystem.enableBlend();
        	RenderSystem.disableTexture();
        	RenderSystem.defaultBlendFunc();
        	
        	posA = posA.subtract(eyePos);
	    	posB = posB.subtract(eyePos);
	    	posC = posC.subtract(eyePos);
	    	posD = posD.subtract(eyePos);
	    	
    		Matrix4f matrix = matrixStack.getLast().getMatrix();
    		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
	    		buffer.vertex(matrix, (float)(posA.x),	(float)(posA.y),	(float)(posA.z)).color(red, green, blue, alpha).endVertex();
	    		buffer.vertex(matrix, (float)(posB.x),	(float)(posB.y),	(float)(posB.z)).color(red, green, blue, alpha).endVertex();
	    		buffer.vertex(matrix, (float)(posC.x),	(float)(posC.y),	(float)(posC.z)).color(red, green, blue, alpha).endVertex();
	    		buffer.vertex(matrix, (float)(posD.x),	(float)(posD.y),	(float)(posD.z)).color(red, green, blue, alpha).endVertex();
    		buffer.finishDrawing();
    		WorldVertexBufferUploader.draw(buffer);
    		RenderSystem.enableTexture();
    		RenderSystem.disableBlend();
    	matrixStack.popPose();
	}
	
	public static void drawBoundingBox(PoseStack matrixStackIn, Vec3 min, Vec3 max, Vec3 lookVec, Vec3 eyePos, float red, float green, float blue, float alpha, float thickness, boolean highlightAxes)
	{
        matrixStackIn.pushPose();
	        double minX = min.x;
	        double minY = min.y;
	        double minZ = min.z;
	        
	        double maxX = max.x;
	        double maxY = max.y;
	        double maxZ = max.z;
	        
	        // Floor
	        Vec3 thick = lookVec.scale(thickness).rotatePitch(90F).rotateYaw(90F);
	        RenderUtils.drawLine(matrixStackIn, new Vec3(minX, minY, minZ), new Vec3(maxX, minY, minZ), eyePos, red, highlightAxes ? 0F : green, highlightAxes ? 0F : blue, alpha, thick);
	        RenderUtils.drawLine(matrixStackIn, new Vec3(minX, minY, minZ), new Vec3(minX, minY, maxZ), eyePos, highlightAxes ? 0F : red, highlightAxes ? 0F : green, blue, alpha, thick);
	        RenderUtils.drawLine(matrixStackIn, new Vec3(minX, minY, maxZ), new Vec3(maxX, minY, maxZ), eyePos, red, green, blue, alpha, thick);
	        RenderUtils.drawLine(matrixStackIn, new Vec3(maxX, minY, minZ), new Vec3(maxX, minY, maxZ), eyePos, red, green, blue, alpha, thick);
	        
	        // Ceiling
	        RenderUtils.drawLine(matrixStackIn, new Vec3(minX, maxY, minZ), new Vec3(maxX, maxY, minZ), eyePos, red, green, blue, alpha, thick);
	        RenderUtils.drawLine(matrixStackIn, new Vec3(minX, maxY, minZ), new Vec3(minX, maxY, maxZ), eyePos, red, green, blue, alpha, thick);
	        RenderUtils.drawLine(matrixStackIn, new Vec3(minX, maxY, maxZ), new Vec3(maxX, maxY, maxZ), eyePos, red, green, blue, alpha, thick);
	        RenderUtils.drawLine(matrixStackIn, new Vec3(maxX, maxY, minZ), new Vec3(maxX, maxY, maxZ), eyePos, red, green, blue, alpha, thick);
	        
	        // Vertical edges
	        thick = lookVec.scale(thickness).rotateYaw(90F);
	        RenderUtils.drawLine(matrixStackIn, new Vec3(minX, minY, minZ), new Vec3(minX, maxY, minZ), eyePos, highlightAxes ? 0F : red, green, highlightAxes ? 0F : blue, alpha, thick);
	        RenderUtils.drawLine(matrixStackIn, new Vec3(maxX, minY, minZ), new Vec3(maxX, maxY, minZ), eyePos, red, green, blue, alpha, thick);
	        RenderUtils.drawLine(matrixStackIn, new Vec3(minX, minY, maxZ), new Vec3(minX, maxY, maxZ), eyePos, red, green, blue, alpha, thick);
	        RenderUtils.drawLine(matrixStackIn, new Vec3(maxX, minY, maxZ), new Vec3(maxX, maxY, maxZ), eyePos, red, green, blue, alpha, thick);
        matrixStackIn.popPose();
	}
}
