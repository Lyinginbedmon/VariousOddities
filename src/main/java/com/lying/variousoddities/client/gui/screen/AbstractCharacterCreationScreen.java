package com.lying.variousoddities.client.gui.screen;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.lying.variousoddities.client.gui.menu.MenuCharacterCreation;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSpeciesSelected;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.abilities.AbilityModifierCon;
import com.lying.variousoddities.species.abilities.AbilityNaturalArmour;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.Types;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractCharacterCreationScreen<T extends MenuCharacterCreation> extends Screen implements MenuAccess<MenuCharacterCreation>
{
	public final ResourceLocation healthKey = AbilityRegistry.getClassRegistryKey(AbilityModifierCon.class).location();
	public final ResourceLocation armourKey = AbilityRegistry.getClassRegistryKey(AbilityNaturalArmour.class).location();
	
	protected final Player player;
	protected final T menu;
	
	protected final boolean randomise;
	protected final int targetPower;
	
	protected CharacterSheet sheet;
	
	protected AbstractCharacterCreationScreen(T menuIn, Component titleIn, Player thePlayer, int power, boolean random, CharacterSheet sheetIn)
	{
		super(titleIn);
		this.menu = menuIn;
		this.player = thePlayer;
		this.targetPower = power;
		this.randomise = random;
		this.sheet = sheetIn;
	}
	
	public boolean shouldCloseOnEsc(){ return false; }
	
	public boolean isPauseScreen(){ return true; }
	
	public boolean hasCustomTypes() { return sheet.hasCustomTypes(); }
	
	protected void finalise() { this.sheet.finalise(this.player); }
	
	public T getMenu() { return this.menu; }
	
	protected static class CharacterSheet
	{
		protected final Types customTypes;
		protected Species selectedSpecies = Species.HUMAN;
		protected List<Template> selectedTemplates;
		
		public CharacterSheet(EnumSet<EnumCreatureType> typesIn)
		{
			this(new Types(typesIn));
		}
		
		public CharacterSheet(@Nonnull Types customTypes)
		{
			this.customTypes = customTypes;
		}
		
		public Types customTypes() { return this.customTypes; }
		public boolean hasCustomTypes() { return !customTypes.isEmpty(); }
		public void clearCustomTypes() { this.customTypes.clear(); }
		
		public void setSpecies(Species speciesIn) { this.selectedSpecies = speciesIn; }
		public Species getSpecies() { return this.selectedSpecies; }
		
		public void addTemplate(Template templateIn) { selectedTemplates.add(templateIn); }
		public void removeTemplate(Template templateIn) { selectedTemplates.remove(templateIn); }
		public boolean hasTemplate(Template templateIn) { return selectedTemplates.contains(templateIn); }
		public void clearTemplates() { selectedTemplates.clear(); }
	    
	    protected void finalise(Player player)
	    {
	    	List<ResourceLocation> templateNames = Lists.newArrayList();
	    	if(!this.selectedTemplates.isEmpty())
	    		this.selectedTemplates.forEach((template) -> { templateNames.add(template.getRegistryName()); });
	    	
			PacketHandler.sendToServer(new PacketSpeciesSelected(player.getUUID(), this.selectedSpecies.getRegistryName(), !customTypes.isEmpty(), templateNames.toArray(new ResourceLocation[0])));
			Minecraft.getInstance().setScreen(null);
	    }
	}
}
