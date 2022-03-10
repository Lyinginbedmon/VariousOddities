package com.lying.variousoddities.client.gui;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Template;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class TemplateList extends ExtendedList<TemplateList.TemplateListEntry>
{
	private final Screen parentScreen;
	private final ITextComponent title;
	private final boolean removeList;
	
	public TemplateList(Minecraft mcIn, Screen screenIn, int listHeightIn, int screenHeightIn, boolean removeIn, ITextComponent titleIn)
	{
		super(mcIn, 120, listHeightIn, 32, screenHeightIn - 51, 20);
		this.parentScreen = screenIn;
		this.removeList = removeIn;
		this.title = titleIn;
		this.setRenderHeader(false, (int)(9F * 1.5F));
	}
	
	protected void renderHeader(MatrixStack p_230448_1_, int p_230448_2_, int p_230448_3_, Tessellator p_230448_4_)
	{
		ITextComponent itextcomponent = (new StringTextComponent("")).append(this.title).mergeStyle(TextFormatting.UNDERLINE, TextFormatting.BOLD);
		this.minecraft.fontRenderer.func_243248_b(p_230448_1_, itextcomponent, (float)(p_230448_2_ + this.width / 2 - this.minecraft.fontRenderer.getStringPropertyWidth(itextcomponent) / 2), (float)Math.min(this.y0 + 3, p_230448_3_), 16777215);
	}
	
	public int getRowWidth(){ return this.width; }
	
	protected int getScrollbarPosition(){ return this.x1 - 6; }
	
	public void clear(){ this.clearEntries(); }
	
	public void setEntries(List<Template> templatesIn)
	{
		this.clearEntries();
		templatesIn.forEach((template) -> { addEntry(template); });
	}
	
	public void addEntry(Template templateIn)
	{
		super.addEntry(new TemplateListEntry(this.minecraft, this, this.parentScreen, this.removeList, templateIn));
	}
	
	public List<Template> getTemplates()
	{
		List<Template> templates = Lists.newArrayList();
		for(int index=0; index<this.getEventListeners().size(); index++)
			templates.add(this.getEntry(index).template);
		return templates;
	}
	
	public class TemplateListEntry extends ExtendedList.AbstractListEntry<TemplateList.TemplateListEntry>
	{
		private final ResourceLocation TEXTURES = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/templates_select.png");
		private final Minecraft mc;
		private final IReorderingProcessor field_243407_e;
		private final TemplateList parentList;
		private final Screen parentScreen;
		public final Template template;
		
		private final boolean remove;
		
		public TemplateListEntry(Minecraft mcIn, TemplateList listIn, Screen screenIn, boolean removeIn, Template templateIn)
		{
			this.mc = mcIn;
			this.field_243407_e = func_244424_a(mcIn, templateIn.getDisplayName());
			this.parentList = listIn;
			this.parentScreen = screenIn;
			this.template = templateIn;
			this.remove = removeIn;
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
		
	 	public boolean mouseClicked(double mouseX, double mouseY, int button)
		{
			double relX = mouseX - (double)this.parentList.getRowLeft();
			if((this.parentList.removeList && relX <= 32D) || (!this.parentList.removeList && relX > this.parentList.getRowWidth() - 32D))
				if(this.parentScreen instanceof ScreenSelectTemplates)
				{
					ScreenSelectTemplates screen = (ScreenSelectTemplates)this.parentScreen;
					if(this.remove)
						screen.removeTemplate(this.template);
					else
						screen.applyTemplate(this.template);
					this.parentList.setScrollAmount(0D);
					return true;
				}
			
			return false;
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
			int left = rowLeft - 2;
			int right = left + texWidth;
			int top = rowTop;
			int bottom = top + texHeight;
			// Top Left
			blit(matrixStack, left, top, 0, texY, texWidth, texHeight);
			// Bottom Left
			blit(matrixStack, left, bottom, 0, texY + 20 - texHeight, texWidth, texHeight);
			// Top Right
			blit(matrixStack, right, top, 200 - texWidth, texY, texWidth, texHeight);
			// Bottom Right
			blit(matrixStack, right, bottom, 200 - texWidth, texY + 20 - texHeight, texWidth, texHeight);
			
			ScreenSelectSpecies.drawStars(matrixStack, template.getPower(), rowLeft + rowWidth - 25 - (9 * Math.max(1, Math.abs(template.getPower()))), rowTop + (rowHeight - 9) / 2);
			this.mc.fontRenderer.func_238407_a_(matrixStack, this.field_243407_e, (float)(rowLeft + 17), (float)(rowTop + (rowHeight - this.mc.fontRenderer.FONT_HEIGHT) / 2), 16777215);
			
			this.mc.getTextureManager().bindTexture(TEXTURES);
			Segment hovered = getMouseOverSegment(mouseX, mouseY, rowLeft, rowTop, rowWidth, rowHeight, this.parentList.removeList);
			int leftSeg = rowLeft + 1;
			int rightSeg = rowLeft + rowWidth - 20 - 4;
			
			int iconY = rowTop + (rowHeight - 16) / 2;
			if(mouseOver && hovered == Segment.INFO)
			{
				blit(matrixStack, this.parentList.removeList ? rightSeg : leftSeg, iconY, 32, 16, 16, 16);
				if(this.parentScreen instanceof ScreenSelectTemplates)
					((ScreenSelectTemplates)this.parentScreen).highlightEntry = this;
			}
			else
				blit(matrixStack, this.parentList.removeList ? rightSeg : leftSeg, iconY, 32, 0, 16, 16);
			
			if(mouseOver)
				if(hovered == Segment.ARROW)
					blit(matrixStack, this.parentList.removeList ? leftSeg : rightSeg, iconY, this.parentList.removeList ? 16 : 0, 16, 16, 16);
				else
					blit(matrixStack, this.parentList.removeList ? leftSeg : rightSeg, iconY, this.parentList.removeList ? 16 : 0, 0, 16, 16);
		}
		
		private Segment getMouseOverSegment(int mouseX, int mouseY, int rowLeft, int rowTop, int rowWidth, int rowHeight, boolean removeList)
		{
			int relY = mouseY - rowTop;
			if(relY < 0 || relY > rowHeight)
				return null;
			
			int relX = mouseX - rowLeft;
			if(relX < 17 && relX > 1)
				return removeList ? Segment.ARROW : Segment.INFO;
			else if(relX >= rowWidth - 20 - 4 && relX < rowWidth)
				return removeList ? Segment.INFO : Segment.ARROW;
			
			return null;
		}
	}
	
	private static enum Segment
	{
		ARROW,
		INFO;
	}
}
