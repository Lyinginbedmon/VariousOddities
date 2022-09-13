package com.lying.variousoddities.client.model;

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
}
