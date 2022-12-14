package com.lying.variousoddities.species.abilities;

import java.util.Collection;

import com.lying.variousoddities.utility.VOHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public abstract class AbilityVision extends ToggledAbility
{
	protected double range;
	private double rangeMin = 0D;
	
	public AbilityVision(double rangeIn)
	{
		super();
		this.range = Math.max(4D, rangeIn);
		this.isActive = true;
	}
	
	public AbilityVision(double rangeIn, double rangeMinIn)
	{
		this(rangeIn);
		this.rangeMin = rangeMinIn;
	}
	
	public int compare(Ability abilityIn)
	{
		AbilityVision vision = (AbilityVision)abilityIn;
		return vision.range > this.range ? -1 : vision.range < this.range ? 1 : 0;
	}
	
	public Type getType(){ return Ability.Type.UTILITY; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::visionModifiers);
	}
	
	public static boolean ignoreForTargeted = true;
	
	public void visionModifiers(LivingEvent.LivingVisibilityEvent event)
	{
		if(event.getLookingEntity() != null && event.getLookingEntity() instanceof LivingEntity)
		{
			LivingEntity mob = (LivingEntity)event.getLookingEntity();
			LivingEntity entity = event.getEntity();
			
			if(ignoreForTargeted && mob instanceof Monster && ((Monster)mob).getTarget() == entity)
				return;
			
			// Light level affects visibility IF the looking entity does not have Night Vision
			if(!mob.hasEffect(MobEffects.NIGHT_VISION))
			{
				Level world = entity.getLevel();
				BlockPos eyePos = entity.blockPosition().offset(0D, entity.getEyeHeight(), 0D);
				int light = Math.max(world.getBrightness(LightLayer.BLOCK, eyePos), VOHelper.getSkyLight(eyePos, world));
				
				double mod = Math.sin(((double)(light + 5) / (double)(14 + 5)) * 1.5D);
				event.modifyVisibility(mod);
			}
			
			// Blind mobs have minimal vision capacity
			if(AbilityBlind.isMobBlind(mob))
				event.modifyVisibility(0.07D / event.getVisibilityModifier());
			
			// Vision abilities confer guaranteed vision
			if(entity.hasEffect(MobEffects.GLOWING) || canMobSeeEntity(mob, entity))
				event.modifyVisibility(1D / event.getVisibilityModifier());
			
			ignoreForTargeted = true;
		}
	}
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		super.writeToNBT(compound);
		compound.putDouble("Max", this.range);
		compound.putDouble("Min", this.rangeMin);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		super.readFromNBT(compound);
		this.range = compound.getDouble("Max");
		this.rangeMin = compound.getDouble("Min");
	}
	
	public boolean isInRange(double range){ return isActive() && range <= this.range && range >= this.rangeMin; }
	
	public abstract boolean testEntity(Entity entity, LivingEntity owner);
	
	public static Collection<AbilityVision> getVisionAbilities(LivingEntity entity)
	{
		return AbilityRegistry.getAbilitiesOfClass(entity, AbilityVision.class);
	}
	
	public static boolean canMobSeeEntity(LivingEntity mob, LivingEntity entity)
	{
		for(Ability vision : getVisionAbilities(mob))
			if((vision.isActive() || mob.getType() != EntityType.PLAYER) && ((AbilityVision)vision).testEntity(entity, mob))
				return true;
		
		return false;
	}
}
