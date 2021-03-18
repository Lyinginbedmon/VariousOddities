package com.lying.variousoddities.entity.ai.passive;

import java.util.EnumSet;

import com.lying.variousoddities.entity.AbstractGoblinWolf;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EntityAIGoblinWolfBeg extends Goal
{
	private final AbstractGoblinWolf wolf;
	private PlayerEntity player;
	private final World world;
	private final float minDist;
	private int timeout;
	private final EntityPredicate playerPredicate;
	
	public EntityAIGoblinWolfBeg(AbstractGoblinWolf wolfIn, float minDistance)
	{
		this.wolf = wolfIn;
		this.world = wolfIn.getEntityWorld();
		this.minDist = minDistance;
		this.playerPredicate = (new EntityPredicate()).setDistance((double)minDistance).allowInvulnerable().allowFriendlyFire().setSkipAttackChecks();
		this.setMutexFlags(EnumSet.of(Goal.Flag.LOOK));
	}
	
	public boolean shouldExecute()
	{
		this.player = this.world.getClosestPlayer(this.playerPredicate, this.wolf);
		return this.player == null ? false : this.hasTemptationItemInHand(this.player);
	}
	
	public boolean shouldContinueExecuting()
	{
		if(!this.player.isAlive())
			return false;
		else if(this.wolf.getDistanceSq(this.player) > (this.minDist * this.minDist))
			return false;
		else
			return this.timeout > 0 && this.hasTemptationItemInHand(this.player);
	}
	
	public void startExecuting()
	{
		this.wolf.setBegging(true);
		this.timeout = 40 + this.wolf.getRNG().nextInt(40);
	}
	
	public void resetTask()
	{
		this.wolf.setBegging(false);
		this.player = null;
	}
	
	private boolean hasTemptationItemInHand(PlayerEntity player)
	{
		for(Hand hand : Hand.values())
		{
			ItemStack stack = player.getHeldItem(hand);
			if(this.wolf.isTamed() && stack.getItem() == Items.BONE || this.wolf.isFoodItem(stack))
				return true;
		}
		return false;
	}

}
