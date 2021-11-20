package com.lying.variousoddities.client.gui;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSpeciesSelected;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.Types;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ScreenSelectTemplates extends Screen
{
	private final PlayerEntity player;
	private final Types customTypes;
	private final Species baseSpecies;
	
	private final Map<ResourceLocation, Ability> baseAbilities;
	
	private List<Template> appliedTemplates = Lists.newArrayList();
	
	private final int power;
	
	public ScreenSelectTemplates(PlayerEntity playerIn, Species speciesIn, EnumSet<EnumCreatureType> customTypesIn, int powerIn)
	{
		super(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".templates_select"));
		this.player = playerIn;
		this.power = powerIn;
		this.customTypes = customTypesIn.isEmpty() ? null : new Types(customTypesIn);
		this.baseSpecies = speciesIn;
		
		baseAbilities = getBaseAbilities();
	}
	
	private Map<ResourceLocation, Ability> getBaseAbilities()
	{
		Map<ResourceLocation, Ability> abilityMap = new HashMap<>();
		
		if(customTypes != null)
			customTypes.addAbilitiesToMap(abilityMap);
		else if(baseSpecies != null)
			baseSpecies.getTypes().addAbilitiesToMap(abilityMap);
		
		if(baseSpecies != null)
			baseSpecies.getAbilities().forEach((ability) -> { abilityMap.put(ability.getMapName(), ability); });
		
		return abilityMap;
	}
	
	private EnumSet<EnumCreatureType> getBaseTypes()
	{
		return customTypes == null ? baseSpecies.getTypes().asSet() : this.customTypes.asSet();
	}
	
	public int totalPower()
	{
		int tally = this.baseSpecies == null ? 0 : this.baseSpecies.getPower();
		for(Template template : this.appliedTemplates)
			tally += template.getPower();
		return tally;
	}
	
	public boolean testTemplate(Template templateIn)
	{
		EnumSet<EnumCreatureType> types = getBaseTypes();
		appliedTemplates.forEach((template) -> { template.applyTypeOperations(types); });
		
		Map<ResourceLocation, Ability> abilities = this.baseAbilities;
		appliedTemplates.forEach((template) -> { template.applyAbilityOperations(abilities); });
		
		return templateIn.isApplicableTo(this.player, types, abilities);
	}
	
	/** Returns a list of templates applicable to the current set and within the power budget */
	public List<Template> getViableTemplates()
	{
		int currentPower = totalPower();
		List<Template> templates = Lists.newArrayList();
		for(Template template : VORegistries.TEMPLATES.values())
			if(currentPower + template.getPower() <= this.power && testTemplate(template))
				templates.add(template);
		return templates;
	}
	
	/** Replaces the current template selection with a randomised set */
	public void randomise()
	{
		this.appliedTemplates.clear();
		Random rand = new Random(System.currentTimeMillis());
		
		List<Template> options = getViableTemplates();
		while(totalPower() < this.power && !options.isEmpty())
		{
			this.appliedTemplates.add(options.get(rand.nextInt(options.size())));
			options = getViableTemplates();
		}
	}
	
    public void init(Minecraft minecraft, int width, int height)
    {
    	super.init(minecraft, width, height);
        this.buttons.clear();
        
        int midX = width / 2;
        
//    	this.addButton(new Button(midX + 100, 120, 20, 20, new StringTextComponent(">"), (button) -> 
//    		{
//    			index = ++index % selectableSpecies.size();
//    		}));
//    	
//    	this.addButton(new Button(midX - 120, 120, 20, 20, new StringTextComponent("<"), (button) -> 
//    		{
//    			index--;
//    			if(index < 0)
//    				index = selectableSpecies.size() - 1;
//    		}));
    	
    	this.addButton(new Button(midX - 50, 35, 100, 20, new StringTextComponent("Select"), (button) -> 
    		{
    			// Complete character creation
    			PacketHandler.sendToServer(new PacketSpeciesSelected(player.getUniqueID(), this.baseSpecies.getRegistryName(), this.customTypes != null));
    			Minecraft.getInstance().displayGuiScreen(null);
    		}));
    	
    	this.addButton(new Button(midX + 100, 35, 20, 20, new StringTextComponent("X"), (button) -> 
    	{
			PacketHandler.sendToServer(new PacketSpeciesSelected(player.getUniqueID()));
			Minecraft.getInstance().displayGuiScreen(null);
    	}, (button,matrix,x,y) -> { renderTooltip(matrix, new TranslationTextComponent("gui.varodd.species_select.exit"), x, y); }));
    }
}
