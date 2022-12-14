package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.api.event.GatherAbilitiesEvent;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;

public abstract class AbilityForm extends ToggledAbility
{
	protected AbilityForm()
	{
		super(Reference.Values.TICKS_PER_SECOND * 10);
	}
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public Type getType(){ return Ability.Type.UTILITY; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::gatherAbilities);
	}
	
	public void gatherAbilities(GatherAbilitiesEvent event)
	{
		event.getAbilityMap().values().forEach((ability) -> 
		{
			if(ability instanceof AbilityForm)
			{
				AbilityForm form = (AbilityForm)event.getOriginalAbility(ability.getMapName());
				if(form != null && form.isActive)
				{
					Ability subAbility = form.getAbility();
					if(!event.hasAbility(subAbility.getMapName()))
						event.addTempAbility(subAbility);
				}
			}
		});
	}
	
	public void trigger(LivingEntity entity, Dist side)
	{
		super.trigger(entity, side);
		switch(side)
		{
			case CLIENT:
				break;
			default:
				this.markForUpdate(entity);
				break;
		}
	}
	
	protected abstract Ability getAbility();
	
	public static class Mist extends AbilityForm
	{
		public Mist(){ super(); }
		
		protected Ability getAbility(){ return new AbilityGaseous(); }
		
		public static class Builder extends Ability.Builder
		{
			public Builder(){ super(); }
			
			public Ability create(CompoundTag compound)
			{
				AbilityForm.Mist ability = new AbilityForm.Mist();
				ability.readFromNBT(compound);
				return ability;
			}
		}
	}
	
	public static class Ghost extends AbilityForm
	{
		public Ghost(){ super(); }
		
		protected Ability getAbility(){ return new AbilityIncorporeality(); }
		
		public static class Builder extends Ability.Builder
		{
			public Builder(){ super(); }
			
			public Ability create(CompoundTag compound)
			{
				AbilityForm.Ghost ability = new AbilityForm.Ghost();
				ability.readFromNBT(compound);
				return ability;
			}
		}
	}
}
