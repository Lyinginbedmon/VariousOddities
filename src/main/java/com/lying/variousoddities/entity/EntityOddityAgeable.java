package com.lying.variousoddities.entity;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.entity.ai.controller.EntityController;
import com.lying.variousoddities.utility.DataHelper;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;

public abstract class EntityOddityAgeable extends AgeableEntity
{
    protected static final DataParameter<Integer> AGE		= EntityDataManager.<Integer>createKey(EntityOddityAgeable.class, DataSerializers.VARINT);
    protected static final DataParameter<Boolean> IN_LOVE	= EntityDataManager.<Boolean>createKey(EntityOddityAgeable.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Byte> JAW_OPEN		= EntityDataManager.<Byte>createKey(EntityOddityAgeable.class, DataSerializers.BYTE);
    
    /** A list of EntityControllers able to influence this entity */
	@SuppressWarnings("rawtypes")
	private final List<EntityController> controllers;
	/** The current EntityController (if any) actively influencing this entity's behaviour */
	@SuppressWarnings("rawtypes")
	private EntityController activeController = null;
	
	@SuppressWarnings("rawtypes")
	protected EntityOddityAgeable(EntityType<? extends AgeableEntity> type, World worldIn)
	{
		super(type, worldIn);
		
        this.controllers = new ArrayList<EntityController>();
        addControllers();
	}
	
	protected void registerData()
	{
		super.registerData();
		getDataManager().register(AGE, 0);
		getDataManager().register(IN_LOVE, false);
		DataHelper.Booleans.registerBooleanByte(getDataManager(), JAW_OPEN);
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return EntityOddity.getAttributes();
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
    	if(!getEntityWorld().isRemote)
    		getDataManager().set(AGE, getGrowingAge());
    }
    
    public void livingTick()
    {
	    super.livingTick();
	    
	    if(isInLove())
	    {
		    double d0 = this.rand.nextGaussian() * 0.02D;
		    double d1 = this.rand.nextGaussian() * 0.02D;
		    double d2 = this.rand.nextGaussian() * 0.02D;
		    this.world.addParticle(ParticleTypes.HEART, this.getPosXRandom(1.0D), this.getPosYRandom() + 0.5D, this.getPosZRandom(1.0D), d0, d1, d2);
	    }
    }
    
    public int getGrowingAge()
    {
    	if(getEntityWorld().isRemote)
    		return getDataManager().get(AGE).intValue();
    	else
    		return super.getGrowingAge();
    }

    public boolean isJawOpen(){ return DataHelper.Booleans.getBooleanByte(getDataManager(), JAW_OPEN); }
    public void setJawOpen(boolean par1Bool){ DataHelper.Booleans.setBooleanByte(getDataManager(), par1Bool, JAW_OPEN); }
}
