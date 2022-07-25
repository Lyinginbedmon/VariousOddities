package com.lying.variousoddities.entity.ai.controller;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Predicate;
import com.lying.variousoddities.VariousOddities;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

public abstract class EntityController<T extends Mob>
{
	/** The priority of this controller.<br>
	 * Lower value = higher priority. */
	private final int priority;
	
	private final T theCreature;
	
	/** Predicate defining the conditions for activating this controller */
	private final Predicate<T> activationCondition;
	/** Predicate defining the conditions for this controller to remain active */
	private final Predicate<T> continueCondition;
	
	private Map<Goal, Integer> behaviours = new HashMap<>();
	private Map<Goal, Integer> behavioursTarget = new HashMap<>();
	
	private boolean isActive = false;
	
	public EntityController(int priorityIn, T par1Entity, Predicate<T> activatorIn)
	{
		this(priorityIn, par1Entity, activatorIn, activatorIn);
	}
	
	public EntityController(int priorityIn, T par1Entity, Predicate<T> activatorIn, Predicate<T> continueIn)
	{
		this.priority = priorityIn;
		this.theCreature = par1Entity;
		this.activationCondition = activatorIn;
		this.continueCondition = continueIn;
	}
	
	public int priority(){ return priority; }
	
	public boolean shouldActivate()
	{
		return activationCondition.apply(theCreature);
	}
	
	public boolean shouldStayActive()
	{
		return continueCondition.apply(theCreature);
	}
	
	public void updateController()
	{
		if(!isActive && activationCondition.apply(theCreature))
			applyBehaviours();
		else if(isActive && !continueCondition.apply(theCreature))
			clearBehaviours();
	}
	
	public void applyBehaviours()
	{
		if(this.isActive)
			return;
		
		for(Goal behaviour : this.behaviours.keySet())
			this.theCreature.goalSelector.addGoal(this.behaviours.get(behaviour), behaviour);
		
		for(Goal behaviour : this.behavioursTarget.keySet())
			this.theCreature.targetSelector.addGoal(this.behavioursTarget.get(behaviour), behaviour);
		
		this.isActive = true;
	}
	
	public void clearBehaviours()
	{
		if(!this.isActive)
			return;
		
		for(Goal behaviour : this.behaviours.keySet())
			this.theCreature.goalSelector.removeGoal(behaviour);
		
		for(Goal behaviour : this.behavioursTarget.keySet())
			this.theCreature.targetSelector.removeGoal(behaviour);
		
		this.isActive = false;
	}
	
	protected final void addBehaviour(int priorityIn, Goal behaviourIn)
	{
		if(behaviourIn == null){ VariousOddities.log.info("Tried to add a null task!"); return; }
		else if(behaviourIn instanceof TargetGoal)
			behavioursTarget.put(behaviourIn, priorityIn);
		else
			behaviours.put(behaviourIn, priorityIn);
	}
}
