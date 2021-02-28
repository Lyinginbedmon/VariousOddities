package com.lying.variousoddities.entity.ai.controller;

import com.google.common.base.Predicate;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.ai.EntityAITargetHostileFaction;
import com.lying.variousoddities.entity.ai.hostile.EntityAIGoblinFlee;
import com.lying.variousoddities.entity.ai.hostile.EntityAIGoblinMate;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.entity.hostile.EntityGoblin.GoblinType;
import com.lying.variousoddities.entity.passive.EntityKobold;

import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;

public class ControllerGoblin
{
	public static class ControllerGoblinChild extends EntityController<EntityGoblin>
	{
		public ControllerGoblinChild(int priorityIn, EntityGoblin par1Entity)
		{
			super(priorityIn, par1Entity, new Predicate<EntityGoblin>()
					{
						public boolean apply(EntityGoblin input)
						{
							return input.isChild();
						}
					});
			
			addBehaviour(1, new EntityAIGoblinFlee(par1Entity, 1.0D));
		}
	}
	
	public static class ControllerGoblinBasic extends EntityController<EntityGoblin>
	{
		public ControllerGoblinBasic(int priorityIn, EntityGoblin par1Entity)
		{
			this(priorityIn, par1Entity, new Predicate<EntityGoblin>()
			{
				public boolean apply(EntityGoblin input)
				{
					return input.getGoblinType() == GoblinType.BASIC;
				}
			});
		}
		
		private ControllerGoblinBasic(int priorityIn, EntityGoblin par1Entity, Predicate<EntityGoblin> activatorIn)
		{
			super(priorityIn, par1Entity, activatorIn);
			
	    	addBehaviour(6, new EntityAIGoblinMate(par1Entity));
//			addBehaviour(6, new EntityAIGoblinWorgHurt(par1Entity));
			
	        if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(par1Entity.getType()))
	        {
	    	    addBehaviour(1, new HurtByTargetGoal(par1Entity));
	    	    addBehaviour(2, new EntityAITargetHostileFaction(par1Entity, true));
	            addBehaviour(2, new NearestAttackableTargetGoal<EntityKobold>(par1Entity, EntityKobold.class, true));
	        }
		}
	}
	
	public static class ControllerGoblinWorgTamer extends ControllerGoblinBasic
	{
		public ControllerGoblinWorgTamer(int priorityIn, EntityGoblin par1Entity)
		{
			super(priorityIn, par1Entity, new Predicate<EntityGoblin>()
			{
				public boolean apply(EntityGoblin input)
				{
					return input.getGoblinType() == GoblinType.WORG_TAMER;
				}
			});
			
//			addBehaviour(6, new EntityAIGoblinWorgBreed(par1Entity));
//			addBehaviour(3, new EntityAIGoblinWorgHeal(par1Entity));
//			addBehaviour(6, new EntityAIGoblinWorgFight(par1Entity));
//			addBehaviour(6, new EntityAIGoblinWorgTame(par1Entity));
		}
	}
	
	public static class ControllerGoblinShaman extends EntityController<EntityGoblin>
	{
		public ControllerGoblinShaman(int priorityIn, EntityGoblin par1Entity, Predicate<EntityGoblin> activatorIn)
		{
			super(priorityIn, par1Entity, new Predicate<EntityGoblin>()
			{
				public boolean apply(EntityGoblin input)
				{
					return input.getGoblinType() == GoblinType.SHAMAN;
				}
			});
		}
	}
}
