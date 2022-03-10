package com.lying.variousoddities.client.gui;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSpeciesSelected;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityModifier;
import com.lying.variousoddities.species.abilities.AbilityModifierCon;
import com.lying.variousoddities.species.abilities.AbilityNaturalArmour;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.templates.TemplateOperation;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.Types;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ScreenSelectTemplates extends Screen
{
	public static final ResourceLocation TEXTURES = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/templates_select.png");
	private static final Comparator<Template> TEMPLATE_SORT = new Comparator<Template>()
			{
				public int compare(Template o1, Template o2)
				{
					String name1 = o1.getDisplayName().getString();
					String name2 = o2.getDisplayName().getString();
					
					List<String> names = Arrays.asList(name1, name2);
					Collections.sort(names);
					
					int index1 = names.indexOf(name1);
					int index2 = names.indexOf(name2);
					return (index1 > index2 ? 1 : index1 < index2 ? -1 : 0);
				}
			};
	
	private final PlayerEntity player;
	private final Types customTypes;
	private final Species baseSpecies;
	
	private TemplateList listAvailable;
	private TemplateList listApplied;
	public TemplateList.TemplateListEntry highlightEntry = null;
	
	private final int targetPower;
	
	private static final int LIST_SEP = 7;
	private static final int LIST_BORDER = 6;
	
	private Button clearButton;
	
	public ScreenSelectTemplates(@Nonnull PlayerEntity playerIn, @Nonnull Species speciesIn, EnumSet<EnumCreatureType> customTypesIn, int powerIn)
	{
		super(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".templates_select"));
		this.player = playerIn;
		this.targetPower = powerIn;
		this.customTypes = customTypesIn.isEmpty() ? null : new Types(customTypesIn);
		this.baseSpecies = speciesIn;
	}
	
	public boolean shouldCloseOnEsc(){ return false; }
	
	public boolean isPauseScreen(){ return true; }
	
	private Map<ResourceLocation, Ability> getBaseAbilities()
	{
		Map<ResourceLocation, Ability> abilityMap = new HashMap<>();
		
		if(customTypes != null)
			customTypes.addAbilitiesToMap(abilityMap);
		else if(baseSpecies != null)
			baseSpecies.getTypes().addAbilitiesToMap(abilityMap);
		
		if(baseSpecies != null)
			baseSpecies.getAbilities().forEach((ability) -> { abilityMap.put(ability.getMapName(), AbilityRegistry.getAbility(ability.writeAtomically(new CompoundNBT()))); });
		
		return abilityMap;
	}
	
	private EnumSet<EnumCreatureType> getBaseTypes()
	{
		EnumSet<EnumCreatureType> types = EnumSet.noneOf(EnumCreatureType.class);
		if(customTypes == null)
			types.addAll(baseSpecies.getTypes().asSet());
		else
			types.addAll(this.customTypes.asSet());
		return types;
	}
	
	public int totalPower()
	{
		int tally = this.baseSpecies == null ? 0 : this.baseSpecies.getPower();
		if(this.listApplied != null)
			for(Template template : this.listApplied.getTemplates())
				tally += template.getPower();
		return tally;
	}
	
	public boolean testTemplate(Template templateIn)
	{
		return testTemplate(templateIn, getProcessedTemplates());
	}
	
	public boolean testTemplate(Template templateIn, Pair<Types, Map<ResourceLocation, Ability>> setIn)
	{
		return this.player.isCreative() || templateIn.isApplicableTo(this.player, setIn.getFirst().asSet(), setIn.getSecond());
	}
	
	private Pair<Types, Map<ResourceLocation, Ability>> getProcessedTemplates()
	{
		List<Template> appliedTemplates = this.listApplied == null ? Lists.newArrayList() : this.listApplied.getTemplates();
		return processTemplates(appliedTemplates.toArray(new Template[0]));
	}
	
	private Pair<Types, Map<ResourceLocation, Ability>> processTemplates(Template... templates)
	{
		EnumSet<EnumCreatureType> types = getBaseTypes();
		Map<ResourceLocation, Ability> abilities = getBaseAbilities();
		
		if(templates.length > 0)
		{
			for(Template template : templates)
			{
				template.applyTypeOperations(types);
				template.applyAbilityOperations(abilities);
			}
		}
		
		return new Pair<Types, Map<ResourceLocation, Ability>>(new Types(types), abilities);
	}
	
	/** Returns a list of templates applicable to the current set and within the power budget */
	public List<Template> getViableTemplates()
	{
		List<Template> templates = Lists.newArrayList();
		List<Template> applied = this.listApplied == null ? Lists.newArrayList() : this.listApplied.getTemplates();
		if(player.isCreative())
		{
			templates.addAll(VORegistries.TEMPLATES.values());
			templates.removeAll(applied);
		}
		else
		{
			int currentPower = totalPower();
			for(Template template : VORegistries.TEMPLATES.values())
				if(!applied.contains(template))
					if(currentPower + template.getPower() <= this.targetPower && testTemplate(template))
						templates.add(template);
		}
		
		templates.sort(TEMPLATE_SORT);
		return templates;
	}
	
	/** Adds the given template to the applied list and updates the available list */
	public void applyTemplate(Template template)
	{
		this.listApplied.addEntry(template);
		this.listAvailable.setEntries(getViableTemplates());
	}
	
	/** Removes the given template from the applied list and updates the available list */
	public void removeTemplate(Template template)
	{
		List<Template> applied = this.listApplied.getTemplates();
		applied.remove(template);
		
		// Quality control
		boolean noHits = false;
		while(!noHits)
		{
			// Iterate through applied templates, breaking when invalid found
			List<Template> tested = Lists.newArrayList();
			Template hitFound = null;
			for(Template extant : applied)
			{
				Pair<Types, Map<ResourceLocation, Ability>> atStep = processTemplates(tested.toArray(new Template[0]));
				if(testTemplate(extant, atStep))
					tested.add(extant);
				else
				{
					hitFound = extant;
					break;
				}
			}
			
			if(hitFound != null)
				applied.remove(hitFound);
			else
				noHits = true;
		}
		
		this.listApplied.setEntries(applied);
		this.listAvailable.setEntries(getViableTemplates());
	}
	
	public void clear()
	{
		this.listApplied.clear();
		this.listAvailable.setEntries(getViableTemplates());
		
	}
	
	/** Replaces the current template selection with a randomised set */
	public void randomise()
	{
		clear();
		Random rand = new Random(System.currentTimeMillis());
		
		List<Template> options = getViableTemplates();
		while((totalPower() < this.targetPower || this.player.isCreative()) && !options.isEmpty())
		{
			applyTemplate(options.get(rand.nextInt(options.size())));
			options = getViableTemplates();
		}
	}
	
    public void init(Minecraft minecraft, int width, int height)
    {
    	super.init(minecraft, width, height);
    	
		this.listAvailable = new TemplateList(minecraft, this, 200, this.height, false, new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".templates_select.allowed"));
		this.listAvailable.setLeftPos(this.width / 2 - this.listAvailable.getRowWidth() - LIST_SEP);
		this.listAvailable.setEntries(getViableTemplates());
		this.children.add(this.listAvailable);
		
		this.listApplied = new TemplateList(minecraft, this, 200, this.height, true, new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".templates_select.applied"));
		this.listApplied.setLeftPos(this.width/2 + LIST_SEP);
		this.children.add(this.listApplied);
		
		if(this.listAvailable.getTemplates().isEmpty() && this.listApplied.getTemplates().isEmpty())
		{
			/* If there are no applicable templates at initialisation, conclude character creation outright */
			finalise();
			return;
		}
		
        this.buttons.clear();
        int midX = width / 2;
    	this.addButton(new Button(midX - 50, this.height - 22, 100, 20, new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".templates_select.finalise"), (button) -> { this.finalise(); }));
    	this.addButton(new Button(midX - 62, this.height - 44, 60, 20, new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".templates_select.randomise"), (button) -> { this.randomise(); }));
    	this.addButton(clearButton = new Button(midX + 2, this.height - 44, 60, 20, new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".templates_select.clear"), (button) -> { this.clear(); }));
    	this.addButton(new Button(3, 3, 20, 20, new StringTextComponent("<"), (button) -> { Minecraft.getInstance().displayGuiScreen(new ScreenSelectSpecies(player, this.baseSpecies)); },
    			(button,matrix,x,y) -> { renderTooltip(matrix, new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".species_select"), x, y); }));
    }
    
    public void tick()
    {
    	this.clearButton.active = !this.listApplied.getTemplates().isEmpty();
    }
    
    private void finalise()
    {
    	List<ResourceLocation> templateNames = Lists.newArrayList();
    	this.listApplied.getTemplates().forEach((template) -> { templateNames.add(template.getRegistryName()); });
    	
		PacketHandler.sendToServer(new PacketSpeciesSelected(player.getUniqueID(), this.baseSpecies.getRegistryName(), this.customTypes != null, templateNames.toArray(new ResourceLocation[0])));
		Minecraft.getInstance().displayGuiScreen(null);
    }
    
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
    	renderDirtBackground(0);
		this.highlightEntry = null;
    	this.listAvailable.render(matrixStack, mouseX, mouseY, partialTicks);
    	this.listApplied.render(matrixStack, mouseX, mouseY, partialTicks);
    	hideListEdge();
    	ScreenSelectSpecies.drawListBorder(matrixStack, listAvailable, height, 0, 32, LIST_BORDER, TEXTURES);
    	ScreenSelectSpecies.drawListBorder(matrixStack, listApplied, height, 0, 32, LIST_BORDER, TEXTURES);
    	
		drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 12, 16777215);
		
		displayBaseStats(matrixStack, mouseX, mouseY, (this.width / 2) - (this.listAvailable.getWidth() + LIST_SEP + LIST_BORDER) - 2);
		displayTemplatedStats(matrixStack, mouseX, mouseY, this.width / 2 + LIST_SEP + this.listApplied.getWidth() + LIST_BORDER + 1 + 2);
		
    	super.render(matrixStack, mouseX, mouseY, partialTicks);
    	if(this.highlightEntry != null)
    		renderHighlightedEntry(matrixStack, mouseX, mouseY);
    }
    
    private void drawHealthAndArmour(MatrixStack matrixStack, int xPos, int yPos, int health, int armour)
    {
		int healthX = xPos;
		ScreenSelectSpecies.drawHUDIcon(matrixStack, healthX, yPos, 16, 0);
		ScreenSelectSpecies.drawHUDIcon(matrixStack, healthX, yPos, 52, 0);
		drawString(matrixStack, this.font, String.valueOf(health), healthX + 10, yPos, 16777215);
		
		int armourX = healthX + 10 + this.font.getStringWidth(String.valueOf((int)health)) + 5;
		ScreenSelectSpecies.drawHUDIcon(matrixStack, armourX, yPos, 34, 9);
		drawString(matrixStack, this.font, String.valueOf(armour), armourX + 10, yPos, 16777215);
    }
    
    @SuppressWarnings("deprecation")
	private void displayBaseStats(MatrixStack matrixStack, int mouseX, int mouseY, int xPos)
    {
		// Describe base creature
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(TEXTURES);
		int yPos = 20 + this.font.FONT_HEIGHT;
		this.blit(matrixStack, xPos - 100, 20 + this.font.FONT_HEIGHT, 0, 65, 100, 120);
		xPos -= 8;
		yPos += 10;
    	
		drawString(matrixStack, this.font, this.baseSpecies.getDisplayName().getString(), xPos - this.font.getStringWidth(this.baseSpecies.getDisplayName().getString()) - 2, yPos, 16777215);
		yPos += this.font.FONT_HEIGHT + 8;
		int basePower = this.baseSpecies.getPower();
		int totalStars = Math.max(1, Math.abs(basePower));
		while(totalStars > 0)
		{
			int stars = Math.min(5, totalStars);
			ScreenSelectSpecies.drawStars(matrixStack, Math.min(5, basePower), xPos - (stars * 9), yPos);
			totalStars -= stars;
			yPos += this.font.FONT_HEIGHT;
		}
		yPos += 1;
		Types baseTypes = new Types(getBaseTypes());
		ITextComponent baseTypesText = baseTypes.toHeader();
		if(baseTypesText.getString().length() > 15)
			baseTypesText = new StringTextComponent(baseTypesText.getString().substring(0, 15) + "...");
		drawString(matrixStack, this.font, baseTypesText, xPos - this.font.getStringWidth(baseTypesText.getString()), yPos, 16777215);
		yPos += this.font.FONT_HEIGHT + 5;
		Map<ResourceLocation, Ability> baseAbilities = getBaseAbilities();
		
		double health = baseTypes.getPlayerHealth();
		double armour = 0D;
		if(!baseAbilities.isEmpty())
			for(Ability ability : baseAbilities.values()) 
			{
				if(ability.getRegistryName().equals(AbilityNaturalArmour.REGISTRY_NAME))
					armour += ((AbilityNaturalArmour)ability).amount(); 
				
				if(ability.getRegistryName().equals(AbilityModifierCon.REGISTRY_NAME))
					health += ((AbilityModifier)ability).amount();
			};
		
		int hudWidth = 10 + this.font.getStringWidth(String.valueOf((int)health)) + 5 + 10 + this.font.getStringWidth(String.valueOf((int)armour));
		drawHealthAndArmour(matrixStack, xPos - hudWidth, yPos, (int)health, (int)armour);
		
		yPos += this.font.FONT_HEIGHT + 5;
		ITextComponent abilityCount = new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".templates_select.ability_tally", baseAbilities.size());
		drawString(matrixStack, this.font, abilityCount, xPos - this.font.getStringWidth(abilityCount.getString()), yPos, 16777215);
		
		if(mouseX <= xPos && mouseY <= (yPos + font.FONT_HEIGHT) && mouseY >= 29)
			renderCharacterSummary(matrixStack, mouseX, mouseY, baseTypes, baseAbilities);
    }
    
	/** Describe base creature after selected templates are applied */
    @SuppressWarnings("deprecation")
	private void displayTemplatedStats(MatrixStack matrixStack, int mouseX, int mouseY, int xPos)
    {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(TEXTURES);
		
		int yPos = 20 + this.font.FONT_HEIGHT;
		this.blit(matrixStack, xPos, yPos, 0, 65, 100, 120);
		xPos += 8;
		yPos += 10;
		
		drawString(matrixStack, this.font, this.baseSpecies.getDisplayName().getString(), xPos + 2, yPos, 16777215);
		yPos += this.font.FONT_HEIGHT + 8;
		int templatePower = this.baseSpecies.getPower();
		for(Template template : this.listApplied.getTemplates())
			templatePower += template.getPower();
		
		int totalStars = this.targetPower;
		while(Math.max(totalStars, templatePower) > 0)
		{
			if(totalStars > 0)
			{
				ScreenSelectSpecies.drawStars(matrixStack, Math.min(5, totalStars), xPos, yPos, 0.5F, 0.5F, 0.5F);
				totalStars -= Math.min(5, totalStars);
			}
			
			if(templatePower > 0)
			{
				ScreenSelectSpecies.drawStars(matrixStack, Math.min(5, templatePower), xPos, yPos);
				templatePower -= Math.min(5, templatePower);
			}
			
			yPos += this.font.FONT_HEIGHT;
		}
		yPos += 1;
		Pair<Types, Map<ResourceLocation, Ability>> selection = getProcessedTemplates();
		ITextComponent selectionTypesText = selection.getFirst().toHeader();
		if(selectionTypesText.getString().length() > 15)
			selectionTypesText = new StringTextComponent(selectionTypesText.getString().substring(0, 15) + "...");
		drawString(matrixStack, this.font, selectionTypesText, xPos, yPos, 16777215);
		yPos += this.font.FONT_HEIGHT + 5;
		
		// Display health and armour total
		double health = selection.getFirst().getPlayerHealth();
		double armour = 0D;
		Collection<Ability> abilities = selection.getSecond().values();
		if(!abilities.isEmpty())
			for(Ability ability : abilities) 
			{
				if(ability.getRegistryName().equals(AbilityNaturalArmour.REGISTRY_NAME))
					armour += ((AbilityNaturalArmour)ability).amount(); 
				
				if(ability.getRegistryName().equals(AbilityModifierCon.REGISTRY_NAME))
					health += ((AbilityModifier)ability).amount();
			};
		drawHealthAndArmour(matrixStack, xPos, yPos, (int)health, (int)armour);
		
		yPos += this.font.FONT_HEIGHT + 5;
		ITextComponent abilityCount = new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".templates_select.ability_tally", selection.getSecond().size());
		drawString(matrixStack, this.font, abilityCount, xPos, yPos, 16777215);
		
		if(mouseX >= xPos && mouseY <= (yPos + font.FONT_HEIGHT) && mouseY >= 29)
			renderCharacterSummary(matrixStack, mouseX, mouseY, selection.getFirst(), selection.getSecond());
    }
    
    @SuppressWarnings("deprecation")
	private void hideListEdge()
    {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		this.minecraft.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		int listStart = 32;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
	        bufferbuilder.pos(0.0D, (double)listStart, 0.0D).tex(0.0F, (float)listStart / 32.0F).color(64, 64, 64, 255).endVertex();
	        bufferbuilder.pos((double)this.width, (double)listStart, 0.0D).tex((float)this.width / 32.0F, (float)listStart / 32.0F).color(64, 64, 64, 255).endVertex();
	        bufferbuilder.pos((double)this.width, (double)0, 0.0D).tex((float)this.width / 32.0F, (float)0 / 32F).color(64, 64, 64, 255).endVertex();
	        bufferbuilder.pos(0.0D, (double)0, 0.0D).tex(0.0F, (float)0 / 32F).color(64, 64, 64, 255).endVertex();
	    tessellator.draw();
		
		int listEnd = this.height - 51;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
	        bufferbuilder.pos(0.0D, (double)this.height, 0.0D).tex(0.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
	        bufferbuilder.pos((double)this.width, (double)this.height, 0.0D).tex((float)this.width / 32.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
	        bufferbuilder.pos((double)this.width, (double)listEnd, 0.0D).tex((float)this.width / 32.0F, (float)listEnd / 32F).color(64, 64, 64, 255).endVertex();
	        bufferbuilder.pos(0.0D, (double)listEnd, 0.0D).tex(0.0F, (float)listEnd / 32F).color(64, 64, 64, 255).endVertex();
        tessellator.draw();
		RenderSystem.enableTexture();
		RenderSystem.shadeModel(7424);
		RenderSystem.enableAlphaTest();
		RenderSystem.disableBlend();
    }
    
    private void renderCharacterSummary(MatrixStack matrixStack, int mouseX, int mouseY, Types typesIn, Map<ResourceLocation, Ability> abilitiesIn)
    {
		List<ITextComponent> tooltip = Lists.newArrayList();
		tooltip.add(typesIn.toHeader());
		if(!abilitiesIn.isEmpty())
		{
			tooltip.add(new StringTextComponent(""));
			
			List<Ability> abilities = Lists.newArrayList();
			abilities.addAll(abilitiesIn.values());
			abilities.sort(ScreenSelectSpecies.ABILITY_SORT);
			
			Ability.Type type = null;
			for(Ability ability : abilities)
			{
				if(type == null)
				{
					type = ability.getType();
					tooltip.add(type.translated());
				}
				else if(ability.getType() != type)
				{
					type = ability.getType();
					tooltip.add(new StringTextComponent(""));
					tooltip.add(type.translated());
				}
				tooltip.add(new StringTextComponent(" ").append(ability.getDisplayName()));
			}
		}
		
		renderWrappedToolTip(matrixStack, tooltip, mouseX, mouseY, font);
    }
    
    private void renderHighlightedEntry(MatrixStack matrixStack, int mouseX, int mouseY)
    {
    	Template template = this.highlightEntry.template;
		List<ITextComponent> tooltip = Lists.newArrayList();
		if(!template.getPreconditions().isEmpty() && !this.player.isCreative())
		{
			tooltip.add(new TranslationTextComponent("command."+Reference.ModInfo.MOD_ID+".species.templates.info_preconditions", template.getPreconditions().size()));
			template.getPreconditions().forEach((precondition) -> { tooltip.add(new StringTextComponent(" ").append(precondition.translate())); });
		}
		
		if(!template.getOperations().isEmpty())
		{
			if(!tooltip.isEmpty())
				tooltip.add(new StringTextComponent(""));
			
			tooltip.add(new TranslationTextComponent("command."+Reference.ModInfo.MOD_ID+".species.templates.info_operations", template.getOperations().size()));
			
			List<TemplateOperation> totalOperations = Lists.newArrayList();
			totalOperations.addAll(template.getOperations());
			
			while(!totalOperations.isEmpty())
			{
				List<TemplateOperation> group = Lists.newArrayList();
				for(TemplateOperation operation : totalOperations)
					if(group.isEmpty() || operation.canStackWith(group.get(0)))
						group.add(operation);
				totalOperations.removeAll(group);
				
				if(group.size() > 1)
					group.get(0).stackAsList(group).forEach((entry) -> { tooltip.add(new StringTextComponent(" ").append(entry)); });
				else
					tooltip.add(new StringTextComponent(" ").append(group.get(0).translate()));
			}
		}
		
		renderWrappedToolTip(matrixStack, tooltip, mouseX, mouseY, font);
    }
}
