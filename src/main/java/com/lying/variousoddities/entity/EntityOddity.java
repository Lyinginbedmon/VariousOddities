package com.lying.variousoddities.entity;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.entity.ai.controller.EntityController;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public abstract class EntityOddity extends PathfinderMob
{
    /** A list of EntityControllers able to influence this entity */
	@SuppressWarnings("rawtypes")
	private final List<EntityController> controllers;
	/** The current EntityController (if any) actively influencing this entity's behaviour */
	@SuppressWarnings("rawtypes")
	private EntityController activeController = null;
	
	@SuppressWarnings("rawtypes")
	protected EntityOddity(EntityType<? extends EntityOddity> type, Level worldIn)
	{
		super(type, worldIn);
		
        this.controllers = new ArrayList<EntityController>();
        addControllers();
	}

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
        		.add(Attributes.MAX_HEALTH, 20.0D)
        		.add(Attributes.MOVEMENT_SPEED, (double)0.275F);
    }
	
	/**
	 * Adds all EntityControllers to the list of controllers for this entity.<br>
	 * EntityControllers contain clusters of AI tasks to be executed under corresponding contexts.
	 */
    protected void addControllers(){ }
	
	@SuppressWarnings("rawtypes")
	protected void addController(EntityController controllerIn)
	{
		if(this.controllers == null)
			return;
		
		this.controllers.add(controllerIn);
	}
	
	@SuppressWarnings("rawtypes")
	protected void customServerAiStep()
	{
		super.customServerAiStep();
		
		if(!this.controllers.isEmpty())
		{
			// Update current active controller, if any
			if(this.activeController != null)
			{
				this.activeController.updateController();
				if(!this.activeController.shouldStayActive())
					this.activeController = null;
			}
			
			// Find highest-priority active controller
			EntityController nextController = null;
			for(EntityController controller : this.controllers)
				if(controller.shouldActivate() && (nextController == null || controller.priority() < nextController.priority()))
					nextController = controller;
			
			// If controller != current controller, deactivate latter (if necessary) and activate former
			if(this.activeController != nextController)
			{
				if(this.activeController != null)
					this.activeController.clearBehaviours();
				this.activeController = nextController;
			}
		}
	}
}
