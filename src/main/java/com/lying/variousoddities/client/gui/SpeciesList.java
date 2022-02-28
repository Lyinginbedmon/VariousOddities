package com.lying.variousoddities.client.gui;

import java.util.List;

import com.lying.variousoddities.species.Species;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;

public class SpeciesList extends ExtendedList<SpeciesList.SpeciesListEntry>
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
	
	public class SpeciesListEntry extends ExtendedList.AbstractListEntry<SpeciesList.SpeciesListEntry>
	{
		private final Minecraft mc;
		private final IReorderingProcessor field_243407_e;
		private final Species species;
		private final SpeciesList parentList;
		
		public SpeciesListEntry(Minecraft mcIn, Species speciesIn, SpeciesList parentListIn)
		{
			this.species = speciesIn;
			this.parentList = parentListIn;
			this.mc = mcIn;
			this.field_243407_e = func_244424_a(mcIn, speciesIn.getDisplayName());
		}
		
		private IReorderingProcessor func_244424_a(Minecraft p_244424_0_, ITextComponent p_244424_1_)
		{
			int i = p_244424_0_.fontRenderer.getStringPropertyWidth(p_244424_1_);
			if (i > 157)
			{
				ITextProperties itextproperties = ITextProperties.func_240655_a_(p_244424_0_.fontRenderer.func_238417_a_(p_244424_1_, 157 - p_244424_0_.fontRenderer.getStringWidth("...")), ITextProperties.func_240652_a_("..."));
				return LanguageMap.getInstance().func_241870_a(itextproperties);
			}
			else
				return p_244424_1_.func_241878_f();
		}
		
		@SuppressWarnings("deprecation")
		public void render(MatrixStack matrixStack, int slotIndex, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean mouseOver, float partialTicks)
		{
			this.mc.getTextureManager().bindTexture(Widget.WIDGETS_LOCATION);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableDepthTest();
			int texY = 46 + (mouseOver ? 2 : 1) * 20;
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
			
			IReorderingProcessor processor = this.field_243407_e;
			this.mc.fontRenderer.func_238407_a_(matrixStack, processor, (float)(rowLeft + 2), (float)(rowTop + (rowHeight + 3 - mc.fontRenderer.FONT_HEIGHT) / 2), 16777215);
		}
		
	 	public boolean mouseClicked(double mouseX, double mouseY, int button)
		{
			double relX = mouseX - (double)this.parentList.getRowLeft();
			if(relX > 0 && relX < this.parentList.getRowWidth())
				{
					this.parentList.parentScreen.setCurrentSpecies(this.species);
					return true;
				}
			
			return false;
		}
	}
}
