package com.lying.variousoddities.entity.ai.controller;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.ai.EntityAITargetHostileFaction;
import com.lying.variousoddities.entity.ai.passive.EntityAIKoboldGuardEgg;
import com.lying.variousoddities.entity.ai.passive.EntityAIKoboldMate;
import com.lying.variousoddities.entity.ai.passive.EntityAIKoboldParade;
import com.lying.variousoddities.entity.ai.passive.EntityAIKoboldPlaceTorch;
import com.lying.variousoddities.entity.passive.EntityKobold;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;

public class ControllerKobold extends EntityController<EntityKobold>
{
	protected final EntityKobold theKobold;
	
	protected ControllerKobold(int priorityIn, EntityKobold par1Entity, Predicate<EntityKobold> predicate)
	{
		super(priorityIn, par1Entity, predicate);
		
		this.theKobold = par1Entity;
		
		addBehaviour(0, new FloatGoal(par1Entity));
		addBehaviour(3, par1Entity.getOperateRoomTask());
		addBehaviour(6, new WaterAvoidingRandomStrollGoal(par1Entity, 1.0D));
		
		if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(VOEntities.KOBOLD))
			addBehaviour(2, new HurtByTargetGoal(par1Entity));
	}
	
	public static class ControllerKoboldChild extends ControllerKobold
	{
		public ControllerKoboldChild(int priorityIn, EntityKobold par1Entity)
		{
			super(priorityIn, par1Entity, new Predicate<EntityKobold>()
				{
					public boolean apply(EntityKobold input){ return input.isBaby(); }
				});
			
			addBehaviour(2, new EntityAIKoboldParade(par1Entity, 0.20999999046325684D));
		}
	}
	
	public static class ControllerKoboldAdult extends ControllerKobold
	{
		public ControllerKoboldAdult(int priorityIn, EntityKobold par1Entity)
		{
			super(priorityIn, par1Entity, Predicates.alwaysTrue());
			
			addBehaviour(6, new EntityAIKoboldMate(par1Entity));
			addBehaviour(2, new EntityAIKoboldParade(par1Entity, 0.20999999046325684D));
			
			if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(VOEntities.KOBOLD))
				addBehaviour(2, new EntityAITargetHostileFaction(par1Entity, true));
		}
		
		public void applyBehaviours()
		{
			super.applyBehaviours();
			this.theKobold.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
			this.theKobold.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(16.0D);
		}
	}
	
	public static class ControllerKoboldGuardian extends ControllerKobold
	{
		public ControllerKoboldGuardian(int priorityIn, EntityKobold par1Entity)
		{
			super(priorityIn, par1Entity, new Predicate<EntityKobold>()
			{
				public boolean apply(EntityKobold input)
				{
					return !input.isBaby() && input.isHatcheryGuardian();
				}
			});
			
			addBehaviour(3, new EntityAIKoboldGuardEgg(par1Entity));
			addBehaviour(5, new EntityAIKoboldPlaceTorch(par1Entity));
			
			if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(VOEntities.KOBOLD))
				addBehaviour(2, new EntityAITargetHostileFaction(par1Entity, true));
		}
		
		public void applyBehaviours()
		{
			super.applyBehaviours();
			this.theKobold.getAttribute(Attributes.MAX_HEALTH).setBaseValue(25.0D);
			this.theKobold.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(35.0D);
		}
	}
}
