package com.lying.variousoddities.init;

import com.lying.variousoddities.condition.Condition;
import com.lying.variousoddities.condition.Conditions;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.templates.TemplateOperation;
import com.lying.variousoddities.species.templates.TemplatePrecondition;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VORegistryHandler
{
	public VORegistryHandler(){ }
	
	@SubscribeEvent
	public void initRegistries(RegistryEvent.NewRegistry event)
	{
		VORegistries.init();
	}
	
	@SubscribeEvent
    public void onRegisterAbilities(RegistryEvent.Register<Ability.Builder> event)
    {
		AbilityRegistry.onRegisterAbilities(event);
    }
	
	@SubscribeEvent
    public void onRegisterOperations(RegistryEvent.Register<TemplateOperation.Builder> event)
    {
		TemplateOperation.onRegisterOperations(event);
    }
	
	@SubscribeEvent
    public void onRegisterPreconditions(RegistryEvent.Register<TemplatePrecondition.Builder> event)
    {
		TemplatePrecondition.onRegisterPreconditions(event);
    }
	
	@SubscribeEvent
    public void onRegisterConditions(RegistryEvent.Register<Condition> event)
    {
		Conditions.onRegisterConditions(event);
    }
}
