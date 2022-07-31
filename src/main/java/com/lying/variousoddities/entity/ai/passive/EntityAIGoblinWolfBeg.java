package com.lying.variousoddities.entity.ai.passive;

import java.util.EnumSet;

import com.lying.variousoddities.entity.AbstractGoblinWolf;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags.Items;

public class EntityAIGoblinWolfBeg extends Goal
{
	private final AbstractGoblinWolf wolf;
	private Player player;
	private final Level world;
	private final float minDist;
	private int timeout;
	private final TargetingConditions playerPredicate;
	
	public EntityAIGoblinWolfBeg(AbstractGoblinWolf wolfIn, float minDistance)
	{
		this.wolf = wolfIn;
		this.world = wolfIn.getLevel();
		this.minDist = minDistance;
		this.playerPredicate = TargetingConditions.forNonCombat().range((double)minDistance);
		this.setFlags(EnumSet.of(Goal.Flag.LOOK));
	}
	
	public boolean canUse()
	{
		this.player = this.world.getNearestPlayer(this.playerPredicate, this.wolf);
		return this.player == null ? false : this.hasTemptationItemInHand(this.player);
	}
	
	public boolean canContinueToUse()
	{
		if(!this.player.isAlive())
			return false;
		else if(this.wolf.distanceToSqr(this.player) > (this.minDist * this.minDist))
			return false;
		else
			return this.timeout > 0 && this.hasTemptationItemInHand(this.player);
	}
	
	public void startExecuting()
	{
		this.wolf.setBegging(true);
		this.timeout = 40 + this.wolf.getRandom().nextInt(40);
	}
	
	public void resetTask()
	{
		this.wolf.setBegging(false);
		this.player = null;
	}
	
	private boolean hasTemptationItemInHand(Player player)
	{
		for(InteractionHand hand : InteractionHand.values())
		{
			ItemStack stack = player.getItemInHand(hand);
			if(this.wolf.isTame() && stack.is(Items.BONES) || this.wolf.isFoodItem(stack))
				return true;
		}
		return false;
	}

}
