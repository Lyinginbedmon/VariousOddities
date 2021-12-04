package com.lying.variousoddities.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

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
			
			MATTE_COLOUR = RenderType.makeType("matte_colour", DefaultVertexFormats.POSITION_COLOR, 7, 256, false, true, 
					RenderType.State.getBuilder().
						writeMask(mask).
						target(target).
						shadeModel(new RenderState.ShadeModelState(true)).build(false));
		}
		
		return MATTE_COLOUR;
	}
	
	public static void drawLine(MatrixStack matrixStack, Vector3d posA, Vector3d posB, Vector3d eyePos, float red, float green, float blue, float startAlpha, float endAlpha, double heightA, double heightB)
	{
		Minecraft mc = Minecraft.getInstance();
        IRenderTypeBuffer.Impl buffers = mc.getRenderTypeBuffers().getBufferSource();
        IVertexBuilder buffer = buffers.getBuffer(getMatteColour());
		
		Vector3d heightVecA = new Vector3d(0, heightA, 0);
		Vector3d heightVecB = new Vector3d(0, heightB, 0);
        matrixStack.push();
        	posA = posA.subtract(eyePos);
	    	posB = posB.subtract(eyePos);
	    	
	    		Matrix4f matrix = matrixStack.getLast().getMatrix();
	    		buffer.pos(matrix, (float)(posA.x - heightVecA.x),	(float)(posA.y - heightVecA.y),	(float)(posA.z - heightVecA.z)).color(red, green, blue, startAlpha).endVertex();
	    		buffer.pos(matrix, (float)(posA.x + heightVecA.x),	(float)(posA.y + heightVecA.y),	(float)(posA.z + heightVecA.z)).color(red, green, blue, startAlpha).endVertex();
	    		buffer.pos(matrix, (float)(posB.x + heightVecB.x),	(float)(posB.y + heightVecB.y),	(float)(posB.z + heightVecB.z)).color(red, green, blue, endAlpha).endVertex();
	    		buffer.pos(matrix, (float)(posB.x - heightVecB.x),	(float)(posB.y - heightVecB.y),	(float)(posB.z - heightVecB.z)).color(red, green, blue, endAlpha).endVertex();
    	matrixStack.pop();
	}
	
	public static void drawCube(MatrixStack matrixStack, Vector3d pos, Vector3d eyePos, float red, float green, float blue, float alpha, double size)
	{
		drawCube(matrixStack, pos, eyePos, red, green, blue, alpha, size, size, size);
	}
	
	public static void drawCube(MatrixStack matrixStack, Vector3d pos, Vector3d eyePos, float red, float green, float blue, float alpha, double sizeX, double sizeY, double sizeZ)
	{
		sizeX *= 0.5D;
		sizeY *= 0.5D;
		sizeZ *= 0.5D;
        matrixStack.push();
	        	Vector3d min = new Vector3d(-sizeX, -sizeY, -sizeZ);
	        	Vector3d max = new Vector3d(sizeX, sizeY, sizeZ);
	        	min = min.add(pos);
	        	max = max.add(pos);
	        	
	        	Vector3d a1b1c1 = min;
	        	Vector3d a2b1c1 = new Vector3d(max.x, min.y, min.z);
	        	Vector3d a2b2c1 = new Vector3d(max.x, max.y, min.z);
	        	Vector3d a1b2c1 = new Vector3d(min.x, max.y, min.z);
	        	
	        	Vector3d a1b1c2 = new Vector3d(min.x, min.y, max.z);
	        	Vector3d a2b1c2 = new Vector3d(max.x, min.y, max.z);
	        	Vector3d a2b2c2 = max;
	        	Vector3d a1b2c2 = new Vector3d(min.x, max.y, max.z);
	        	
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
    	matrixStack.pop();
	}
	
	public static void drawSquarePlane(MatrixStack matrixStack, Vector3d posA, Vector3d posB, Vector3d posC, Vector3d posD, Vector3d eyePos, float red, float green, float blue, float alpha)
	{
		Minecraft mc = Minecraft.getInstance();
        IRenderTypeBuffer.Impl buffers = mc.getRenderTypeBuffers().getBufferSource();
        IVertexBuilder buffer = buffers.getBuffer(getMatteColour());
        matrixStack.push();
        	posA = posA.subtract(eyePos);
	    	posB = posB.subtract(eyePos);
	    	posC = posC.subtract(eyePos);
	    	posD = posD.subtract(eyePos);
	    	
    		Matrix4f matrix = matrixStack.getLast().getMatrix();
    		buffer.pos(matrix, (float)(posA.x),	(float)(posA.y),	(float)(posA.z)).color(red, green, blue, alpha).endVertex();
    		buffer.pos(matrix, (float)(posB.x),	(float)(posB.y),	(float)(posB.z)).color(red, green, blue, alpha).endVertex();
    		buffer.pos(matrix, (float)(posC.x),	(float)(posC.y),	(float)(posC.z)).color(red, green, blue, alpha).endVertex();
    		buffer.pos(matrix, (float)(posD.x),	(float)(posD.y),	(float)(posD.z)).color(red, green, blue, alpha).endVertex();
    	matrixStack.pop();
	}
}
