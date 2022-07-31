package com.lying.variousoddities.entity;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.entity.ai.controller.EntityController;
import com.lying.variousoddities.utility.DataHelper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public abstract class EntityOddityAgeable extends AgeableMob
{
    protected static final EntityDataAccessor<Integer> AGE		= SynchedEntityData.defineId(EntityOddityAgeable.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Boolean> IN_LOVE	= SynchedEntityData.defineId(EntityOddityAgeable.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Byte> JAW_OPEN		= SynchedEntityData.defineId(EntityOddityAgeable.class, EntityDataSerializers.BYTE);
    
    /** A list of EntityControllers able to influence this entity */
	@SuppressWarnings("rawtypes")
	private final List<EntityController> controllers;
	/** The current EntityController (if any) actively influencing this entity's behaviour */
	@SuppressWarnings("rawtypes")
	private EntityController activeController = null;
	
	@SuppressWarnings("rawtypes")
	protected EntityOddityAgeable(EntityType<? extends AgeableMob> type, Level worldIn)
	{
		super(type, worldIn);
		
        this.controllers = new ArrayList<EntityController>();
        addControllers();
	}
	
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		getEntityData().define(AGE, 0);
		getEntityData().define(IN_LOVE, false);
		DataHelper.Booleans.registerBooleanByte(getEntityData(), JAW_OPEN);
	}
	
    public static AttributeSupplier.Builder createAttributes()
    {
        return EntityOddity.createAttributes();
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
	protected void updateAITasks()
	{
		super.updateAITasks();
		
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
	
	public boolean isInLove(){ return false; }
	public void setInLove(boolean par1Bool){ }
    
    public void tick()
    {
    	super.tick();
    	if(!getLevel().isClientSide)
    		getEntityData().set(AGE, getAge());
    }
    
    public void livingTick()
    {
	    super.livingTick();
	    
	    if(isInLove())
	    {
		    double d0 = this.random.nextGaussian() * 0.02D;
		    double d1 = this.random.nextGaussian() * 0.02D;
		    double d2 = this.random.nextGaussian() * 0.02D;
		    this.level.addParticle(ParticleTypes.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
	    }
    }
    
    public int getAge()
    {
    	if(getLevel().isClientSide)
    		return getEntityData().get(AGE).intValue();
    	else
    		return super.getAge();
    }

    public boolean isJawOpen(){ return DataHelper.Booleans.getBooleanByte(getEntityData(), JAW_OPEN); }
    public void setJawOpen(boolean par1Bool){ DataHelper.Booleans.setBooleanByte(getEntityData(), par1Bool, JAW_OPEN); }
}
