package com.lying.variousoddities.client.gui;

import java.util.List;

import com.lying.variousoddities.species.Species;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
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
		for(Species entry : entriesIn)
			this.addEntry(new SpeciesListEntry(mcIn, entry, this));
	}
	
	public int getRowWidth(){ return this.width; }
	
	protected int getScrollbarPosition(){ return this.x1 - 6; }
	
	public class SpeciesListEntry extends ObjectSelectionList.Entry<SpeciesList.SpeciesListEntry>
	{
		private final Minecraft mc;
		private final FormattedCharSequence field_243407_e;
		private final Species species;
		private final SpeciesList parentList;
		
		public SpeciesListEntry(Minecraft mcIn, Species speciesIn, SpeciesList parentListIn)
		{
			this.species = speciesIn;
			this.parentList = parentListIn;
			this.mc = mcIn;
			this.field_243407_e = func_244424_a(mcIn, speciesIn.getDisplayName());
		}
		
		private FormattedCharSequence func_244424_a(Minecraft p_244424_0_, Component p_244424_1_)
		{
			int i = p_244424_0_.font.width(p_244424_1_);
			if (i > 157)
			{
				FormattedText itextproperties = FormattedText.composite(p_244424_0_.font.substrByWidth(p_244424_1_, 157 - p_244424_0_.font.width("...")), FormattedText.of("..."));
				return Language.getInstance().getVisualOrder(itextproperties);
			}
			else
				return p_244424_1_.getVisualOrderText();
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
			
			this.mc.font.draw(matrixStack, this.field_243407_e, (float)(rowLeft + 2), (float)(rowTop + (rowHeight + 3 - mc.font.lineHeight) / 2), 16777215);
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
	 	
	 	public Component getNarration() { return null; }
	}
}
