package com.lying.variousoddities.client.model;

import com.mojang.math.Vector3d;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

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
	
	public static ModelPart freshRenderer(Model par1ModelBase){ return new ModelPart(par1ModelBase).setTextureSize(par1ModelBase.textureWidth,par1ModelBase.textureHeight); }
	
	public static ModelPart clonePosition(ModelPart fromModel, ModelPart toModel)
	{
		toModel.setPos(fromModel.x, fromModel.y, fromModel.z);
		return toModel;
	}
	
	public static ModelPart cloneRotation(ModelPart fromModel, ModelPart toModel)
	{
		toModel.xRot = fromModel.xRot;
		toModel.yRot = fromModel.yRot;
		toModel.zRot = fromModel.zRot;
		return toModel;
	}
	
	public static ModelPart shiftWithRotation(ModelPart par1ModelRenderer, Vector3d angle, Vector3d shift)
	{
//		Vec3d newVec = shift.rotatePitch((float)angle.x).rotateYaw((float)angle.y);
		Vector3d newVec = new Vector3d(0,0,0);
		
		par1ModelRenderer.x += newVec.x;
		par1ModelRenderer.y += newVec.y;
		par1ModelRenderer.z += newVec.z;
		
		return par1ModelRenderer;
	}
	
	public static Vector3d getAngles(ModelPart par1ModelRenderer)
	{
		return new Vector3d(par1ModelRenderer.xRot, par1ModelRenderer.yRot, par1ModelRenderer.zRot);
	}
	
	public static Vector3d getPosition(ModelPart par1ModelRenderer)
	{
		return new Vector3d(par1ModelRenderer.x, par1ModelRenderer.y, par1ModelRenderer.z);
	}
}
