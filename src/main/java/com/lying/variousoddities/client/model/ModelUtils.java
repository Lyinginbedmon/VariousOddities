package com.lying.variousoddities.client.model;

import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.vector.Vector3d;

public class ModelUtils
{
	/** 180 degrees expressed as radians */
	public static final float degree180 = (float)(Math.toRadians(180D));
	/** 90 degrees expressed as radians */
	public static final float degree90 = (float)(Math.toRadians(90D));
	/** 10 degrees expressed as radians */
	public static final float degree10 = (float)(Math.toRadians(10D));
	/** 5 degrees expressed as radians */
	public static final float degree5 = (float)(Math.toRadians(5D));
	
	/** Converts a given double from degrees to radians as a float */
	public static float toRadians(double par1Double){ return (float)(Math.toRadians(par1Double)); }
	
	public static ModelRenderer freshRenderer(Model par1ModelBase){ return new ModelRenderer(par1ModelBase).setTextureSize(par1ModelBase.textureWidth,par1ModelBase.textureHeight); }
	
	public static ModelRenderer clonePosition(ModelRenderer fromModel, ModelRenderer toModel)
	{
		toModel.setRotationPoint(fromModel.rotationPointX, fromModel.rotationPointY, fromModel.rotationPointZ);
		return toModel;
	}
	
	public static ModelRenderer cloneRotation(ModelRenderer fromModel, ModelRenderer toModel)
	{
		toModel.rotateAngleX = fromModel.rotateAngleX;
		toModel.rotateAngleY = fromModel.rotateAngleY;
		toModel.rotateAngleZ = fromModel.rotateAngleZ;
		return toModel;
	}
	
	public static ModelRenderer shiftWithRotation(ModelRenderer par1ModelRenderer, Vector3d angle, Vector3d shift)
	{
//		Vec3d newVec = shift.rotatePitch((float)angle.x).rotateYaw((float)angle.y);
		Vector3d newVec = new Vector3d(0,0,0);
		
		par1ModelRenderer.rotationPointX += newVec.x;
		par1ModelRenderer.rotationPointY += newVec.y;
		par1ModelRenderer.rotationPointZ += newVec.z;
		
		return par1ModelRenderer;
	}
	
	public static Vector3d getAngles(ModelRenderer par1ModelRenderer)
	{
		return new Vector3d(par1ModelRenderer.rotateAngleX, par1ModelRenderer.rotateAngleY, par1ModelRenderer.rotateAngleZ);
	}
	
	public static Vector3d getPosition(ModelRenderer par1ModelRenderer)
	{
		return new Vector3d(par1ModelRenderer.rotationPointX, par1ModelRenderer.rotationPointY, par1ModelRenderer.rotationPointZ);
	}
}
