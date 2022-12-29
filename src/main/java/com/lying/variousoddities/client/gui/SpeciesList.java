package com.lying.variousoddities.client.gui;

import java.util.List;

import com.lying.variousoddities.species.Species;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class SpeciesList extends ObjectSelectionList<SpeciesList.SpeciesListEntry>
{
	private final ScreenSelectSpecies parentScreen;
	
	public SpeciesList(Minecraft mcIn, ScreenSelectSpecies screenIn, int listHeightIn, int screenHeightIn, List<Species> entriesIn)
	{
		super(mcIn, 100, listHeightIn, 32, screenHeightIn - 51, 18);
		this.parentScreen = screenIn;
		setEntries(entriesIn);
	}
	
	public int getRowWidth(){ return this.width; }
	
	protected int getScrollbarPosition(){ return this.x1 - 6; }
	
	public void setEntries(List<Species> entriesIn)
	{
		this.clearEntries();
		for(Species entry : entriesIn)
			this.addEntry(new SpeciesListEntry(this.minecraft, entry, this));
	}
	
	public class SpeciesListEntry extends ObjectSelectionList.Entry<SpeciesList.SpeciesListEntry>
	{
		private final Minecraft mc;
		private final FormattedCharSequence displayText;
		private final Species species;
		private final SpeciesList parentList;
		
		public SpeciesListEntry(Minecraft mcIn, Species speciesIn, SpeciesList parentListIn)
		{
			this.species = speciesIn;
			this.parentList = parentListIn;
			this.mc = mcIn;
			this.displayText = truncateIfNecessary(speciesIn.getDisplayName());
		}
		
		private FormattedCharSequence truncateIfNecessary(Component componentIn)
		{
			if(mc.font.width(componentIn) > 157)
			{
				FormattedText itextproperties = FormattedText.composite(minecraft.font.substrByWidth(componentIn, 157 - minecraft.font.width("...")), FormattedText.of("..."));
				return Language.getInstance().getVisualOrder(itextproperties);
			}
			else
				return componentIn.getVisualOrderText();
		}
		
		public void render(PoseStack matrixStack, int slotIndex, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean mouseOver, float partialTicks)
		{
			RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableDepthTest();
			int texY = 46 + (mouseOver ? 2 : 1) * 20;
			if(this.parentList.parentScreen.getCurrentSpecies().getRegistryName().equals(species.getRegistryName()))
				texY = 46;
			
			int texWidth = Math.min(200, rowWidth) / 2;
			int texHeight = Math.min(20, rowHeight + 3) / 2;
			// Top Left
			blit(matrixStack, rowLeft - 2, rowTop, 0, texY, texWidth, texHeight);
			// Bottom Left
			blit(matrixStack, rowLeft - 2, rowTop + texHeight, 0, texY + 20 - texHeight, texWidth, texHeight);
			// Top Right
			blit(matrixStack, rowLeft - 2 + texWidth, rowTop, 200 - texWidth, texY, texWidth, texHeight);
			// Bottom Right
			blit(matrixStack, rowLeft - 2 + texWidth, rowTop + texHeight, 200 - texWidth, texY + 20 - texHeight, texWidth, texHeight);
			
			GuiComponent.drawString(matrixStack, mc.font, this.displayText, (rowLeft + 2), (rowTop + (rowHeight + 3 - mc.font.lineHeight) / 2), 16777215);
		}
		
	 	public boolean mouseClicked(double mouseX, double mouseY, int button)
		{
			if(this.parentList.parentScreen.getCurrentSpecies().getRegistryName().equals(species.getRegistryName()))
				return false;
			
			double relX = mouseX - (double)this.parentList.getRowLeft();
			if(relX > 0 && relX < this.parentList.getRowWidth())
			{
				this.parentList.parentScreen.setCurrentSpecies(this.species);
				return true;
			}
			
			return false;
		}
	 	
	 	public Component getNarration() { return this.species.getDisplayName(); }
	}
}
