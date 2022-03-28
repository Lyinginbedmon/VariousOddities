package com.lying.variousoddities.species.abilities;

import java.util.Collection;

import com.lying.variousoddities.utility.VOHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public abstract class AbilityVision extends ToggledAbility
{
	protected double range;
	private double rangeMin = 0D;
	
	public AbilityVision(ResourceLocation registryName, double rangeIn)
	{
		super(registryName);
		this.range = Math.max(4D, rangeIn);
		this.isActive = true;
	}
	
	public AbilityVision(ResourceLocation registryName, double rangeIn, double rangeMinIn)
	{
		this(registryName, rangeIn);
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
	
	public void visionModifiers(LivingEvent.LivingVisibilityEvent event)
	{
		if(event.getLookingEntity() != null && event.getLookingEntity() instanceof LivingEntity)
		{
			LivingEntity mob = (LivingEntity)event.getLookingEntity();
			LivingEntity entity = event.getEntityLiving();
			
			// Light level affects visibility IF the looking entity does not have Night Vision
			if(!mob.isPotionActive(Effects.NIGHT_VISION))
			{
				World world = entity.getEntityWorld();
				BlockPos eyePos = entity.getPosition().add(0D, entity.getEyeHeight(), 0D);
				int light = Math.max(world.getLightFor(LightType.BLOCK, eyePos), VOHelper.getSkyLight(eyePos, world));
				
				double mod = Math.sin(((double)(light + 5) / (double)(14 + 5)) * 1.5D);
				event.modifyVisibility(mod);
			}
			
			// Blind mobs have minimal vision capacity
			if(mob.isPotionActive(Effects.BLINDNESS) || AbilityRegistry.hasAbility(mob, AbilityBlind.REGISTRY_NAME))
				event.modifyVisibility(0.07D / event.getVisibilityModifier());
			
			// Vision abilities confer guaranteed vision
			if(entity.isPotionActive(Effects.GLOWING) || canMobSeeEntity(mob, entity))
				event.modifyVisibility(1D / event.getVisibilityModifier());
		}
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		super.writeToNBT(compound);
		compound.putDouble("Max", this.range);
		compound.putDouble("Min", this.rangeMin);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		super.readFromNBT(compound);
		this.range = compound.getDouble("Max");
		this.rangeMin = compound.getDouble("Min");
	}
	
	public boolean isInRange(double range){ return isActive() && range <= this.range && range >= this.rangeMin; }
	
	public abstract boolean testEntity(Entity entity, LivingEntity owner);
	
	public static Collection<AbilityVision> getVisionAbilities(LivingEntity entity)
	{
		return AbilityRegistry.getAbilitiesOfType(entity, AbilityVision.class);
	}
	
	public static boolean canMobSeeEntity(LivingEntity mob, LivingEntity entity)
	{
		for(Ability vision : getVisionAbilities(mob))
			if((vision.isActive() || mob.getType() != EntityType.PLAYER) && ((AbilityVision)vision).testEntity(entity, mob))
				return true;
		
		return false;
	}
}
