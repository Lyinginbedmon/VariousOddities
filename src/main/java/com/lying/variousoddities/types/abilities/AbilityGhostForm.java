package com.lying.variousoddities.types.abilities;

import com.lying.variousoddities.api.event.GatherAbilitiesEvent;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityGhostForm extends ActivatedAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "ghost_form");
	
	private boolean isActive = false;
	
	public AbilityGhostForm()
	{
		super(Reference.Values.TICKS_PER_SECOND * 10);
	}
	
	public ResourceLocation getRegistryName(){ return REGISTRY_NAME; }
	
	public Type getType(){ return Ability.Type.UTILITY; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		super.writeToNBT(compound);
		compound.putBoolean("Active", this.isActive);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		super.readFromNBT(compound);
		this.isActive = compound.getBoolean("Active");
	}
	
	public void trigger(LivingEntity entity, Dist side)
	{
		if(side != Dist.CLIENT)
		{
			isActive = !isActive;
			markForUpdate(entity);
			putOnCooldown(entity);
		}
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::gatherAbilities);
	}
	
	public void gatherAbilities(GatherAbilitiesEvent event)
	{
		if(event.hasAbility(getRegistryName()))
		{
			AbilityGhostForm ghostForm = (AbilityGhostForm)event.getAbility(getRegistryName());
			if(ghostForm.isActive && !event.hasAbility(AbilityIncorporeality.REGISTRY_NAME))
				event.addAbility(AbilityRegistry.getAbility(AbilityIncorporeality.REGISTRY_NAME, new CompoundNBT()));
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Ability create(CompoundNBT compound)
		{
			AbilityGhostForm ability = new AbilityGhostForm();
			ability.readFromNBT(compound);
			return ability;
		}
	}
}
