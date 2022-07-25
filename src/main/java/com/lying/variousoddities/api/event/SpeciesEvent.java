package com.lying.variousoddities.api.event;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.SpeciesRegistry;
import com.lying.variousoddities.species.Template;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;

public abstract class SpeciesEvent extends LivingEvent
{
	public SpeciesEvent(LivingEntity entity)
	{
		super(entity);
	}
	
	public static class SpeciesSelected extends SpeciesEvent
	{
		private final ResourceLocation species;
		private final ResourceLocation[] templates;
		
		public SpeciesSelected(Player player, ResourceLocation speciesIn, ResourceLocation... templatesIn)
		{
			super(player);
			this.species = speciesIn;
			this.templates = templatesIn;
		}
		
		public Species getSpecies() { return SpeciesRegistry.getSpecies(species); }
		public List<Template> getTemplates()
		{
			List<Template> templateList = Lists.newArrayList();
			for(ResourceLocation template : templates)
			{
				Template var = VORegistries.TEMPLATES.get(template);
				if(var != null)
					templateList.add(var);
			}
			return templateList;
		}
	}
	
	public static class TemplateApplied extends SpeciesEvent
	{
		private final ResourceLocation template;
		
		public TemplateApplied(Player player, ResourceLocation templateIn)
		{
			super(player);
			this.template = templateIn;
		}
		
		public ResourceLocation getTemplate() { return this.template; }
	}
}
