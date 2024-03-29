package com.lying.variousoddities.utility;

import com.lying.variousoddities.VariousOddities;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;

public class DataHelper
{
	public static class Booleans
	{
		public static void setBooleanByte(SynchedEntityData par1Manager, boolean par2Boolean, EntityDataAccessor<Byte> par3Param) {
		    byte b0 = ((Byte)par1Manager.get(par3Param)).byteValue();
		    
		    if (par2Boolean)
		    {
		    	par1Manager.set(par3Param, Byte.valueOf((byte)(b0 | 2)));
		    }
		    else
		    {
		    	par1Manager.set(par3Param, Byte.valueOf((byte)(b0 & -3)));
		    }
		}
	
		public static boolean getBooleanByte(SynchedEntityData par1Manager, EntityDataAccessor<Byte> par2Param) {
		    return (((Byte)par1Manager.get(par2Param)).byteValue() & 2) != 0;
		}
	
		public static void flipBooleanByte(SynchedEntityData par1Manager, EntityDataAccessor<Byte> par2Param) {
			setBooleanByte(par1Manager, !getBooleanByte(par1Manager,par2Param), par2Param);
		}
	
		public static boolean registerBooleanByte(SynchedEntityData par1Manager, EntityDataAccessor<Byte> par2Param) {
		    par1Manager.define(par2Param, Byte.valueOf((byte)0));
		    return par1Manager.get(par2Param) != null;
		}
	
		public static boolean registerBooleanByte(SynchedEntityData par1Manager, EntityDataAccessor<Byte> par2Param, boolean par3Bool) {
			boolean val = registerBooleanByte(par1Manager, par2Param);
			if(val){ setBooleanByte(par1Manager, par3Bool, par2Param); }
			else{ VariousOddities.log.warn("Failed to define a data parameter! Things might break!"); }
			return val;
		}
	}
	
	public static class Integers
	{
		public static void setInteger(SynchedEntityData par1Manager, int par2Int, EntityDataAccessor<Byte> par3Param)
		{
	        byte b0 = ((Byte)par1Manager.get(par3Param)).byteValue();
	        par1Manager.set(par3Param, Byte.valueOf((byte)(b0 & 240 | par2Int & 15)));
		}
		
		public static int getInteger(SynchedEntityData par1Manager, EntityDataAccessor<Byte> par2Param)
		{
			return (((Byte)par1Manager.get(par2Param)).byteValue() & 15);
		}
		
		public static boolean registerInteger(SynchedEntityData par1Manager, EntityDataAccessor<Byte> par2Param)
		{
			par1Manager.define(par2Param, Byte.valueOf((byte)0));
		    return par1Manager.get(par2Param) != null;
		}
		
		public static boolean registerInteger(SynchedEntityData par1Manager, EntityDataAccessor<Byte> par2Param, int par3Int)
		{
			boolean val = registerInteger(par1Manager, par2Param);
			if(val){ setInteger(par1Manager, par3Int, par2Param); }
			else{ VariousOddities.log.warn("Failed to define a data parameter! Things might break!"); }
		    return val;
		}
	}
	
	public static class Bytes
	{
		/** Returns the value of the given bit in the given value (0-indexed) */
		public static boolean getBit(int value, int index)
		{
//    		return (val & (1 << n) >> 0) == 1; // Flying's original code
			return (((byte)value >> index) & 1) == 1;
		}
		
		public static int setBit(int value, int index, boolean val)
		{
			if(val)
				value |= 1 << index;
			else
				value &= ~(1 << index);
			return value;
		}
	}
}
