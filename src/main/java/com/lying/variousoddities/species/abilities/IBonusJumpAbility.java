package com.lying.variousoddities.species.abilities;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public interface IBonusJumpAbility
{
	/** Returns the rate at which bonus jumps return after usage */
	public int getRate();
	
	/** Returns true if this ability could provide bonus jumps in this context */
	public boolean isValid(LivingEntity entity, World world);
	
	/**
	 * Returns the specific type of jump this ability provides.<br>
	 * This allows Abilities to determine which if any is in control.<br>
	 * Different types of bonus jump also function differently.
	 */
	public JumpType jumpType();
	
	public static enum JumpType
	{
		WATER,
		AIR;
	}
}
