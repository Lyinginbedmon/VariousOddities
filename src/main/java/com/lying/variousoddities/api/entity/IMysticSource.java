package com.lying.variousoddities.api.entity;

/**
 * Interface class describing an EntityLivingBase that provides power to the inscribing table for high-level inscribing
 * @author Lying
 *
 */
public interface IMysticSource
{
	/** Returns true if the mob can currently provide power */ 
	public boolean canProvidePower();
	
	/** Returns the total power available from this mob */
	public int getTotalPower();
	
	/** Sets the mob to recharge by default */
	public default void usePower(int par1Int){ setRecharge(); }
	
	/** Sets the mob to recharge its power */
	public void setRecharge();
}
